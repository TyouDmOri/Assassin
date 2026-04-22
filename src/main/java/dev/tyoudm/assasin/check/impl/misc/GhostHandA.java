/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.misc;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * GhostHandA — Interact without prior arm swing detection.
 *
 * <p>Detects "ghost hand" exploits where the client sends an interact
 * (block place, item use) packet without the preceding arm swing animation
 * packet. Vanilla clients always send an arm swing before interacting.
 *
 * <p>This is used by some hacked clients to interact with blocks or
 * entities without the server seeing the arm animation, which can be
 * used to bypass certain anti-cheat checks that rely on arm swing timing.
 *
 * <h2>Algorithm</h2>
 * Tracks whether an arm swing was received in the last {@link #ARM_SWING_WINDOW_TICKS}
 * ticks before an interact packet. If no arm swing preceded the interact,
 * it's suspicious.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "GhostHandA",
    type              = CheckType.MISC_C,
    category          = CheckCategory.MISC,
    description       = "Detects interact without prior arm swing (ghost hand).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "medium"
)
public final class GhostHandA extends Check {

    /** Maximum ticks between arm swing and interact to be considered valid. */
    private static final int ARM_SWING_WINDOW_TICKS = 3;

    /** Consecutive ghost-hand interactions required before flagging. */
    private static final int MIN_CONSECUTIVE = 3;

    // Per-player state
    private long lastArmSwingTick;
    private int  ghostHandCount;

    public GhostHandA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Per-tick check is not needed — driven by packet events
    }

    /**
     * Records an arm swing event. Call from the arm animation packet handler.
     *
     * @param tick current server tick
     */
    public void onArmSwing(final long tick) {
        lastArmSwingTick = tick;
    }

    /**
     * Validates an interact event. Call from the interact/use-item packet handler.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   current server tick
     */
    public void onInteract(final Player player, final PlayerData data, final long tick) {
        if (data.getExemptManager().isExemptAny(tick,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK,
                ExemptType.GAMEMODE, ExemptType.DEAD)) return;

        final long ticksSinceSwing = tick - lastArmSwingTick;

        if (lastArmSwingTick == 0 || ticksSinceSwing > ARM_SWING_WINDOW_TICKS) {
            ghostHandCount++;
            if (ghostHandCount >= MIN_CONSECUTIVE) {
                flag(player, data, 1.0,
                    String.format("ghost hand: no arm swing in last %dt (gap=%dt consecutive=%d)",
                        ARM_SWING_WINDOW_TICKS, ticksSinceSwing, ghostHandCount),
                    tick);
            }
        } else {
            ghostHandCount = 0;
        }
    }
}
