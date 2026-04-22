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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.command.AssasinCommand;
import dev.tyoudm.assasin.storage.model.AlertPreference;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /assasin alerts [on|off]} — Toggles alert visibility for the sender.
 *
 * <p>Permission: {@code assasin.command.alerts}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class AlertsSubCommand {

    private final AssasinPlugin plugin;

    public AlertsSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("alerts")
            .requires(src -> src.getSender().hasPermission("assasin.command.alerts"))
            .executes(ctx -> {
                // No arg → show current state
                showState(ctx.getSource().getSender());
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("on").executes(ctx -> {
                toggle(ctx.getSource().getSender(), true);
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("off").executes(ctx -> {
                toggle(ctx.getSource().getSender(), false);
                return Command.SINGLE_SUCCESS;
            }))
            .build();
    }

    private void toggle(final CommandSender sender, final boolean enabled) {
        if (!(sender instanceof final Player player)) {
            AssasinCommand.sendInfo(sender, "This command is player-only.");
            return;
        }
        plugin.getServiceContainer().getAlertManager()
            .setPreference(player.getUniqueId(), "*", enabled, AlertPreference.DEFAULT_CHANNELS);
        AssasinCommand.sendSuccess(sender,
            "Alerts " + (enabled ? "enabled" : "disabled") + ".");
    }

    private void showState(final CommandSender sender) {
        AssasinCommand.sendInfo(sender, "Use /assasin alerts on|off to toggle alerts.");
    }
}
