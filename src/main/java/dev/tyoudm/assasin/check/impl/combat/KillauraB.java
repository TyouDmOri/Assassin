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
 * KillauraB — Multi-target killaura detection.
 *
 * <p>Detects killaura that attacks multiple targets in rapid succession
 * without the yaw rotation required to face each target. Legitimate
 * combo-reset involves rotation pre-hit; killaura switches targets
 * without rotating.
 *
 * <h2>Legit combo-reset</h2>
 * Exempt when {@link ExemptType#ATTRIBUTE_SWAP} is active (held-item change
 * between attacks) — this is the combo-reset pattern.
 *
 * <h2>Algorithm</h2>
 * If the player switches targets and the yaw delta between attacks is
 * &gt; {@link #MAX_YAW_DELTA_FOR_SWITCH} degrees, it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "KillauraB",
    type              = CheckType.KILLAURA_B,
    category          = CheckCategory.COMBAT,
    description       = "Detects multi-target killaura without rotation.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class KillauraB extends Check {

    /**
     * Maximum yaw delta (degrees/tick) allowed when switching targets.
     * Legitimate players rotate to face the new target; killaura doesn't.
     */
    private static final double MAX_YAW_DELTA_FOR_SWITCH = 180.0;

    /** Minimum ticks between target switches to consider suspicious. */
    private static final int MIN_SWITCH_INTERVAL_TICKS = 2;

    public KillauraB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final CombatTracker ct = data.getCombatTracker();
        if (ct == null || ct.getLastAttackTick() != tick) return;

        // Only check on target switch
        if (tick - ct.getLastTargetSwitchTick() > MIN_SWITCH_INTERVAL_TICKS) return;

        final double yawDelta = MathUtil.angleDiff(data.getYaw(), ct.getAttackYaw());

        // Killaura switches targets with huge yaw jumps in a single tick
        if (yawDelta > MAX_YAW_DELTA_FOR_SWITCH) {
            flag(player, data, 1.5,
                String.format("targetSwitch yawDelta=%.1f° max=%.1f° targets=%d",
                    yawDelta, MAX_YAW_DELTA_FOR_SWITCH, ct.getRecentTargetCount()),
                tick);
        }
    }
}
