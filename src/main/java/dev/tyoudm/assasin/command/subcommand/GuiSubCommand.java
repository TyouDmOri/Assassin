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
import org.bukkit.entity.Player;

/**
 * {@code /assasin gui} — Opens the main ASSASIN GUI.
 *
 * <p>Permission: {@code assasin.command.gui}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class GuiSubCommand {

    private final AssasinPlugin plugin;

    public GuiSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("gui")
            .requires(src -> src.getSender().hasPermission("assasin.command.gui"))
            .executes(ctx -> {
                final var sender = ctx.getSource().getSender();
                if (!(sender instanceof final Player player)) {
                    AssasinCommand.sendInfo(sender, "This command is player-only.");
                    return Command.SINGLE_SUCCESS;
                }
                plugin.getServiceContainer().getGuiManager().openMain(player);
                return Command.SINGLE_SUCCESS;
            })
            .build();
    }
}
