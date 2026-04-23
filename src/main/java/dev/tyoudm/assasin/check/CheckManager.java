package dev.tyoudm.assasin.check;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckManager {
    // Pool de hilos: un hilo por cada 2 núcleos de tu CPU es lo ideal
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void runMovementChecks(Player player, PlayerData data, long tick) {
        executor.execute(() -> {
            movementChecks.forEach(c -> c.process(player, data, tick));
        });
    }

    public void runCombatChecks(Player player, PlayerData data, long tick) {
        executor.execute(() -> {
            combatChecks.forEach(c -> c.process(player, data, tick));
        });
    }
    
    public void shutdown() { executor.shutdown(); }
}