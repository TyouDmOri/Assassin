package dev.tyoudm.assasin.check;

import dev.tyoudm.assasin.check.impl.combat.*;
import dev.tyoudm.assasin.check.impl.movement.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CheckManager {
    private final List<Check> movementChecks = new ArrayList<>();
    private final List<Check> combatChecks = new ArrayList<>();

    public CheckManager(MitigationEngine engine) {
        // Registro de checks
        movementChecks.add(new SpeedB(engine));
        // movementChecks.add(new MotionA(engine)); // Si ya lo creaste
        
        combatChecks.add(new ReachA(engine));
    }

    public void runMovementChecks(Player player, PlayerData data, long tick) {
        movementChecks.forEach(c -> c.process(player, data, tick));
    }

    public void runCombatChecks(Player player, PlayerData data, long tick) {
        combatChecks.forEach(c -> c.process(player, data, tick));
    }
}