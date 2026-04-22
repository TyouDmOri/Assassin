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
 * Execution priority for mitigation strategies within a single cascade.
 *
 * <p>When multiple strategies are triggered by the same violation, they
 * are executed in ascending ordinal order (IMMEDIATE first, DEFERRED last).
 * Strategies with the same priority are executed in registration order.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public enum MitigationPriority {

    /**
     * Execute before the packet is processed (packet-cancel strategies).
     * Only valid for strategies that cancel or modify the triggering packet.
     */
    IMMEDIATE,

    /**
     * Execute synchronously on the main thread in the same tick.
     * Used for setbacks, velocity corrections, and dismounts.
     */
    NORMAL,

    /**
     * Execute after all NORMAL strategies have completed.
     * Used for alerts, resync, and freeze.
     */
    HIGH,

    /**
     * Execute asynchronously (DB writes, Discord webhooks, kick).
     * Must not touch Bukkit API directly — schedule back to main thread if needed.
     */
    DEFERRED
}
