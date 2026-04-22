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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.command.AssasinCommand;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * {@code /assasin exempt <player> <type> <seconds>} — Exempt a player.
 *
 * <p>Permission: {@code assasin.command.exempt}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class ExemptSubCommand {

    private final AssasinPlugin plugin;

    public ExemptSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("exempt")
            .requires(src -> src.getSender().hasPermission("assasin.command.exempt"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                .then(Commands.argument("type", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (final ExemptType t : ExemptType.values()) builder.suggest(t.name());
                        return builder.buildFuture();
                    })
                    .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 3600))
                        .executes(ctx -> {
                            final String playerName = StringArgumentType.getString(ctx, "player");
                            final String typeName   = StringArgumentType.getString(ctx, "type");
                            final int    seconds    = IntegerArgumentType.getInteger(ctx, "seconds");
                            final var    sender     = ctx.getSource().getSender();

                            final Player target = Bukkit.getPlayerExact(playerName);
                            if (target == null) {
                                AssasinCommand.sendInfo(sender, "Player '" + playerName + "' not found.");
                                return Command.SINGLE_SUCCESS;
                            }

                            ExemptType type;
                            try {
                                type = ExemptType.valueOf(typeName.toUpperCase());
                            } catch (final IllegalArgumentException ex) {
                                AssasinCommand.sendInfo(sender, "Unknown exempt type: " + typeName);
                                return Command.SINGLE_SUCCESS;
                            }

                            final PlayerData data = plugin.getServiceContainer()
                                .getPlayerDataManager().get(target);
                            if (data == null) {
                                AssasinCommand.sendInfo(sender, "No data for " + playerName + ".");
                                return Command.SINGLE_SUCCESS;
                            }

                            final long tick = plugin.getServer().getCurrentTick();
                            data.getExemptManager().add(type, tick, seconds * 20L);
                            AssasinCommand.sendSuccess(sender,
                                playerName + " exempted from " + type.name()
                                + " for " + seconds + "s.");
                            return Command.SINGLE_SUCCESS;
                        }))))
            .build();
    }
}
