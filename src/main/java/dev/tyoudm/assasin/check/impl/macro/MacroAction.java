/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.macro;

/**
 * Enumeration of player action types tracked by macro detection checks.
 *
 * <p>Maps to {@link dev.tyoudm.assasin.data.tracker.ActionTracker.Action}
 * but lives in the macro check package for semantic clarity.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public enum MacroAction {

    /** Left-click / attack. */
    CLICK,

    /** Offhand swap (F key). */
    SWAP,

    /** Sneak toggle. */
    CROUCH,

    /** Jump. */
    JUMP,

    /** Right-click / use item. */
    USE_ITEM,

    /** Hotbar slot change (1–9 keys). */
    HOTBAR_KEY,

    /** Inventory window click. */
    WINDOW_CLICK
}
