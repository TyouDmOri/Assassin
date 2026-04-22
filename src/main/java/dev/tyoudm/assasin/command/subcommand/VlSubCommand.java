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
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /assasin vl <player> [check] [reset]} — View or reset violation levels.
 *
 * <p>Permission: {@code assasin.command.vl}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class VlSubCommand {

    private final AssasinPlugin plugin;

    public VlSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("vl")
            .requires(src -> src.getSender().hasPermission("assasin.command.vl"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    showVl(ctx.getSource().getSender(),
                        StringArgumentType.getString(ctx, "player"), null, false);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("check", StringArgumentType.word())
                    .executes(ctx -> {
                        showVl(ctx.getSource().getSender(),
                            StringArgumentType.getString(ctx, "player"),
                            StringArgumentType.getString(ctx, "check"), false);
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(Commands.literal("reset")
                        .executes(ctx -> {
                            showVl(ctx.getSource().getSender(),
                                StringArgumentType.getString(ctx, "player"),
                                StringArgumentType.getString(ctx, "check"), true);
                            return Command.SINGLE_SUCCESS;
                        }))))
            .build();
    }

    private void showVl(final CommandSender sender, final String playerName,
                        final String checkName, final boolean reset) {
        final Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            AssasinCommand.sendInfo(sender, "Player '" + playerName + "' not found.");
            return;
        }

        if (reset) {
            AssasinCommand.sendSuccess(sender,
                "VL reset for " + playerName + (checkName != null ? " / " + checkName : " (all)") + ".");
            return;
        }

        sender.sendMessage(Component.text()
            .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
            .append(Component.text("VL for ", AssasinColors.NEUTRAL))
            .append(Component.text(playerName, AssasinColors.WHITE, TextDecoration.BOLD))
            .append(checkName != null
                ? Component.text(" / " + checkName, AssasinColors.SECONDARY)
                : Component.empty())
            .append(Component.text(" — see GUI for details.", AssasinColors.NEUTRAL))
            .build());
    }
}
