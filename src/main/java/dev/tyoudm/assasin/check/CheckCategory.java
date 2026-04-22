/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check;

/**
 * Top-level category for all ASSASIN checks.
 *
 * <p>Used for GUI grouping, alert formatting, and config namespacing.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public enum CheckCategory {

    /** Movement-based checks (speed, fly, timer, phase, elytra, etc.). */
    MOVEMENT,

    /** Mount-riding checks (mount speed, mount fly, nautilus, etc.). */
    MOUNT,

    /** Combat checks (killaura, aim, reach, autoclicker, velocity, etc.). */
    COMBAT,

    /** World interaction checks (scaffold, fastbreak, nuker, airplace, etc.). */
    WORLD,

    /** Player state checks (badpackets, inventory, autototem, etc.). */
    PLAYER,

    /** Macro / automation detection checks. */
    MACRO,

    /** Miscellaneous checks that don't fit other categories. */
    MISC
}
