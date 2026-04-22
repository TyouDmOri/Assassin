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
import dev.tyoudm.assasin.AssasinColors;
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.command.AssasinCommand;
import dev.tyoudm.assasin.data.PlayerData;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /assasin info <player>} — Shows player check data.
 *
 * <p>Permission: {@code assasin.command.info}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class InfoSubCommand {

    private final AssasinPlugin plugin;

    public InfoSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("info")
            .requires(src -> src.getSender().hasPermission("assasin.command.info"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    final String name   = StringArgumentType.getString(ctx, "player");
                    final CommandSender sender = ctx.getSource().getSender();
                    final Player target = Bukkit.getPlayerExact(name);

                    if (target == null) {
                        AssasinCommand.sendInfo(sender, "Player '" + name + "' not found.");
                        return Command.SINGLE_SUCCESS;
                    }

                    final PlayerData data = plugin.getServiceContainer()
                        .getPlayerDataManager().get(target);

                    if (data == null) {
                        AssasinCommand.sendInfo(sender, "No data for " + name + ".");
                        return Command.SINGLE_SUCCESS;
                    }

                    sender.sendMessage(Component.text()
                        .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
                        .append(Component.text("Info for ", AssasinColors.NEUTRAL))
                        .append(Component.text(name, AssasinColors.WHITE, TextDecoration.BOLD))
                        .build());
                    sender.sendMessage(Component.text()
                        .append(Component.text("  Ping: ", AssasinColors.NEUTRAL))
                        .append(Component.text(data.getPing() + "ms", AssasinColors.WHITE))
                        .append(Component.text("  Pos: ", AssasinColors.NEUTRAL))
                        .append(Component.text(String.format("%.1f, %.1f, %.1f",
                            data.getX(), data.getY(), data.getZ()), AssasinColors.WHITE))
                        .build());
                    return Command.SINGLE_SUCCESS;
                }))
            .build();
    }
}
