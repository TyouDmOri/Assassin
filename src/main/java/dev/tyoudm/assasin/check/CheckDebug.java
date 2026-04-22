/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check;

import java.util.logging.Logger;

/**
 * Static debug logger for check flags.
 *
 * <p>When {@code debug: true} is set in config.yml, every flag call logs
 * a detailed line to the console so false positives can be diagnosed.
 *
 * <p>Format:
 * <pre>
 *   [ASSASIN DEBUG] [CheckName] Player | details | VL: x.xx (+points)
 * </pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CheckDebug {

    private static volatile boolean enabled = true; // default on
    private static volatile Logger  logger  = Logger.getLogger("ASSASIN");

    private CheckDebug() {}

    /**
     * Initializes the debug logger. Called from {@code ServiceContainer}.
     *
     * @param pluginLogger the plugin logger
     * @param debugEnabled whether debug mode is enabled
     */
    public static void init(final Logger pluginLogger, final boolean debugEnabled) {
        logger  = pluginLogger;
        enabled = debugEnabled;
    }

    /** Returns {@code true} if debug logging is active. */
    public static boolean isEnabled() { return enabled; }

    /** Enables or disables debug logging at runtime. */
    public static void setEnabled(final boolean value) { enabled = value; }

    /**
     * Logs a flag event to the console if debug is enabled.
     *
     * @param checkName  the check that flagged
     * @param playerName the player name
     * @param details    the flag details string
     * @param newVl      the new violation level after flagging
     * @param points     the points added this flag
     */
    public static void logFlag(final String checkName, final String playerName,
                               final String details, final double newVl, final double points) {
        if (!enabled) return;
        logger.info(String.format(
            "[DEBUG] [%s] %s | %s | VL: %.2f (+%.2f)",
            checkName, playerName, details, newVl, points));
    }
}
