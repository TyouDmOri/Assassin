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
import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckRegistry;
import dev.tyoudm.assasin.command.AssasinCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/**
 * {@code /assasin check <name> <enable|disable|info>} — Manage individual checks.
 *
 * <p>Permission: {@code assasin.command.check}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class CheckSubCommand {

    private final AssasinPlugin plugin;

    public CheckSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("check")
            .requires(src -> src.getSender().hasPermission("assasin.command.check"))
            .then(Commands.argument("name", StringArgumentType.word())
                .then(Commands.literal("enable").executes(ctx -> {
                    final String name   = StringArgumentType.getString(ctx, "name");
                    final CommandSender sender = ctx.getSource().getSender();
                    final Check check   = findCheck(name);
                    if (check == null) {
                        AssasinCommand.sendInfo(sender, "Check '" + name + "' not found.");
                        return Command.SINGLE_SUCCESS;
                    }
                    check.setEnabled(true);
                    AssasinCommand.sendSuccess(sender, "Check '" + check.getCheckName() + "' enabled.");
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("disable").executes(ctx -> {
                    final String name   = StringArgumentType.getString(ctx, "name");
                    final CommandSender sender = ctx.getSource().getSender();
                    final Check check   = findCheck(name);
                    if (check == null) {
                        AssasinCommand.sendInfo(sender, "Check '" + name + "' not found.");
                        return Command.SINGLE_SUCCESS;
                    }
                    check.setEnabled(false);
                    AssasinCommand.sendSuccess(sender, "Check '" + check.getCheckName() + "' disabled.");
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("info").executes(ctx -> {
                    final String name   = StringArgumentType.getString(ctx, "name");
                    final CommandSender sender = ctx.getSource().getSender();
                    final Check check   = findCheck(name);
                    if (check == null) {
                        AssasinCommand.sendInfo(sender, "Check '" + name + "' not found.");
                        return Command.SINGLE_SUCCESS;
                    }
                    sender.sendMessage(Component.text()
                        .append(Component.text("§6[ASSASIN] §fCheck: §e" + check.getCheckName()))
                        .append(Component.newline())
                        .append(Component.text("§7Category: §f" + check.getCategory().name()))
                        .append(Component.newline())
                        .append(Component.text("§7Severity: §f" + check.getInfo().severity().name()))
                        .append(Component.newline())
                        .append(Component.text("§7Status:   " + (check.isEnabled() ? "§aEnabled" : "§cDisabled")))
                        .append(Component.newline())
                        .append(Component.text("§7MaxVL:    §f" + check.getInfo().maxVl()))
                        .append(Component.newline())
                        .append(Component.text("§7Desc:     §f" + check.getInfo().description()))
                        .build());
                    return Command.SINGLE_SUCCESS;
                })))
            .then(Commands.literal("list").executes(ctx -> {
                final CommandSender sender = ctx.getSource().getSender();
                final CheckRegistry registry = plugin.getServiceContainer().getCheckProcessor().getRegistry();
                final var checks = registry.getAllChecks();
                sender.sendMessage(Component.text("§6[ASSASIN] §fChecks (" + checks.size() + " total):"));
                for (final Check c : checks) {
                    final String status = c.isEnabled() ? "§a✔" : "§c✘";
                    sender.sendMessage(Component.text(
                        String.format("  %s §f%s §7[%s] VL:%.1f",
                            status, c.getCheckName(),
                            c.getCategory().name(), c.getVl())));
                }
                return Command.SINGLE_SUCCESS;
            }))
            .build();
    }

    private Check findCheck(final String name) {
        final CheckRegistry registry = plugin.getServiceContainer().getCheckProcessor().getRegistry();
        return registry.getAllChecks().stream()
            .filter(c -> c.getCheckName().equalsIgnoreCase(name))
            .findFirst().orElse(null);
    }
}
