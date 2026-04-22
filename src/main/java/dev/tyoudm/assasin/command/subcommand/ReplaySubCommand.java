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
 * {@code /assasin replay <player>} — View the replay buffer for a player.
 *
 * <p>Permission: {@code assasin.command.replay}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class ReplaySubCommand {

    private final AssasinPlugin plugin;

    public ReplaySubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("replay")
            .requires(src -> src.getSender().hasPermission("assasin.command.replay"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    final String name   = StringArgumentType.getString(ctx, "player");
                    final var    sender = ctx.getSource().getSender();
                    final Player target = Bukkit.getPlayerExact(name);

                    if (target == null) {
                        AssasinCommand.sendInfo(sender, "Player '" + name + "' not found.");
                        return Command.SINGLE_SUCCESS;
                    }

                    final var data = plugin.getServiceContainer()
                        .getPlayerDataManager().get(target);
                    if (data == null || data.getReplayBuffer() == null) {
                        AssasinCommand.sendInfo(sender, "No replay data for " + name + ".");
                        return Command.SINGLE_SUCCESS;
                    }

                    AssasinCommand.sendInfo(sender,
                        "Replay for " + name + ": " + data.getReplayBuffer().size()
                        + " snapshots (last 200 ticks).");
                    return Command.SINGLE_SUCCESS;
                }))
            .build();
    }
}
