package dev.tyoudm.assasin.command;

import dev.tyoudm.assasin.AssasinPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AssassinCommand implements CommandExecutor {
    private static final Set<UUID> alertSubscribers = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("assasin.admin")) return true;

        if (args.length > 0 && args[0].equalsIgnoreCase("alerts")) {
            if (alertSubscribers.contains(player.getUniqueId())) {
                alertSubscribers.remove(player.getUniqueId());
                player.sendMessage("§cAlertas desactivadas.");
            } else {
                alertSubscribers.add(player.getUniqueId());
                player.sendMessage("§aAlertas activadas.");
            }
        } else {
            player.sendMessage("§cUso: /assasin alerts");
        }
        return true;
    }

    public static Set<UUID> getAlertSubscribers() { return alertSubscribers; }
}