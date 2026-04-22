/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.exempt;

import dev.tyoudm.assasin.data.PlayerData;

import java.util.EnumMap;
import java.util.Map;

/**
 * Per-player exemption manager.
 *
 * <p>Tracks active {@link ExemptType} entries with their expiry tick.
 * Each {@link PlayerData} owns one {@code ExemptManager} instance.
 * All methods are called from the packet-processing thread for that player
 * and are therefore not required to be thread-safe.
 *
 * <h2>Tick-based expiry</h2>
 * Exemptions are stored as {@code expiryTick = currentTick + durationTicks}.
 * {@link #isExempt(ExemptType, long)} returns {@code true} while
 * {@code currentTick <= expiryTick}.
 *
 * <h2>Permanent exemptions</h2>
 * Pass {@link #PERMANENT} as the duration to create an exemption that never
 * expires. Remove it explicitly with {@link #clear(ExemptType)}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ExemptManager {

    /** Sentinel value for exemptions that never expire. */
    public static final long PERMANENT = Long.MAX_VALUE;

    /** Maps each active ExemptType to its expiry tick. */
    private final Map<ExemptType, Long> exemptions = new EnumMap<>(ExemptType.class);

    // ─── Add ──────────────────────────────────────────────────────────────────

    /**
     * Adds or refreshes an exemption for {@code durationTicks} ticks.
     *
     * <p>If the exemption already exists, the expiry is updated to
     * {@code max(existing, currentTick + durationTicks)}.
     *
     * @param type          the exemption type to add
     * @param currentTick   the current server tick
     * @param durationTicks how many ticks the exemption should last (use
     *                      {@link #PERMANENT} for indefinite)
     */
    public void add(final ExemptType type, final long currentTick, final long durationTicks) {
        final long expiry = durationTicks == PERMANENT
                ? PERMANENT
                : currentTick + durationTicks;
        exemptions.merge(type, expiry, Math::max);
    }

    /**
     * Adds a permanent exemption (never expires until explicitly cleared).
     *
     * @param type the exemption type to add permanently
     */
    public void addPermanent(final ExemptType type) {
        exemptions.put(type, PERMANENT);
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the given exemption is currently active.
     *
     * @param type        the exemption type to check
     * @param currentTick the current server tick
     * @return {@code true} if exempt
     */
    public boolean isExempt(final ExemptType type, final long currentTick) {
        final Long expiry = exemptions.get(type);
        if (expiry == null) return false;
        if (expiry == PERMANENT) return true;
        if (currentTick <= expiry) return true;
        // Lazily remove expired entry
        exemptions.remove(type);
        return false;
    }

    /**
     * Returns {@code true} if <em>any</em> of the given exemption types is active.
     *
     * @param currentTick the current server tick
     * @param types       the exemption types to check
     * @return {@code true} if at least one is active
     */
    public boolean isExemptAny(final long currentTick, final ExemptType... types) {
        for (final ExemptType type : types) {
            if (isExempt(type, currentTick)) return true;
        }
        return false;
    }

    // ─── Remove ───────────────────────────────────────────────────────────────

    /**
     * Removes an exemption immediately, regardless of its remaining duration.
     *
     * @param type the exemption type to remove
     */
    public void clear(final ExemptType type) {
        exemptions.remove(type);
    }

    /**
     * Removes all active exemptions.
     * Call on player death, respawn, or full reset.
     */
    public void clearAll() {
        exemptions.clear();
    }

    /**
     * Removes all exemptions that have expired as of {@code currentTick}.
     * Call once per tick to keep the map clean.
     *
     * @param currentTick the current server tick
     */
    public void purgeExpired(final long currentTick) {
        exemptions.entrySet().removeIf(e ->
            e.getValue() != PERMANENT && currentTick > e.getValue()
        );
    }

    // ─── Introspection ────────────────────────────────────────────────────────

    /**
     * Returns the number of currently active exemptions.
     *
     * @return active exemption count
     */
    public int activeCount() {
        return exemptions.size();
    }

    /**
     * Returns {@code true} if no exemptions are currently active.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return exemptions.isEmpty();
    }
}
