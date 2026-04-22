/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.util;

import dev.tyoudm.assasin.AssasinColors;
import net.kyori.adventure.text.format.TextColor;

/**
 * GUI-specific color aliases that delegate to {@link AssasinColors}.
 *
 * <p>Provides semantic names for GUI contexts (e.g., {@link #ENABLED},
 * {@link #DISABLED}) so GUI code reads clearly without importing
 * {@link AssasinColors} directly.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class GuiColors {

    /** Color for enabled/active state items. */
    public static final TextColor ENABLED  = AssasinColors.SUCCESS;

    /** Color for disabled/inactive state items. */
    public static final TextColor DISABLED = AssasinColors.FAILURE;

    /** Color for border items (glass panes). */
    public static final TextColor BORDER   = AssasinColors.PRIMARY;

    /** Color for navigation items (arrows, back buttons). */
    public static final TextColor NAV      = AssasinColors.NEUTRAL;

    /** Color for admin-only items. */
    public static final TextColor ADMIN    = AssasinColors.SECONDARY;

    /** Color for informational items. */
    public static final TextColor INFO     = AssasinColors.ALERT_LOW;

    /** Color for title/header items. */
    public static final TextColor TITLE    = AssasinColors.WHITE;

    /** Utility class — no instantiation. */
    private GuiColors() {
        throw new UnsupportedOperationException("GuiColors is a utility class.");
    }
}
