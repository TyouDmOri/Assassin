/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata annotation for ASSASIN check classes.
 *
 * <p>Every class that extends {@link Check} must be annotated with
 * {@code @CheckInfo}. The annotation provides the check's identity,
 * category, severity, and description for use in the GUI, alerts,
 * and configuration.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * \@CheckInfo(
 *     name        = "SpeedA",
 *     type        = CheckType.SPEED_A,
 *     category    = CheckCategory.MOVEMENT,
 *     description = "Detects horizontal speed exceeding vanilla maximum.",
 *     maxVl       = 10.0,
 *     severity    = CheckInfo.Severity.HIGH
 * )
 * public final class SpeedA extends Check { ... }
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {

    /**
     * Severity levels for GUI color-coding and alert routing.
     */
    enum Severity {
        /** Low severity — informational, rarely triggers mitigation. */
        LOW,
        /** Medium severity — triggers soft mitigation. */
        MEDIUM,
        /** High severity — triggers standard mitigation. */
        HIGH,
        /** Critical severity — triggers hard mitigation immediately. */
        CRITICAL
    }

    /** Short display name (e.g., {@code "SpeedA"}). */
    String name();

    /** The {@link CheckType} enum constant for this check. */
    CheckType type();

    /** The {@link CheckCategory} this check belongs to. */
    CheckCategory category();

    /** Human-readable description of what this check detects. */
    String description();

    /** Maximum violation level before the buffer is considered maxed. */
    double maxVl() default 10.0;

    /** Severity level for GUI and alert routing. */
    Severity severity() default Severity.MEDIUM;

    /** Name of the mitigation profile to use (maps to {@link dev.tyoudm.assasin.mitigation.MitigationProfile}). */
    String mitigationProfile() default "medium";
}
