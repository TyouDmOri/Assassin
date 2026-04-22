package dev.tyoudm.assasin.mitigation;
import dev.tyoudm.assasin.Assassin;
import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
public class MitigationEngine {
    public void handleFlag(Player player, Check check, double vl, String info, long tick) {
        Bukkit.broadcastMessage("Â§8[Â§cAssassinÂ§8] Â§7" + player.getName() + " flagged Â§c" + check.getName() + " Â§8[Â§f" + info + "Â§8]");
        if (check.getCategory().name().equals("MOVEMENT") && vl > 3) {
            PlayerData data = Assassin.getInstance().getDataManager().getData(player);
            Bukkit.getScheduler().runTask(Assassin.getInstance(), () -> player.teleport(data.getMovementTracker().getLastValidLocation()));
        }
    }
}
