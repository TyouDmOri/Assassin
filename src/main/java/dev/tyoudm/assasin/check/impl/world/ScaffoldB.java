/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.world;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.core.LegitTechniqueRegistry;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import dev.tyoudm.assasin.data.tracker.RotationTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.WelfordStats;
import org.bukkit.entity.Player;

/**
 * ScaffoldB — Invalid angles and godbridge jitter detection.
 *
 * <p>Detects scaffold hacks that use impossible pitch angles (e.g., looking
 * straight down at -90° while placing blocks behind) or godbridge bots that
 * produce sub-tick pitch changes without human jitter.
 *
 * <h2>Godbridge detection</h2>
 * Legitimate godbridge has pitch ≈ 80° with σ &gt; {@link #MIN_PITCH_SIGMA}
 * (human jitter). Bots produce instantaneous sub-tick pitch changes with
 * σ ≈ 0 — this is flagged.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ScaffoldB",
    type              = CheckType.SCAFFOLD_B,
    category          = CheckCategory.WORLD,
    description       = "Detects scaffold invalid angles and godbridge bot detection.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class ScaffoldB extends Check {

    /** Minimum pitch σ required for godbridge to be considered human. */
    private static final double MIN_PITCH_SIGMA = 0.1;

    /** Minimum samples before godbridge check activates. */
    private static final int MIN_SAMPLES = 10;

    /** Pitch range considered "godbridge pitch" (degrees). */
    private static final double GODBRIDGE_PITCH_MIN = 70.0;
    private static final double GODBRIDGE_PITCH_MAX = 90.0;

    private final LegitTechniqueRegistry legitRegistry;

    public ScaffoldB(final MitigationEngine engine,
                     final LegitTechniqueRegistry legitRegistry) {
        super(engine);
        this.legitRegistry = legitRegistry;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastPlaceTick() != tick) return;

        final RotationTracker rt = data.getRotationTracker();
        if (rt == null) return;

        final float pitch = data.getPitch();

        // Godbridge check: pitch in godbridge range with near-zero σ
        if (pitch >= GODBRIDGE_PITCH_MIN && pitch <= GODBRIDGE_PITCH_MAX) {
            final WelfordStats pitchStats = rt.getPitchDeltaStats();
            if (pitchStats.count() >= MIN_SAMPLES) {
                final double sigma = pitchStats.stdDev();
                final double minSigma = legitRegistry
                    .get(LegitTechniqueRegistry.Technique.GODBRIDGE).sigmaThreshold();

                if (sigma < minSigma) {
                    flag(player, data, 1.5,
                        String.format("godbridge bot: pitch=%.1f σ=%.4f min=%.4f",
                            pitch, sigma, minSigma),
                        tick);
                }
            }
        }
    }
}
