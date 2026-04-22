/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.player;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * FastEatA — Eating speed bypass detection.
 *
 * <p>Detects when a player consumes food faster than the vanilla eating
 * duration. Vanilla eating takes 32 ticks (1.6 seconds). Fast-eat hacks
 * reduce this to 1–5 ticks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "FastEatA",
    type              = CheckType.FAST_EAT_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects eating speed bypass.",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class FastEatA extends Check {

    /** Vanilla eating duration in ticks. */
    private static final int VANILLA_EAT_TICKS = 32;

    /** Tolerance: allow eating up to this many ticks faster. */
    private static final int TOLERANCE_TICKS = 5;

    private long eatStartTick;
    private boolean wasEating;

    public FastEatA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final boolean isEating = player.isHandRaised()
            && isFood(player.getInventory().getItemInMainHand());

        if (isEating && !wasEating) {
            // Started eating
            eatStartTick = tick;
        } else if (!isEating && wasEating && eatStartTick > 0) {
            // Finished eating
            final long eatTicks = tick - eatStartTick;
            final int  minTicks = VANILLA_EAT_TICKS - TOLERANCE_TICKS;

            if (eatTicks < minTicks) {
                flag(player, data, 2.0,
                    String.format("fast eat: %dt (min %dt vanilla %dt)",
                        eatTicks, minTicks, VANILLA_EAT_TICKS),
                    tick);
            }
            eatStartTick = 0;
        }

        wasEating = isEating;
    }

    private static boolean isFood(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return item.getType().isEdible();
    }
}
