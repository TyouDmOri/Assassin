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
import dev.tyoudm.assasin.data.tracker.CombatTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * CriticalsA — Fake critical hit detection.
 *
 * <p>A critical hit requires: {@code onGround=false} AND {@code motionY < 0}
 * (player is descending). Detects fake crits where the client reports a
 * critical hit while the player is on the ground or ascending.
 *
 * <h2>Legit crit-tapping</h2>
 * Crit-tapping (jumping and attacking at the peak) is legitimate.
 * This check only flags when the server-side state contradicts the crit.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "CriticalsA",
    type              = CheckType.CRITICALS_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects fake critical hits.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class CriticalsA extends Check {

    public CriticalsA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;
        if (!ct.isLastWasCrit()) return; // only check crit attacks

        // Server-side crit conditions: not on ground AND motionY < 0
        final boolean serverOnGround = data.isOnGround();
        final double  motionY        = ct.getAttackMotionY();

        if (serverOnGround || motionY >= 0) {
            flag(player, data, 2.0,
                String.format("fakeCrit onGround=%b motionY=%.4f", serverOnGround, motionY),
                tick);
        }
    }
}
