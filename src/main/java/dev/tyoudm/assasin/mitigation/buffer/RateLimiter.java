/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation.buffer;

/**
 * Token-bucket rate limiter for mitigation actions.
 *
 * <p>Prevents a single check from triggering the same mitigation action
 * too frequently (e.g., spamming setbacks or kicks). Uses a token-bucket
 * model: tokens refill at a fixed rate and are consumed on each action.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   // Allow at most 1 setback per 10 ticks
 *   RateLimiter limiter = new RateLimiter(1.0, 0.1, 1.0);
 *
 *   if (limiter.tryConsume(currentTick)) {
 *       // execute setback
 *   }
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class RateLimiter {

    // ─── State ────────────────────────────────────────────────────────────────

    /** Maximum number of tokens in the bucket. */
    private final double maxTokens;

    /** Token refill rate per tick. */
    private final double refillRate;

    /** Cost of one action in tokens. */
    private final double cost;

    /** Current token count. */
    private double tokens;

    /** Server tick of the last refill. */
    private long lastRefillTick;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new rate limiter.
     *
     * @param maxTokens  maximum bucket capacity (e.g., 3.0 for burst of 3)
     * @param refillRate tokens added per tick (e.g., 0.1 = 1 token per 10 ticks)
     * @param cost       tokens consumed per action (usually 1.0)
     */
    public RateLimiter(final double maxTokens, final double refillRate, final double cost) {
        this.maxTokens  = maxTokens;
        this.refillRate = refillRate;
        this.cost       = cost;
        this.tokens     = maxTokens; // start full
    }

    // ─── API ──────────────────────────────────────────────────────────────────

    /**
     * Attempts to consume {@link #cost} tokens.
     *
     * <p>First refills tokens based on elapsed ticks since last refill,
     * then checks if enough tokens are available.
     *
     * @param currentTick the current server tick
     * @return {@code true} if the action is allowed (tokens consumed),
     *         {@code false} if rate-limited
     */
    public boolean tryConsume(final long currentTick) {
        refill(currentTick);
        if (tokens >= cost) {
            tokens -= cost;
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if an action would be allowed right now,
     * without consuming any tokens.
     *
     * @param currentTick the current server tick
     * @return {@code true} if tokens ≥ cost
     */
    public boolean canConsume(final long currentTick) {
        refill(currentTick);
        return tokens >= cost;
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void refill(final long currentTick) {
        if (lastRefillTick == 0L) {
            lastRefillTick = currentTick;
            return;
        }
        final long elapsed = currentTick - lastRefillTick;
        if (elapsed > 0) {
            tokens = Math.min(maxTokens, tokens + elapsed * refillRate);
            lastRefillTick = currentTick;
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns the current token count. */
    public double getTokens()    { return tokens; }

    /** Returns the maximum token capacity. */
    public double getMaxTokens() { return maxTokens; }

    /** Resets the bucket to full capacity. */
    public void reset()          { tokens = maxTokens; lastRefillTick = 0L; }
}
