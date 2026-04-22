/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

/**
 * Per-player finite state machine (FSM) for macro detection.
 *
 * <p>Tracks the macro detection lifecycle from initial observation through
 * confirmation and mitigation. The FSM state is consumed by all macro
 * checks ({@code MacroSequenceA}, {@code MacroTimingA}, etc.) to coordinate
 * evidence accumulation and avoid redundant processing.
 *
 * <h2>State transitions</h2>
 * <pre>
 *   IDLE ──(suspicion)──► DETECTING ──(confirmed)──► CONFIRMED
 *     ▲                       │                          │
 *     └──(reset/exempt)───────┘◄─────(reset/exempt)──────┘
 *
 *   Any state ──(exempt condition)──► EXEMPT ──(condition cleared)──► IDLE
 * </pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MacroStateTracker {

    // ─── State enum ───────────────────────────────────────────────────────────

    /**
     * FSM states for macro detection.
     */
    public enum MacroState {
        /** No suspicion — normal processing. */
        IDLE,
        /** Preliminary suspicion — accumulating evidence. */
        DETECTING,
        /** Evidence threshold met — mitigation active. */
        CONFIRMED,
        /** Player is exempt (high ping, lag spike, whitelist, etc.). */
        EXEMPT
    }

    // ─── State ────────────────────────────────────────────────────────────────

    /** Current FSM state. */
    private MacroState state = MacroState.IDLE;

    /** Server tick when the current state was entered. */
    private long stateEnteredTick;

    /** Accumulated evidence score (0–100). */
    private int evidenceScore;

    /** Number of consecutive ticks in DETECTING state. */
    private int detectingTicks;

    /** Number of times CONFIRMED state has been reached. */
    private int confirmationCount;

    // ─── Transitions ──────────────────────────────────────────────────────────

    /**
     * Transitions to a new state.
     *
     * @param newState    the target state
     * @param currentTick current server tick
     */
    public void transition(final MacroState newState, final long currentTick) {
        if (newState == state) return;
        state             = newState;
        stateEnteredTick  = currentTick;
        if (newState == MacroState.DETECTING) {
            detectingTicks = 0;
        } else if (newState == MacroState.CONFIRMED) {
            confirmationCount++;
        } else if (newState == MacroState.IDLE || newState == MacroState.EXEMPT) {
            evidenceScore  = 0;
            detectingTicks = 0;
        }
    }

    /**
     * Adds evidence to the accumulator. If the score reaches 100,
     * the FSM automatically transitions to DETECTING (if IDLE) or
     * CONFIRMED (if already DETECTING).
     *
     * @param points      evidence points to add (1–100)
     * @param currentTick current server tick
     */
    public void addEvidence(final int points, final long currentTick) {
        evidenceScore = Math.min(100, evidenceScore + points);
        if (state == MacroState.IDLE && evidenceScore >= 30) {
            transition(MacroState.DETECTING, currentTick);
        } else if (state == MacroState.DETECTING) {
            detectingTicks++;
            if (evidenceScore >= 80) {
                transition(MacroState.CONFIRMED, currentTick);
            }
        }
    }

    /**
     * Decays evidence score by {@code points} per tick.
     * Call once per tick when no suspicion is active.
     *
     * @param points decay amount
     */
    public void decayEvidence(final int points) {
        evidenceScore = Math.max(0, evidenceScore - points);
        if (evidenceScore == 0 && state == MacroState.DETECTING) {
            state = MacroState.IDLE;
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public MacroState getMacroState()        { return state; }
    public long       getStateEnteredTick()  { return stateEnteredTick; }
    public int        getEvidenceScore()     { return evidenceScore; }
    public int        getDetectingTicks()    { return detectingTicks; }
    public int        getConfirmationCount() { return confirmationCount; }

    public boolean isIdle()       { return state == MacroState.IDLE; }
    public boolean isDetecting()  { return state == MacroState.DETECTING; }
    public boolean isConfirmed()  { return state == MacroState.CONFIRMED; }
    public boolean isExempt()     { return state == MacroState.EXEMPT; }

    /** Resets all state. Call on death, respawn, or explicit reset. */
    public void reset() {
        state             = MacroState.IDLE;
        stateEnteredTick  = 0L;
        evidenceScore     = 0;
        detectingTicks    = 0;
        confirmationCount = 0;
    }
}
