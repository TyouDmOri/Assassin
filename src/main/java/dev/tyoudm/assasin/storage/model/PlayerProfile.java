/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.storage.model;

import java.util.UUID;

/**
 * Immutable record representing a player's persistent profile stored in
 * {@code assasin_players}.
 *
 * @param uuid            player UUID (primary key)
 * @param name            last known username
 * @param firstJoinMs     timestamp of first join (ms)
 * @param lastJoinMs      timestamp of last join (ms)
 * @param totalViolations total violation events recorded
 * @param banned          whether the player is currently banned
 * @param banReason       ban reason (empty if not banned)
 *
 * @author TyouDm
 * @version 1.0.0
 */
public record PlayerProfile(
    UUID   uuid,
    String name,
    long   firstJoinMs,
    long   lastJoinMs,
    int    totalViolations,
    boolean banned,
    String banReason
) {
    /** Creates a new profile for a first-time player. */
    public static PlayerProfile newPlayer(final UUID uuid, final String name, final long nowMs) {
        return new PlayerProfile(uuid, name, nowMs, nowMs, 0, false, "");
    }

    /** Returns a copy with the last-join timestamp updated. */
    public PlayerProfile withLastJoin(final long nowMs) {
        return new PlayerProfile(uuid, name, firstJoinMs, nowMs,
            totalViolations, banned, banReason);
    }

    /** Returns a copy with the violation count incremented. */
    public PlayerProfile withViolation() {
        return new PlayerProfile(uuid, name, firstJoinMs, lastJoinMs,
            totalViolations + 1, banned, banReason);
    }
}
