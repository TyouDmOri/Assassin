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
 * AimC — Pitch variance detection.
 *
 * <p>Detects aim assist by measuring the variance of pitch deltas.
 * Legitimate players have natural pitch variance; aim assist often
 * locks pitch to a fixed value (near-zero pitch variance).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AimC",
    type              = CheckType.AIM_C,
    category          = CheckCategory.COMBAT,
    description       = "Detects aim assist via pitch variance analysis.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "combat"
)
public final class AimC extends Check {

    private static final int    MIN_SAMPLES         = 20;
    private static final double MIN_PITCH_VARIANCE  = 0.005; // below this = suspicious

    public AimC(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final RotationTracker rt = data.getRotationTracker();
        if (rt == null) return;

        final double pitchVariance = rt.getPitchDeltaStats().variance();
        final long   count         = rt.getPitchDeltaStats().count();

        if (count < MIN_SAMPLES) return;

        if (pitchVariance < MIN_PITCH_VARIANCE) {
            flag(player, data, 1.0,
                String.format("pitchVariance=%.6f min=%.6f n=%d",
                    pitchVariance, MIN_PITCH_VARIANCE, count),
                tick);
        }
    }
}
