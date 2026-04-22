/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.movement;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.prediction.CollisionEngine;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * PhaseA — Block phase-through detection.
 *
 * <p>Flags when the player's AABB overlaps a solid block according to the
 * server-side {@link CollisionEngine}. Catches phase hacks that allow
 * moving through walls.
 *
 * <h2>False-flag prevention</h2>
 * <ul>
 *   <li>Exempt during pearl (3s), teleport, setback.</li>
 *   <li>Only flags after {@link #MIN_OVERLAP_TICKS} consecutive overlap ticks
 *       to avoid false positives from lag-induced position desync.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "PhaseA",
    type             = CheckType.PHASE_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects block phase-through.",
    maxVl            = 8.0,
    severity         = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "hard"
)
public final class PhaseA extends Check {

    /** Consecutive overlap ticks required before flagging. */
    private static final int MIN_OVERLAP_TICKS = 3;

    private int overlapTicks;

    public PhaseA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.PEARL, ExemptType.TELEPORT_PENDING,
                ExemptType.SETBACK, ExemptType.VEHICLE,
                ExemptType.CLIMBABLE)) return;

        final boolean overlaps = CollisionEngine.overlapsBlock(
            player.getWorld(), data.getX(), data.getY(), data.getZ(), data.isSneaking());

        if (overlaps) {
            overlapTicks++;
            if (overlapTicks >= MIN_OVERLAP_TICKS) {
                flag(player, data, 2.0,
                    String.format("overlap at (%.2f, %.2f, %.2f) for %dt",
                        data.getX(), data.getY(), data.getZ(), overlapTicks),
                    tick);
            }
        } else {
            overlapTicks = 0;
        }
    }
}
