/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all online players' {@link PlayerData} instances.
 *
 * <p>Uses a {@link ConcurrentHashMap} keyed by {@link UUID} to allow
 * lock-free reads from multiple threads (netty, async check processors,
 * main thread). Writes (join/quit) happen on the main thread only.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>{@link #create(Player, long)} — called on {@code PlayerJoinEvent}.</li>
 *   <li>{@link #get(UUID)} — called by packet handlers and checks.</li>
 *   <li>{@link #remove(UUID)} — called on {@code PlayerQuitEvent}.</li>
 * </ol>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class PlayerDataManager {

    /** UUID → PlayerData map. ConcurrentHashMap for lock-free reads. */
    private final ConcurrentHashMap<UUID, PlayerData> dataMap = new ConcurrentHashMap<>(64);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Creates and registers a new {@link PlayerData} for the given player.
     *
     * <p>If an entry already exists (e.g., duplicate join event), it is
     * replaced with a fresh instance.
     *
     * @param player   the joining player
     * @param joinTick the current server tick
     * @return the newly created {@link PlayerData}
     */
    @NotNull
    public PlayerData create(@NotNull final Player player, final long joinTick) {
        final PlayerData data = new PlayerData(player.getUniqueId(), player.getName(), joinTick);
        data.initLatencyTracker();
        dataMap.put(player.getUniqueId(), data);
        return data;
    }

    /**
     * Removes and returns the {@link PlayerData} for the given UUID.
     * Returns {@code null} if no entry exists.
     *
     * @param uuid the player's UUID
     * @return the removed {@link PlayerData}, or {@code null}
     */
    @Nullable
    public PlayerData remove(@NotNull final UUID uuid) {
        return dataMap.remove(uuid);
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    /**
     * Returns the {@link PlayerData} for the given UUID, or {@code null}
     * if the player is not tracked (offline or not yet joined).
     *
     * @param uuid the player's UUID
     * @return {@link PlayerData} or {@code null}
     */
    @Nullable
    public PlayerData get(@NotNull final UUID uuid) {
        return dataMap.get(uuid);
    }

    /**
     * Returns the {@link PlayerData} for the given {@link Player}.
     *
     * @param player the player
     * @return {@link PlayerData} or {@code null}
     */
    @Nullable
    public PlayerData get(@NotNull final Player player) {
        return dataMap.get(player.getUniqueId());
    }

    /**
     * Returns {@code true} if a {@link PlayerData} entry exists for the UUID.
     *
     * @param uuid the player's UUID
     * @return {@code true} if tracked
     */
    public boolean contains(@NotNull final UUID uuid) {
        return dataMap.containsKey(uuid);
    }

    // ─── Bulk ─────────────────────────────────────────────────────────────────

    /**
     * Returns an unmodifiable view of all currently tracked {@link PlayerData} instances.
     *
     * @return unmodifiable collection of all player data
     */
    @NotNull
    public Collection<PlayerData> all() {
        return Collections.unmodifiableCollection(dataMap.values());
    }

    /**
     * Returns the number of currently tracked players.
     *
     * @return tracked player count
     */
    public int size() {
        return dataMap.size();
    }

    // ─── Shutdown ─────────────────────────────────────────────────────────────

    /**
     * Clears all player data entries.
     * Call during plugin disable to release references.
     */
    public void clear() {
        dataMap.clear();
    }
}
