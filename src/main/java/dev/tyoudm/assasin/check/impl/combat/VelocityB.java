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
import dev.tyoudm.assasin.data.tracker.VelocityTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * VelocityB — Vertical knockback ratio check.
 *
 * <p>Detects velocity hacks that reduce or eliminate the vertical component
 * of knockback. Complements {@link VelocityA} which checks horizontal KB.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "VelocityB",
    type              = CheckType.VELOCITY_B,
    category          = CheckCategory.COMBAT,
    description       = "Detects vertical knockback ratio violation.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "velocity"
)
public final class VelocityB extends Check {

    /** Minimum accepted vertical KB ratio. */
    private static final double MIN_RATIO = 0.4;

    public VelocityB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.WTAP, ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final VelocityTracker vt = data.getVelocityTracker();
        if (vt == null || !vt.hasPending()) return;

        final double expectedY = vt.getPendingVelY();
        if (expectedY < 0.05) return; // no significant vertical KB

        final double observedY = data.getVelocityY();
        final double ratio     = expectedY > 0 ? observedY / expectedY : 1.0;

        if (ratio < MIN_RATIO) {
            flag(player, data, (MIN_RATIO - ratio) * 3.0,
                String.format("ratioY=%.3f min=%.3f observedY=%.4f expectedY=%.4f",
                    ratio, MIN_RATIO, observedY, expectedY),
                tick);
        }
    }
}
