/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation;

/**
 * Contract for all ASSASIN mitigation strategies.
 *
 * <p>A strategy encapsulates a single corrective action (e.g., setback,
 * packet cancel, kick) that the {@link MitigationEngine} can apply when
 * a check flags a violation. Strategies are stateless — all mutable state
 * lives in {@link MitigationContext} or {@link dev.tyoudm.assasin.data.PlayerData}.
 *
 * <h2>Execution contract</h2>
 * <ul>
 *   <li>{@link #execute(MitigationContext)} is called from the thread
 *       appropriate for the strategy's {@link #priority()} — see
 *       {@link MitigationPriority} for threading rules.</li>
 *   <li>Strategies must be idempotent: calling execute twice with the
 *       same context must not cause double-punishment.</li>
 *   <li>Strategies must not throw unchecked exceptions — catch internally
 *       and return {@link MitigationResult#failure(String)}.</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public interface MitigationStrategy {

    /**
     * Executes this strategy for the given context.
     *
     * @param context the mitigation context
     * @return the result of execution
     */
    MitigationResult execute(MitigationContext context);

    /**
     * Returns the execution priority of this strategy.
     * Determines the thread and order of execution within a cascade.
     *
     * @return execution priority
     */
    MitigationPriority priority();

    /**
     * Returns a short human-readable name for this strategy (used in logs).
     *
     * @return strategy name
     */
    String name();
}
