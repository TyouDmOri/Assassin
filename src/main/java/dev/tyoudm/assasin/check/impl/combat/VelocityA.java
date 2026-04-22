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
import dev.tyoudm.assasin.data.tracker.VelocityTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * VelocityA — Horizontal knockback ratio check.
 *
 * <p>Detects velocity hacks by comparing the player's observed horizontal
 * velocity response to the expected knockback vector. A ratio below
 * {@link #MIN_RATIO} indicates the player is ignoring or reducing knockback.
 *
 * <h2>Block-hit modifier</h2>
 * When {@link ExemptType#BLOCK_HIT} is active, the expected horizontal
 * magnitude is multiplied by 0.5 (shield absorption).
 *
 * <h2>W-tap exempt</h2>
 * W-tap suppresses this check for 5 ticks via {@link ExemptType#WTAP}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "VelocityA",
    type              = CheckType.VELOCITY_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects horizontal knockback ratio violation.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "velocity"
)
public final class VelocityA extends Check {

    /** Minimum accepted horizontal KB ratio (observed / expected). */
    private static final double MIN_RATIO = 0.5;

    private final LegitTechniqueRegistry legitRegistry;

    public VelocityA(final MitigationEngine engine,
                     final LegitTechniqueRegistry legitRegistry) {
        super(engine);
        this.legitRegistry = legitRegistry;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.WTAP, ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final VelocityTracker vt = data.getVelocityTracker();
        if (vt == null || !vt.hasPending()) return;

        // Consume the pending KB entry
        final double expectedH = vt.getPendingHorizontalMagnitude();
        if (expectedH < 0.01) {
            vt.consume(true);
            return;
        }

        // Apply block-hit modifier
        final double modifier = isExempt(data, ExemptType.BLOCK_HIT, tick)
            ? legitRegistry.get(LegitTechniqueRegistry.Technique.BLOCK_HIT).kbMultiplier()
            : 1.0;
        final double adjustedExpected = expectedH * modifier;

        final double observedH = data.getVelocityH();
        final double ratio     = adjustedExpected > 0 ? observedH / adjustedExpected : 1.0;

        if (ratio < MIN_RATIO) {
            vt.consume(false);
            flag(player, data, (MIN_RATIO - ratio) * 3.0,
                String.format("ratio=%.3f min=%.3f observedH=%.4f expectedH=%.4f",
                    ratio, MIN_RATIO, observedH, adjustedExpected),
                tick);
        } else {
            vt.consume(true);
        }
    }
}
