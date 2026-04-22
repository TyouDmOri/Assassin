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
import dev.tyoudm.assasin.util.MathUtil;
import org.bukkit.entity.Player;

/**
 * KillauraA — Rotation delta pre-hit detection.
 *
 * <p>Detects killaura by checking whether the player's yaw/pitch changed
 * significantly between the last rotation packet and the attack. Legitimate
 * players aim before attacking; killaura often attacks without rotating.
 *
 * <h2>Algorithm</h2>
 * At attack time, compare the yaw at attack vs. the yaw from the previous
 * rotation packet. If the delta is below {@link #MIN_ROTATION_DELTA} for
 * multiple consecutive attacks, it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "KillauraA",
    type              = CheckType.KILLAURA_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects killaura via rotation delta pre-hit.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class KillauraA extends Check {

    /** Minimum yaw delta (degrees) expected between rotation and attack. */
    private static final double MIN_ROTATION_DELTA = 0.5;

    /** Consecutive low-delta attacks required before flagging. */
    private static final int MIN_CONSECUTIVE = 3;

    private int lowDeltaCount;
    private float lastYawBeforeAttack;

    public KillauraA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return; // only on attack tick

        final float currentYaw = data.getYaw();
        final double delta = MathUtil.angleDiff(currentYaw, lastYawBeforeAttack);

        if (delta < MIN_ROTATION_DELTA) {
            lowDeltaCount++;
            if (lowDeltaCount >= MIN_CONSECUTIVE) {
                flag(player, data, 1.0,
                    String.format("rotDelta=%.3f° min=%.3f° consecutive=%d",
                        delta, MIN_ROTATION_DELTA, lowDeltaCount),
                    tick);
            }
        } else {
            lowDeltaCount = 0;
        }

        lastYawBeforeAttack = currentYaw;
    }
}
