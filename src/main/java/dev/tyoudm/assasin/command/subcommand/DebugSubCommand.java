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
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * {@code /assasin debug <player> [on|off]} — Toggle debug mode for a player.
 *
 * <p>Permission: {@code assasin.command.debug}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class DebugSubCommand {

    private final AssasinPlugin plugin;

    public DebugSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("debug")
            .requires(src -> src.getSender().hasPermission("assasin.command.debug"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    toggle(ctx.getSource().getSender(),
                        StringArgumentType.getString(ctx, "player"), null);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("on").executes(ctx -> {
                    toggle(ctx.getSource().getSender(),
                        StringArgumentType.getString(ctx, "player"), true);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("off").executes(ctx -> {
                    toggle(ctx.getSource().getSender(),
                        StringArgumentType.getString(ctx, "player"), false);
                    return Command.SINGLE_SUCCESS;
                })))
            .build();
    }

    private void toggle(final org.bukkit.command.CommandSender sender, final String playerName, final Boolean state) {
        final Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            AssasinCommand.sendInfo(sender, "Player '" + playerName + "' not found.");
            return;
        }
        final String stateStr = state == null ? "toggled" : (state ? "enabled" : "disabled");
        AssasinCommand.sendSuccess(sender, "Debug " + stateStr + " for " + playerName + ".");
    }
}
