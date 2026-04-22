/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

import dev.tyoudm.assasin.util.RingBuffer;

/**
 * Central action ring buffer for macro detection.
 *
 * <p>Records the last 64 player actions as {@link Action} entries in a
 * fixed-size ring buffer. Used by {@code MacroSequenceA} (Rabin-Karp
 * n-gram hashing), {@code MacroTimingA}, {@code MacroVarianceA}, and
 * {@code MacroInputA}.
 *
 * <h2>Action encoding</h2>
 * Each action is encoded as a {@code long} for zero-autoboxing storage:
 * <pre>
 *   bits 63–32 : system time ms (lower 32 bits)
 *   bits 31–16 : server tick (lower 16 bits)
 *   bits 15– 8 : action type ordinal
 *   bits  7– 0 : action subtype / slot
 * </pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ActionTracker {

    /** Capacity of the action ring buffer. */
    public static final int CAPACITY = 64;

    // ─── Action type enum ─────────────────────────────────────────────────────

    /**
     * Enumeration of trackable player action types.
     */
    public enum Action {
        CLICK,
        SWAP,
        CROUCH,
        JUMP,
        USE_ITEM,
        HOTBAR_KEY,
        WINDOW_CLICK
    }

    // ─── Storage ──────────────────────────────────────────────────────────────

    /** Ring buffer of encoded action longs. */
    private final RingBuffer.OfLong actions = new RingBuffer.OfLong(CAPACITY);

    /** Number of actions recorded in the current tick. */
    private int actionsThisTick;

    /** Server tick of the last recorded action. */
    private long lastActionTick;

    // ─── Encoding ─────────────────────────────────────────────────────────────

    /**
     * Encodes an action into a single {@code long}.
     *
     * @param nowMs       current system time in ms
     * @param currentTick current server tick
     * @param action      action type
     * @param subtype     action subtype or slot (0 if not applicable)
     * @return encoded action long
     */
    public static long encode(final long nowMs, final long currentTick,
                              final Action action, final int subtype) {
        return ((nowMs & 0xFFFFFFFFL) << 32)
             | ((currentTick & 0xFFFFL) << 16)
             | ((action.ordinal() & 0xFF) << 8)
             | (subtype & 0xFF);
    }

    /** Extracts the system time (lower 32 bits of ms) from an encoded action. */
    public static long decodeMs(final long encoded) {
        return (encoded >>> 32) & 0xFFFFFFFFL;
    }

    /** Extracts the server tick (lower 16 bits) from an encoded action. */
    public static int decodeTick(final long encoded) {
        return (int) ((encoded >>> 16) & 0xFFFFL);
    }

    /** Extracts the action type ordinal from an encoded action. */
    public static int decodeActionOrdinal(final long encoded) {
        return (int) ((encoded >>> 8) & 0xFF);
    }

    /** Extracts the subtype/slot from an encoded action. */
    public static int decodeSubtype(final long encoded) {
        return (int) (encoded & 0xFF);
    }

    // ─── Record ───────────────────────────────────────────────────────────────

    /**
     * Records a new action.
     *
     * @param nowMs       current system time in ms
     * @param currentTick current server tick
     * @param action      action type
     * @param subtype     action subtype or slot (0 if not applicable)
     */
    public void record(final long nowMs, final long currentTick,
                       final Action action, final int subtype) {
        actions.add(encode(nowMs, currentTick, action, subtype));
        if (currentTick == lastActionTick) {
            actionsThisTick++;
        } else {
            actionsThisTick = 1;
            lastActionTick  = currentTick;
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns the raw action ring buffer. */
    public RingBuffer.OfLong getActions()    { return actions; }

    /** Returns the number of actions recorded in the current tick. */
    public int getActionsThisTick()          { return actionsThisTick; }

    /** Returns the server tick of the last recorded action. */
    public long getLastActionTick()          { return lastActionTick; }

    /** Resets all state. Call on death or respawn. */
    public void reset() {
        actions.clear();
        actionsThisTick = 0;
        lastActionTick  = 0L;
    }
}
