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
import dev.tyoudm.assasin.latency.PingCompensator;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * VelocityC — Knockback timing check.
 *
 * <p>Detects velocity hacks that delay the knockback response beyond the
 * expected window. If the player does not respond to knockback within
 * {@code baseWindow + pingCompensation} ticks, it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "VelocityC",
    type              = CheckType.VELOCITY_C,
    category          = CheckCategory.COMBAT,
    description       = "Detects knockback timing violation (delayed response).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "velocity"
)
public final class VelocityC extends Check {

    public VelocityC(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.WTAP, ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final VelocityTracker vt = data.getVelocityTracker();
        if (vt == null || !vt.hasPending()) return;

        final long pendingTick = vt.getPendingTick();
        final int  window      = PingCompensator.velocityCompensationTicks(data.getPing())
                                 + 3; // base window

        if (tick - pendingTick > window) {
            // KB was sent but player hasn't responded in time
            vt.consume(false);
            flag(player, data, 1.0,
                String.format("kbDelay=%dt window=%dt ping=%d",
                    (int)(tick - pendingTick), window, data.getPing()),
                tick);
        }
    }
}
