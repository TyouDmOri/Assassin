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
import dev.tyoudm.assasin.AssasinColors;
import dev.tyoudm.assasin.AssasinPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * {@code /assasin version} — Shows version info.
 *
 * <p>Output: {@code ASSASIN v1.0.0 by TyouDm}
 * Permission: {@code assasin.command.version}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class VersionSubCommand {

    private final AssasinPlugin plugin;

    public VersionSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("version")
            .requires(src -> src.getSender().hasPermission("assasin.command.version"))
            .executes(ctx -> {
                final var sender  = ctx.getSource().getSender();
                final String ver  = plugin.getDescription().getVersion();

                sender.sendMessage(Component.text()
                    .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
                    .append(Component.text("ASSASIN ", AssasinColors.WHITE))
                    .append(Component.text("v" + ver, AssasinColors.SECONDARY, TextDecoration.BOLD))
                    .append(Component.text(" by ", AssasinColors.NEUTRAL))
                    .append(Component.text("TyouDm", AssasinColors.SECONDARY, TextDecoration.BOLD))
                    .build());

                sender.sendMessage(Component.text()
                    .append(Component.text("  Target: ", AssasinColors.NEUTRAL))
                    .append(Component.text("Paper 1.21.11 \"Mounts of Mayhem\"", AssasinColors.WHITE))
                    .build());

                return Command.SINGLE_SUCCESS;
            })
            .build();
    }
}
