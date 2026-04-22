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
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.WelfordStats;
import org.bukkit.entity.Player;

/**
 * TowerA — Tower hack detection via jump+place timing variance.
 *
 * <p>Detects tower hacks by measuring the variance (σ) of the interval
 * between jumps and block placements. Legitimate tower building has high
 * variance; tower hacks have near-zero variance (perfectly timed).
 *
 * <h2>Algorithm</h2>
 * Records the tick delta between each jump and the subsequent block
 * placement. If σ &lt; {@link #MIN_SIGMA_TICKS} with n ≥ {@link #MIN_SAMPLES},
 * it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "TowerA",
    type              = CheckType.TOWER_A,
    category          = CheckCategory.WORLD,
    description       = "Detects tower hack via jump+place timing variance.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class TowerA extends Check {

    private static final int    MIN_SAMPLES    = 8;
    private static final double MIN_SIGMA_TICKS = 1.5;

    private final WelfordStats jumpPlaceIntervalStats = new WelfordStats();
    private long lastJumpTick;

    public TowerA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        // Record jump tick
        final boolean justJumped = !data.isOnGround() && data.wasOnGround();
        if (justJumped) {
            lastJumpTick = tick;
            return;
        }

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastPlaceTick() != tick) return;
        if (lastJumpTick == 0) return;

        // Only check upward placements (tower = placing below while jumping)
        if (bt.getLastPlacedFace() != org.bukkit.block.BlockFace.DOWN) return;

        final long interval = tick - lastJumpTick;
        if (interval > 10) return; // too far from jump — not tower

        jumpPlaceIntervalStats.add(interval);

        if (jumpPlaceIntervalStats.count() >= MIN_SAMPLES
                && jumpPlaceIntervalStats.stdDev() < MIN_SIGMA_TICKS) {
            flag(player, data, 1.0,
                String.format("tower σ=%.3f min=%.3f n=%d",
                    jumpPlaceIntervalStats.stdDev(), MIN_SIGMA_TICKS,
                    jumpPlaceIntervalStats.count()),
                tick);
        }
    }
}
