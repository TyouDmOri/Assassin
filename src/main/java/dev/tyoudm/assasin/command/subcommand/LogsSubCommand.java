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
import dev.tyoudm.assasin.AssasinColors;
import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.command.AssasinCommand;
import dev.tyoudm.assasin.storage.model.ViolationRecord;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * {@code /assasin logs <player> [page]} — View player violation logs.
 *
 * <p>Permission: {@code assasin.command.logs}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class LogsSubCommand {

    private static final int PAGE_SIZE = 10;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM/dd HH:mm:ss");

    private final AssasinPlugin plugin;

    public LogsSubCommand(final AssasinPlugin plugin) { this.plugin = plugin; }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("logs")
            .requires(src -> src.getSender().hasPermission("assasin.command.logs"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    Bukkit.getOnlinePlayers().forEach(p -> builder.suggest(p.getName()));
                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    showLogs(ctx.getSource().getSender(),
                        StringArgumentType.getString(ctx, "player"), 1);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        showLogs(ctx.getSource().getSender(),
                            StringArgumentType.getString(ctx, "player"),
                            IntegerArgumentType.getInteger(ctx, "page"));
                        return Command.SINGLE_SUCCESS;
                    })))
            .build();
    }

    private void showLogs(final CommandSender sender, final String playerName, final int page) {
        // Try online first, then fall back to name lookup
        final Player online = Bukkit.getPlayerExact(playerName);
        final UUID uuid = online != null ? online.getUniqueId() : null;

        if (uuid == null) {
            AssasinCommand.sendInfo(sender, "Player '" + playerName + "' not found (must be online).");
            return;
        }

        plugin.getServiceContainer().getStorageProvider()
            .getViolations(uuid, PAGE_SIZE * page)
            .thenAccept(violations -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    final int start = (page - 1) * PAGE_SIZE;
                    final List<ViolationRecord> page_records = violations.subList(
                        Math.min(start, violations.size()),
                        Math.min(start + PAGE_SIZE, violations.size()));

                    sender.sendMessage(Component.text()
                        .append(Component.text("[ASSASIN] ", AssasinColors.PRIMARY, TextDecoration.BOLD))
                        .append(Component.text("Logs for ", AssasinColors.NEUTRAL))
                        .append(Component.text(playerName, AssasinColors.WHITE, TextDecoration.BOLD))
                        .append(Component.text(" (page " + page + ")", AssasinColors.NEUTRAL))
                        .build());

                    for (final ViolationRecord v : page_records) {
                        final String time = DATE_FMT.format(new Date(v.timestampMs()));
                        sender.sendMessage(Component.text()
                            .append(Component.text("  " + time + " ", AssasinColors.NEUTRAL))
                            .append(Component.text(v.checkName(), AssasinColors.SECONDARY))
                            .append(Component.text(" VL:" + String.format("%.1f", v.violationLevel()),
                                AssasinColors.ALERT_MED))
                            .hoverEvent(HoverEvent.showText(
                                Component.text(v.dataJson(), AssasinColors.NEUTRAL)))
                            .build());
                    }

                    if (page_records.isEmpty()) {
                        AssasinCommand.sendInfo(sender, "No logs found.");
                    }
                });
            });
    }
}
