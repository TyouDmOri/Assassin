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
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.command.AssasinCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * {@code /assasin reload [config|messages|checks|all]} — Reload configuration.
 *
 * <p>Permission: {@code assasin.command.reload}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class ReloadSubCommand {

    private final AssasinPlugin plugin;

    public ReloadSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("reload")
            .requires(src -> src.getSender().hasPermission("assasin.command.reload"))
            .executes(ctx -> { reload(ctx.getSource().getSender(), "all"); return Command.SINGLE_SUCCESS; })
            .then(Commands.literal("config").executes(ctx -> { reload(ctx.getSource().getSender(), "config"); return Command.SINGLE_SUCCESS; }))
            .then(Commands.literal("messages").executes(ctx -> { reload(ctx.getSource().getSender(), "messages"); return Command.SINGLE_SUCCESS; }))
            .then(Commands.literal("checks").executes(ctx -> { reload(ctx.getSource().getSender(), "checks"); return Command.SINGLE_SUCCESS; }))
            .then(Commands.literal("all").executes(ctx -> { reload(ctx.getSource().getSender(), "all"); return Command.SINGLE_SUCCESS; }))
            .build();
    }

    private void reload(final org.bukkit.command.CommandSender sender, final String target) {
        plugin.reloadConfig();
        AssasinCommand.sendSuccess(sender, "Reloaded: " + target + ".");
    }
}
