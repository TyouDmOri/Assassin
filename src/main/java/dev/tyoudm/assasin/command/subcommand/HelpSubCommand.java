/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tyoudm.assasin.AssasinColors;
import dev.tyoudm.assasin.AssasinPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

/**
 * {@code /assasin help [page]} — Shows the full command reference with hover details.
 *
 * <p>Permission: {@code assasin.command.help}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class HelpSubCommand {

    private record HelpEntry(String syntax, String description, String permission) {}

    private static final HelpEntry[] ENTRIES = {
        new HelpEntry("/assasin gui",                    "Open the main GUI",                    "assasin.command.gui"),
        new HelpEntry("/assasin alerts [on|off]",        "Toggle alert visibility",               "assasin.command.alerts"),
        new HelpEntry("/assasin info <player>",          "View player check data",               "assasin.command.info"),
        new HelpEntry("/assasin vl <player> [check]",   "View/reset violation levels",          "assasin.command.vl"),
        new HelpEntry("/assasin logs <player> [page]",  "View violation logs",                  "assasin.command.logs"),
        new HelpEntry("/assasin replay <player>",        "View replay buffer",                   "assasin.command.replay"),
        new HelpEntry("/assasin test <check>",           "Test a specific check",                "assasin.command.test"),
        new HelpEntry("/assasin check <name> <action>", "Enable/disable/configure a check",     "assasin.command.check"),
        new HelpEntry("/assasin exempt <p> <t> <s>",    "Exempt a player",                      "assasin.command.exempt"),
        new HelpEntry("/assasin debug <player>",         "Toggle debug mode",                    "assasin.command.debug"),
        new HelpEntry("/assasin reload [target]",        "Reload configuration",                 "assasin.command.reload"),
        new HelpEntry("/assasin db <action>",            "Database management",                  "assasin.command.db"),
        new HelpEntry("/assasin ban <player> [reason]", "Ban a player",                         "assasin.command.ban"),
        new HelpEntry("/assasin kick <player> [reason]","Kick a player",                        "assasin.command.kick"),
        new HelpEntry("/assasin version",                "Show version info",                    "assasin.command.version"),
    };

    private final AssasinPlugin plugin;

    public HelpSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("help")
            .requires(src -> src.getSender().hasPermission("assasin.command.help"))
            .executes(ctx -> {
                sendHelp(ctx.getSource().getSender());
                return Command.SINGLE_SUCCESS;
            })
            .build();
    }

    private static void sendHelp(final CommandSender sender) {
        sender.sendMessage(Component.text()
            .append(Component.text("━━━ ", AssasinColors.PRIMARY))
            .append(Component.text("ASSASIN Commands", AssasinColors.WHITE, TextDecoration.BOLD))
            .append(Component.text(" ━━━", AssasinColors.PRIMARY))
            .build());

        for (final HelpEntry entry : ENTRIES) {
            if (!sender.hasPermission(entry.permission())) continue;

            sender.sendMessage(Component.text()
                .append(Component.text("  " + entry.syntax(), AssasinColors.SECONDARY)
                    .clickEvent(ClickEvent.suggestCommand(entry.syntax()))
                    .hoverEvent(HoverEvent.showText(Component.text()
                        .append(Component.text(entry.description() + "\n", AssasinColors.WHITE))
                        .append(Component.text("Permission: ", AssasinColors.NEUTRAL))
                        .append(Component.text(entry.permission(), AssasinColors.ACCENT))
                        .build())))
                .append(Component.text(" — " + entry.description(), AssasinColors.NEUTRAL))
                .build());
        }

        sender.sendMessage(Component.text()
            .append(Component.text("  by ", AssasinColors.NEUTRAL))
            .append(Component.text("TyouDm", AssasinColors.SECONDARY, TextDecoration.BOLD))
            .build());
    }
}
