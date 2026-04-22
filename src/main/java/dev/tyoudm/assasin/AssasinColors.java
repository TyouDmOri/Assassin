/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin;

import net.kyori.adventure.text.format.TextColor;

/**
 * Central color palette for ASSASIN.
 *
 * <p>All UI elements — chat messages, GUI items, alerts, commands — must use
 * colors from this class to maintain visual consistency. Colors are expressed
 * as Adventure {@link TextColor} instances (hex RGB) and as raw hex strings
 * for contexts that require them (e.g., Discord embeds, YAML configs).
 *
 * <h2>Palette overview</h2>
 * <pre>
 *  PRIMARY    — Deep blood red    #8B0000  (main brand color)
 *  SECONDARY  — Bright crimson    #DC143C  (accents, highlights)
 *  ACCENT     — Dark scarlet      #B22222  (mid-tone accents)
 *  ALERT_HIGH — Vivid red         #FF0000  (high-severity alerts)
 *  ALERT_MED  — Orange-red        #FF4500  (medium-severity alerts)
 *  ALERT_LOW  — Amber             #FFA500  (low-severity / info alerts)
 *  SUCCESS    — Muted green       #228B22  (confirmations, toggles ON)
 *  FAILURE    — Bright red        #FF3333  (errors, toggles OFF)
 *  NEUTRAL    — Light gray        #AAAAAA  (secondary text, separators)
 *  WHITE      — Pure white        #FFFFFF  (primary text on dark backgrounds)
 *  DARK       — Near black        #1A1A1A  (backgrounds, shadows)
 * </pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AssasinColors {

    // ─── Brand ────────────────────────────────────────────────────────────────

    /** Deep blood red — primary brand color. Used for borders, headers, prefixes. */
    public static final TextColor PRIMARY    = TextColor.fromHexString("#8B0000");

    /** Bright crimson — secondary accent. Used for highlights and hover text. */
    public static final TextColor SECONDARY  = TextColor.fromHexString("#DC143C");

    /** Dark scarlet — mid-tone accent. Used for sub-headers and decorative lines. */
    public static final TextColor ACCENT     = TextColor.fromHexString("#B22222");

    // ─── Alert severity ───────────────────────────────────────────────────────

    /** Vivid red — high-severity alert color (VL ≥ 15, kick/ban threshold). */
    public static final TextColor ALERT_HIGH = TextColor.fromHexString("#FF0000");

    /** Orange-red — medium-severity alert color (VL 8–14). */
    public static final TextColor ALERT_MED  = TextColor.fromHexString("#FF4500");

    /** Amber — low-severity / informational alert color (VL 1–7). */
    public static final TextColor ALERT_LOW  = TextColor.fromHexString("#FFA500");

    // ─── State ────────────────────────────────────────────────────────────────

    /** Muted forest green — success state. Used for "enabled", "ON", confirmations. */
    public static final TextColor SUCCESS    = TextColor.fromHexString("#228B22");

    /** Bright red — failure / error state. Used for "disabled", "OFF", errors. */
    public static final TextColor FAILURE    = TextColor.fromHexString("#FF3333");

    // ─── Text ─────────────────────────────────────────────────────────────────

    /** Light gray — secondary text, separators, placeholder items. */
    public static final TextColor NEUTRAL    = TextColor.fromHexString("#AAAAAA");

    /** Pure white — primary text on dark backgrounds. */
    public static final TextColor WHITE      = TextColor.fromHexString("#FFFFFF");

    /** Near black — used for backgrounds and shadow effects. */
    public static final TextColor DARK       = TextColor.fromHexString("#1A1A1A");

    // ─── Raw hex strings (for Discord embeds, YAML, etc.) ────────────────────

    /** Raw hex for {@link #PRIMARY} — {@code #8B0000}. */
    public static final String HEX_PRIMARY    = "#8B0000";

    /** Raw hex for {@link #SECONDARY} — {@code #DC143C}. */
    public static final String HEX_SECONDARY  = "#DC143C";

    /** Raw hex for {@link #ACCENT} — {@code #B22222}. */
    public static final String HEX_ACCENT     = "#B22222";

    /** Raw hex for {@link #ALERT_HIGH} — {@code #FF0000}. */
    public static final String HEX_ALERT_HIGH = "#FF0000";

    /** Raw hex for {@link #ALERT_MED} — {@code #FF4500}. */
    public static final String HEX_ALERT_MED  = "#FF4500";

    /** Raw hex for {@link #ALERT_LOW} — {@code #FFA500}. */
    public static final String HEX_ALERT_LOW  = "#FFA500";

    /** Raw hex for {@link #SUCCESS} — {@code #228B22}. */
    public static final String HEX_SUCCESS    = "#228B22";

    /** Raw hex for {@link #FAILURE} — {@code #FF3333}. */
    public static final String HEX_FAILURE    = "#FF3333";

    /** Raw hex for {@link #NEUTRAL} — {@code #AAAAAA}. */
    public static final String HEX_NEUTRAL    = "#AAAAAA";

    /** Discord embed color integer for {@link #PRIMARY} (0x8B0000 = 9109504). */
    public static final int DISCORD_PRIMARY   = 0x8B0000;

    /** Discord embed color integer for {@link #ALERT_HIGH} (0xFF0000 = 16711680). */
    public static final int DISCORD_ALERT     = 0xFF0000;

    // ─── MiniMessage prefix ───────────────────────────────────────────────────

    /**
     * Standard chat prefix used by all ASSASIN messages.
     *
     * <p>Format: {@code <dark_red><bold>[ASSASIN]</bold></dark_red> }
     * followed by the message in the caller's chosen color.
     *
     * <p>Usage example:
     * <pre>{@code
     *   player.sendMessage(Component.text()
     *       .append(AssasinColors.PREFIX_COMPONENT)
     *       .append(Component.text("You have been flagged.", AssasinColors.ALERT_HIGH))
     *       .build());
     * }</pre>
     */
    public static final String PREFIX_MINI = "<color:#8B0000><bold>[ASSASIN]</bold></color> ";

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Utility class — no instantiation. */
    private AssasinColors() {
        throw new UnsupportedOperationException("AssasinColors is a utility class.");
    }
}
