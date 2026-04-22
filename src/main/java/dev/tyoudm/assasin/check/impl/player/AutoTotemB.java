/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.player;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.core.LegitTechniqueRegistry;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.WelfordStats;
import org.bukkit.entity.Player;

/**
 * AutoTotemB — Totem reswap variance check.
 *
 * <p>Detects auto-totem by measuring the variance (σ) of totem reswap
 * intervals. Legitimate players have high variance; auto-totem has
 * near-zero variance.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AutoTotemB",
    type              = CheckType.AUTO_TOTEM_B,
    category          = CheckCategory.PLAYER,
    description       = "Detects auto-totem via reswap interval variance (σ < 1.5).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class AutoTotemB extends Check {

    private final LegitTechniqueRegistry legitRegistry;
    private final WelfordStats reswapIntervalStats = new WelfordStats();
    private long lastSwapMs;

    public AutoTotemB(final MitigationEngine engine,
                      final LegitTechniqueRegistry legitRegistry) {
        super(engine);
        this.legitRegistry = legitRegistry;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final InventoryTracker it = data.getInventoryTracker();
        if (it == null) return;

        final long swapMs = it.getLastTotemSwapMs();
        if (swapMs == 0 || swapMs == lastSwapMs) return;

        if (lastSwapMs > 0) {
            final long interval = swapMs - lastSwapMs;
            if (interval > 0 && interval < 10_000) { // sanity: ignore gaps > 10s
                reswapIntervalStats.add(interval);
            }
        }
        lastSwapMs = swapMs;

        final var tolerance = legitRegistry.get(LegitTechniqueRegistry.Technique.TOTEM_SWAP);
        if (reswapIntervalStats.count() < tolerance.minSamples()) return;

        final double sigma = reswapIntervalStats.stdDev();
        if (sigma < tolerance.sigmaThreshold()) {
            flag(player, data, 1.5,
                String.format("auto-totem σ=%.3f min=%.3f n=%d",
                    sigma, tolerance.sigmaThreshold(), reswapIntervalStats.count()),
                tick);
        }
    }
}
