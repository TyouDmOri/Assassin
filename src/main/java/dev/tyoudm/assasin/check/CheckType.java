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
 * Enumeration of all check types in ASSASIN.
 *
 * <p>Each constant maps to exactly one check implementation class.
 * The name follows the convention {@code CATEGORY_VARIANT} where variant
 * is a letter suffix (A, B, C…) distinguishing sub-checks within the same
 * detection family.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public enum CheckType {

    // ─── Movement ─────────────────────────────────────────────────────────────
    SPEED_A, SPEED_B,
    FLY_A, FLY_B,
    NO_FALL_A,
    JESUS_A,
    STEP_A,
    TIMER_A,
    PHASE_A,
    STRAFE_A,
    ELYTRA_A,
    JUMP_RESET_A, JUMP_RESET_B,
    MOTION_A,

    // ─── Mount ────────────────────────────────────────────────────────────────
    MOUNT_SPEED_A,
    NAUTILUS_A,
    ZOMBIE_HORSE_A,
    MOUNT_FLY_A,

    // ─── Combat ───────────────────────────────────────────────────────────────
    KILLAURA_A, KILLAURA_B, KILLAURA_C, KILLAURA_D,
    AIM_A, AIM_B, AIM_C,
    REACH_A, REACH_B,
    HITBOX_A,
    AUTO_CLICKER_A, AUTO_CLICKER_B, AUTO_CLICKER_C,
    VELOCITY_A, VELOCITY_B, VELOCITY_C,
    CRITICALS_A,
    SPEAR_A,
    MACE_DMG_A, MACE_DMG_B, MACE_DMG_C,
    ATTRIBUTE_SWAP_A,

    // ─── World ────────────────────────────────────────────────────────────────
    SCAFFOLD_A, SCAFFOLD_B, SCAFFOLD_C,
    TOWER_A,
    NUKER_A,
    FAST_BREAK_A,
    FAST_PLACE_A,
    LIQUID_WALK_A,
    AIR_PLACE_A,

    // ─── Player ───────────────────────────────────────────────────────────────
    INVENTORY_A, INVENTORY_B,
    BAD_PACKETS_A, BAD_PACKETS_B, BAD_PACKETS_C, BAD_PACKETS_D, BAD_PACKETS_E, BAD_PACKETS_F,
    POST_A,
    CRASH_A,
    BOOK_A,
    TIMER_PACKET_A,
    AUTO_TOTEM_A, AUTO_TOTEM_B, AUTO_TOTEM_C, AUTO_TOTEM_D,
    CHEST_STEALER_A,
    AUTO_ARMOR_A,
    FAST_EAT_A,

    // ─── Macro ────────────────────────────────────────────────────────────────
    MACRO_SEQUENCE_A,
    MACRO_TIMING_A,
    MACRO_VARIANCE_A,
    MACRO_INPUT_A,
    MACRO_INVENTORY_A,
    MACRO_CLICKER_A,
    MACRO_CORRELATION_A,

    // ─── Misc ─────────────────────────────────────────────────────────────────
    MISC_A, MISC_B, MISC_C, MISC_D
}
