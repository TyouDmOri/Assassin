/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.alert;

import dev.tyoudm.assasin.AssasinColors;
import dev.tyoudm.assasin.check.CheckInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * Formats alert messages for all delivery channels using Adventure components.
 *
 * <p>Produces rich MiniMessage-compatible components with:
 * <ul>
 *   <li>Color-coded severity (low/medium/high/critical)</li>
 *   <li>{@link HoverEvent} with full violation details</li>
 *   <li>{@link ClickEvent} to run {@code /assasin info <player>}</li>
 * </ul>
 *
 * <h2>Alert format</h2>
 * <pre>
 *   [ASSASIN] ▶ PlayerName failed SpeedA (VL: 3.50) [details]
 *              ↑ hover: ping, TPS, position, check description
 *              ↑ click: /assasin info PlayerName
 * </pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AlertFormatter {

    /** Utility class — no instantiation. */
    private AlertFormatter() {
        throw new UnsupportedOperationException("AlertFormatter is a utility class.");
    }

    // ─── Chat component ───────────────────────────────────────────────────────

    /**
     * Builds the full chat alert component for the given violation.
     *
     * @param ctx the alert context
     * @return the formatted {@link Component}
     */
    public static Component buildChatAlert(final AlertContext ctx) {
        final TextColor severityColor = severityColor(ctx.severity());

        // Prefix
        final Component prefix = Component.text("[ASSASIN] ", AssasinColors.PRIMARY,
            TextDecoration.BOLD);

        // Arrow separator
        final Component arrow = Component.text("▶ ", AssasinColors.ACCENT);

        // Player name (clickable → /assasin info <player>)
        final Component playerName = Component.text(ctx.playerName(), AssasinColors.WHITE)
            .clickEvent(ClickEvent.runCommand("/assasin info " + ctx.playerName()))
            .hoverEvent(HoverEvent.showText(
                Component.text("Click to view " + ctx.playerName() + "'s info",
                    AssasinColors.NEUTRAL)));

        // " failed "
        final Component failed = Component.text(" failed ", AssasinColors.NEUTRAL);

        // Check name (hoverable → description)
        final Component checkName = Component.text(ctx.checkName(), severityColor,
                TextDecoration.BOLD)
            .hoverEvent(HoverEvent.showText(buildHoverDetails(ctx)));

        // VL
        final Component vl = Component.text(
            String.format(" (VL: %.2f)", ctx.violationLevel()), AssasinColors.NEUTRAL);

        // Details (truncated)
        final String detailsStr = ctx.details().length() > 60
            ? ctx.details().substring(0, 57) + "..."
            : ctx.details();
        final Component details = Component.text(" [" + detailsStr + "]",
            AssasinColors.NEUTRAL);

        return Component.text()
            .append(prefix)
            .append(arrow)
            .append(playerName)
            .append(failed)
            .append(checkName)
            .append(vl)
            .append(details)
            .build();
    }

    /**
     * Builds a compact action-bar alert component.
     *
     * @param ctx the alert context
     * @return the formatted action-bar {@link Component}
     */
    public static Component buildActionBarAlert(final AlertContext ctx) {
        final TextColor color = severityColor(ctx.severity());
        return Component.text()
            .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
            .append(Component.text(ctx.playerName(), AssasinColors.WHITE))
            .append(Component.text(" » ", AssasinColors.ACCENT))
            .append(Component.text(ctx.checkName(), color, TextDecoration.BOLD))
            .append(Component.text(String.format(" VL:%.1f", ctx.violationLevel()),
                AssasinColors.NEUTRAL))
            .build();
    }

    /**
     * Builds a title alert component (main title line).
     *
     * @param ctx the alert context
     * @return the title {@link Component}
     */
    public static Component buildTitleAlert(final AlertContext ctx) {
        return Component.text(ctx.playerName() + " » " + ctx.checkName(),
            severityColor(ctx.severity()), TextDecoration.BOLD);
    }

    /**
     * Builds the subtitle for a title alert.
     *
     * @param ctx the alert context
     * @return the subtitle {@link Component}
     */
    public static Component buildSubtitleAlert(final AlertContext ctx) {
        return Component.text(
            String.format("VL: %.2f | ping: %dms", ctx.violationLevel(), ctx.pingMs()),
            AssasinColors.NEUTRAL);
    }

    // ─── Hover details ────────────────────────────────────────────────────────

    /**
     * Builds the hover tooltip with full violation details.
     *
     * @param ctx the alert context
     * @return the hover {@link Component}
     */
    public static Component buildHoverDetails(final AlertContext ctx) {
        return Component.text()
            .append(Component.text("Check: ", AssasinColors.NEUTRAL))
            .append(Component.text(ctx.checkName(), severityColor(ctx.severity()),
                TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Player: ", AssasinColors.NEUTRAL))
            .append(Component.text(ctx.playerName(), AssasinColors.WHITE))
            .append(Component.newline())
            .append(Component.text("VL: ", AssasinColors.NEUTRAL))
            .append(Component.text(String.format("%.2f", ctx.violationLevel()),
                AssasinColors.ALERT_MED))
            .append(Component.newline())
            .append(Component.text("Ping: ", AssasinColors.NEUTRAL))
            .append(Component.text(ctx.pingMs() + "ms", AssasinColors.WHITE))
            .append(Component.newline())
            .append(Component.text("TPS: ", AssasinColors.NEUTRAL))
            .append(Component.text(String.format("%.1f", ctx.tps()), AssasinColors.WHITE))
            .append(Component.newline())
            .append(Component.text("Pos: ", AssasinColors.NEUTRAL))
            .append(Component.text(
                String.format("%.1f, %.1f, %.1f (%s)",
                    ctx.x(), ctx.y(), ctx.z(), ctx.world()),
                AssasinColors.WHITE))
            .append(Component.newline())
            .append(Component.text("Details: ", AssasinColors.NEUTRAL))
            .append(Component.text(ctx.details(), AssasinColors.NEUTRAL))
            .append(Component.newline())
            .append(Component.text("Click to run /assasin info " + ctx.playerName(),
                AssasinColors.ACCENT))
            .build();
    }

    // ─── Discord plain text ───────────────────────────────────────────────────

    /**
     * Builds a plain-text description for the Discord embed body.
     *
     * @param ctx the alert context
     * @return plain text string
     */
    public static String buildDiscordDescription(final AlertContext ctx) {
        return String.format(
            "**Player:** %s\n**Check:** %s\n**VL:** %.2f\n"
            + "**Ping:** %dms | **TPS:** %.1f\n"
            + "**Position:** %.1f, %.1f, %.1f (%s)\n"
            + "**Details:** %s",
            ctx.playerName(), ctx.checkName(), ctx.violationLevel(),
            ctx.pingMs(), ctx.tps(),
            ctx.x(), ctx.y(), ctx.z(), ctx.world(),
            ctx.details()
        );
    }

    // ─── Severity color ───────────────────────────────────────────────────────

    /**
     * Returns the {@link TextColor} for the given check severity.
     *
     * @param severity the check severity
     * @return the corresponding color
     */
    public static TextColor severityColor(final CheckInfo.Severity severity) {
        return switch (severity) {
            case LOW      -> AssasinColors.ALERT_LOW;
            case MEDIUM   -> AssasinColors.ALERT_MED;
            case HIGH     -> AssasinColors.ALERT_HIGH;
            case CRITICAL -> AssasinColors.SECONDARY;
        };
    }

    /**
     * Returns the Discord embed color integer for the given severity.
     *
     * @param severity the check severity
     * @return Discord embed color (0xRRGGBB)
     */
    public static int discordColor(final CheckInfo.Severity severity) {
        return switch (severity) {
            case LOW      -> 0xFFA500; // amber
            case MEDIUM   -> 0xFF4500; // orange-red
            case HIGH     -> 0xFF0000; // red
            case CRITICAL -> 0x8A0303; // deep blood red (brand)
        };
    }
}
