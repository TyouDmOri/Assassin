package dev.tyoudm.assasin.mitigation;

import dev.tyoudm.assasin.Assassin;
import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MitigationEngine {

    public void handleFlag(Player player, Check check, double vl, String info, long tick) {
    String message = "§8[§cASSASIN§8] §f" + player.getName() + " §7failed §e" + check.getName() + " §8(§6VL: " + String.format("%.1f", vl) + "§8) §7" + info;
    
    // Enviar a los admins que usaron /assasin alerts
    AssassinCommand.getAlertSubscribers().forEach(uuid -> {
        Player admin = Bukkit.getPlayer(uuid);
        if (admin != null) admin.sendMessage(message);
    });
}
}