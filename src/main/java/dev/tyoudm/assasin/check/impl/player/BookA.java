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
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * BookA — BOOK_EDIT payload size check.
 *
 * <p>Detects when a player sends a book edit packet with a payload larger
 * than 8KB. Large book payloads can cause server lag or crashes.
 *
 * <p>This check is triggered by the book edit packet handler (FASE 4),
 * not by the movement tick. The {@link #onBookEdit(Player, PlayerData, int, long)}
 * method is called directly by the packet handler.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "BookA",
    type              = CheckType.BOOK_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects BOOK_EDIT payload exceeding 8KB.",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class BookA extends Check {

    /** Maximum allowed book payload size in bytes. */
    public static final int MAX_PAYLOAD_BYTES = 8 * 1024; // 8KB

    public BookA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // This check is triggered externally via onBookEdit — no per-tick logic
    }

    /**
     * Called by the book edit packet handler when a book edit packet is received.
     *
     * @param player      the player
     * @param data        the player's data
     * @param payloadSize the payload size in bytes
     * @param tick        current server tick
     */
    public void onBookEdit(final Player player, final PlayerData data,
                           final int payloadSize, final long tick) {
        if (payloadSize > MAX_PAYLOAD_BYTES) {
            flag(player, data, 3.0,
                String.format("book payload: %d bytes (max %d)", payloadSize, MAX_PAYLOAD_BYTES),
                tick);
        }
    }
}
