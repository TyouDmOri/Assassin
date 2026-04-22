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
 * {@code /assasin db <status|migrate|backup|query>} — Database management.
 *
 * <p>Permission: {@code assasin.command.db}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class DbSubCommand {

    private final AssasinPlugin plugin;

    public DbSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("db")
            .requires(src -> src.getSender().hasPermission("assasin.command.db"))
            .then(Commands.literal("status").executes(ctx -> {
                final var sender = ctx.getSource().getSender();
                final var storage = plugin.getServiceContainer().getStorageProvider();
                AssasinCommand.sendInfo(sender,
                    "Storage: " + storage.getType() + " — active.");
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("migrate").executes(ctx -> {
                AssasinCommand.sendInfo(ctx.getSource().getSender(),
                    "Migrations are applied automatically on startup.");
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("backup").executes(ctx -> {
                AssasinCommand.sendInfo(ctx.getSource().getSender(),
                    "Backup not yet implemented (FASE 18).");
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("query").executes(ctx -> {
                AssasinCommand.sendInfo(ctx.getSource().getSender(),
                    "Query not yet implemented (FASE 18).");
                return Command.SINGLE_SUCCESS;
            }))
            .build();
    }
}
