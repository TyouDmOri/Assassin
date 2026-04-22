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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * {@code /assasin ban <player> [reason]} — Ban a player.
 *
 * <p>Permission: {@code assasin.command.ban}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class BanSubCommand {

    private final AssasinPlugin plugin;

    public BanSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("ban")
            .requires(src -> src.getSender().hasPermission("assasin.command.ban"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    ban(ctx.getSource().getSender(),
                        StringArgumentType.getString(ctx, "player"), "Banned by ASSASIN");
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ban(ctx.getSource().getSender(),
                            StringArgumentType.getString(ctx, "player"),
                            StringArgumentType.getString(ctx, "reason"));
                        return Command.SINGLE_SUCCESS;
                    })))
            .build();
    }

    private void ban(final org.bukkit.command.CommandSender sender, final String playerName, final String reason) {
        final Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            AssasinCommand.sendInfo(sender, "Player '" + playerName + "' not found.");
            return;
        }
        target.kick(Component.text(reason));
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(playerName, reason, null, "ASSASIN");
        AssasinCommand.sendSuccess(sender, "Banned " + playerName + ": " + reason);
    }
}
