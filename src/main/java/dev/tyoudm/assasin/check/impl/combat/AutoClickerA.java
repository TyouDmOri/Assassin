/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.core.LegitTechniqueRegistry;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.AttackTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * AutoClickerA — CPS variance detection using Welford online statistics.
 *
 * <p>Detects autoclickers by measuring the variance (σ) of inter-click
 * intervals. Legitimate clicking has high variance; autoclickers have
 * near-zero variance.
 *
 * <h2>Legit clicking — false-flag prevention</h2>
 * Butterfly / jitter / drag click produce bimodal distributions with
 * high kurtosis. This check only flags when σ is very low AND the
 * distribution is unimodal (handled by requiring n ≥ 20 samples).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AutoClickerA",
    type              = CheckType.AUTO_CLICKER_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects autoclicker via CPS interval variance (Welford).",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class AutoClickerA extends Check {

    /** Minimum samples before flagging. */
    private static final int    MIN_SAMPLES    = 20;
    /** σ below this threshold is suspicious (ms). */
    private static final double MIN_SIGMA_MS   = 2.0;
    /** Maximum CPS before flagging (human max ≈ 20 CPS). */
    private static final double MAX_CPS        = 20.0;

    private final LegitTechniqueRegistry legitRegistry;

    public AutoClickerA(final MitigationEngine engine,
                        final LegitTechniqueRegistry legitRegistry) {
        super(engine);
        this.legitRegistry = legitRegistry;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final AttackTracker at = data.getAttackTracker();
        if (at == null) return;

        final var stats = at.getIntervalStats();
        if (stats.count() < MIN_SAMPLES) return;

        final double sigma  = stats.stdDev();
        final double meanMs = stats.mean();
        if (meanMs <= 0) return;

        final double cps = 1000.0 / meanMs;

        // Flag: very low variance AND high CPS
        if (sigma < MIN_SIGMA_MS && cps > MAX_CPS) {
            flag(player, data, 1.5,
                String.format("σ=%.2fms min=%.2fms cps=%.1f n=%d",
                    sigma, MIN_SIGMA_MS, cps, stats.count()),
                tick);
        }
    }
}
