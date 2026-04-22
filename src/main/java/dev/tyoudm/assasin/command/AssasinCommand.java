/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tyoudm.assasin.AssasinColors;
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.command.subcommand.AlertsSubCommand;
import dev.tyoudm.assasin.command.subcommand.BanSubCommand;
import dev.tyoudm.assasin.command.subcommand.CheckSubCommand;
import dev.tyoudm.assasin.command.subcommand.DbSubCommand;
import dev.tyoudm.assasin.command.subcommand.DebugSubCommand;
import dev.tyoudm.assasin.command.subcommand.ExemptSubCommand;
import dev.tyoudm.assasin.command.subcommand.GuiSubCommand;
import dev.tyoudm.assasin.command.subcommand.HelpSubCommand;
import dev.tyoudm.assasin.command.subcommand.InfoSubCommand;
import dev.tyoudm.assasin.command.subcommand.KickSubCommand;
import dev.tyoudm.assasin.command.subcommand.LogsSubCommand;
import dev.tyoudm.assasin.command.subcommand.ReloadSubCommand;
import dev.tyoudm.assasin.command.subcommand.ReplaySubCommand;
import dev.tyoudm.assasin.command.subcommand.TestSubCommand;
import dev.tyoudm.assasin.command.subcommand.VersionSubCommand;
import dev.tyoudm.assasin.command.subcommand.VlSubCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Root Brigadier command for ASSASIN: {@code /assasin}.
 *
 * <p>Aliases: {@code /ac}, {@code /anticheat}.
 * Base permission: {@code assasin.command}.
 *
 * <p>Registers all subcommands via Paper's Brigadier API
 * ({@link LifecycleEvents#COMMANDS}).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class AssasinCommand {

    private final AssasinPlugin plugin;

    public AssasinCommand(final AssasinPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    /**
     * Registers the command tree with Paper's lifecycle event manager.
     * Call from {@link AssasinPlugin#onEnable()} or the bootstrap phase.
     */
    public void register() {
        final LifecycleEventManager<Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            final LiteralCommandNode<CommandSourceStack> root = buildRoot();
            commands.register(root, "ASSASIN AntiCheat command", java.util.List.of("ac", "anticheat"));
        });
    }

    // ─── Build ────────────────────────────────────────────────────────────────

    private LiteralCommandNode<CommandSourceStack> buildRoot() {
        return Commands.literal("assasin")
            .requires(src -> src.getSender().hasPermission("assasin.command"))
            .executes(ctx -> {
                // No subcommand → show help
                sendHelp(ctx.getSource().getSender());
                return Command.SINGLE_SUCCESS;
            })
            // ── Subcommands ──────────────────────────────────────────────────
            .then(new GuiSubCommand(plugin).build())
            .then(new AlertsSubCommand(plugin).build())
            .then(new InfoSubCommand(plugin).build())
            .then(new VlSubCommand(plugin).build())
            .then(new LogsSubCommand(plugin).build())
            .then(new ReplaySubCommand(plugin).build())
            .then(new TestSubCommand(plugin).build())
            .then(new CheckSubCommand(plugin).build())
            .then(new ExemptSubCommand(plugin).build())
            .then(new DebugSubCommand(plugin).build())
            .then(new ReloadSubCommand(plugin).build())
            .then(new DbSubCommand(plugin).build())
            .then(new BanSubCommand(plugin).build())
            .then(new KickSubCommand(plugin).build())
            .then(new HelpSubCommand(plugin).build())
            .then(new VersionSubCommand(plugin).build())
            .build();
    }

    // ─── Help ─────────────────────────────────────────────────────────────────

    /**
     * Sends the compact help message to the sender.
     *
     * @param sender the command sender
     */
    public static void sendHelp(final CommandSender sender) {
        sender.sendMessage(Component.text()
            .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
            .append(Component.text("Commands — ", AssasinColors.NEUTRAL))
            .append(Component.text("/assasin help", AssasinColors.SECONDARY)
                .clickEvent(ClickEvent.runCommand("/assasin help"))
                .hoverEvent(HoverEvent.showText(
                    Component.text("Click for full help", AssasinColors.NEUTRAL))))
            .build());
    }

    /**
     * Sends a "no permission" message in blood-red.
     *
     * @param sender the command sender
     */
    public static void sendNoPermission(final CommandSender sender) {
        sender.sendMessage(Component.text()
            .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
            .append(Component.text("✖ No permission.", AssasinColors.FAILURE))
            .build());
    }

    /**
     * Sends a usage error message.
     *
     * @param sender the command sender
     * @param usage  the correct usage string
     */
    public static void sendUsage(final CommandSender sender, final String usage) {
        sender.sendMessage(Component.text()
            .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
            .append(Component.text("Usage: ", AssasinColors.NEUTRAL))
            .append(Component.text(usage, AssasinColors.SECONDARY))
            .build());
    }

    /**
     * Sends a success message.
     *
     * @param sender  the command sender
     * @param message the message
     */
    public static void sendSuccess(final CommandSender sender, final String message) {
        sender.sendMessage(Component.text()
            .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
            .append(Component.text(message, AssasinColors.SUCCESS))
            .build());
    }

    /**
     * Sends an info message.
     *
     * @param sender  the command sender
     * @param message the message
     */
    public static void sendInfo(final CommandSender sender, final String message) {
        sender.sendMessage(Component.text()
            .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
            .append(Component.text(message, AssasinColors.NEUTRAL))
            .build());
    }
}
