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
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.RotationTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * AimB — Sensitivity constant detection.
 *
 * <p>Detects aim assist by checking whether the ratio of yaw delta to
 * pitch delta remains suspiciously constant across multiple rotation
 * packets. Legitimate mouse input has a variable ratio; aim assist
 * often locks the ratio to a fixed value.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AimB",
    type              = CheckType.AIM_B,
    category          = CheckCategory.COMBAT,
    description       = "Detects aim assist via constant yaw/pitch ratio.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "combat"
)
public final class AimB extends Check {

    private static final int    MIN_SAMPLES       = 20;
    private static final double MAX_RATIO_VARIANCE = 0.01; // very low variance = suspicious

    public AimB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final RotationTracker rt = data.getRotationTracker();
        if (rt == null || rt.getYawDeltas().size() < MIN_SAMPLES) return;

        final var yawD   = rt.getYawDeltas();
        final var pitchD = rt.getPitchDeltas();
        final int n      = Math.min(yawD.size(), pitchD.size());

        // Compute variance of yaw/pitch ratio
        double sumRatio  = 0.0;
        double sumRatio2 = 0.0;
        int    count     = 0;

        for (int i = 0; i < n; i++) {
            final double p = pitchD.get(i);
            if (Math.abs(p) < 0.01) continue; // avoid division by near-zero
            final double ratio = yawD.get(i) / p;
            sumRatio  += ratio;
            sumRatio2 += ratio * ratio;
            count++;
        }

        if (count < MIN_SAMPLES) return;

        final double mean     = sumRatio / count;
        final double variance = sumRatio2 / count - mean * mean;

        if (variance < MAX_RATIO_VARIANCE && Math.abs(mean) > 0.1) {
            flag(player, data, 1.0,
                String.format("ratio variance=%.5f max=%.5f mean=%.3f n=%d",
                    variance, MAX_RATIO_VARIANCE, mean, count),
                tick);
        }
    }
}
