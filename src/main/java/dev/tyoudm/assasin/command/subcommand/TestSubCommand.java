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

/**
 * {@code /assasin test <check>} — Manually trigger a check for testing.
 *
 * <p>Permission: {@code assasin.command.test}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class TestSubCommand {

    private final AssasinPlugin plugin;

    public TestSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("test")
            .requires(src -> src.getSender().hasPermission("assasin.command.test"))
            .then(Commands.argument("check", StringArgumentType.word())
                .executes(ctx -> {
                    final String check  = StringArgumentType.getString(ctx, "check");
                    final var    sender = ctx.getSource().getSender();
                    // FASE 18: wire to check registry
                    AssasinCommand.sendInfo(sender, "Test for check '" + check + "' — available in FASE 18.");
                    return Command.SINGLE_SUCCESS;
                }))
            .build();
    }
}
