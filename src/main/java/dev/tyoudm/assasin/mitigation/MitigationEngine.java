package dev.tyoudm.assasin.mitigation;

import dev.tyoudm.assasin.Assassin;
import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MitigationEngine {

    public void handleFlag(Player player, Check check, double vl, String info, long tick) {
        // 1. Alerta por consola y chat (puedes añadir permiso para staff aquí)
        String alert = "§8[§cAssassin§8] §f" + player.getName() + " §7flagged §e" + 
                       check.getName() + " §8[§7" + info + "§8]";
        
        Bukkit.getConsoleSender().sendMessage(alert);
        // Bukkit.broadcast(alert, "assasin.alerts"); 

        // 2. Sistema de Setback (Teletransportar atrás si es movimiento)
        if (check.getCategory().name().equals("MOVEMENT")) {
            PlayerData data = Assassin.getInstance().getDataManager().getData(player);
            
            // Si el jugador ya ha fallado varias veces, lo forzamos a volver a su última posición legal
            if (data.getCheckData().getSpeedBBuffer() > 4 || data.getCheckData().getMotionABuffer() > 4) {
                Bukkit.getScheduler().runTask(Assassin.getInstance(), () -> {
                    player.teleport(data.getMovementTracker().getLastValidLocation());
                });
            }
        }
    }
}