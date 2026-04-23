package dev.tyoudm.assasin.exempt;

import java.util.EnumMap;
import java.util.Map;

public class ExemptManager {
    private final Map<ExemptType, Integer> exemptTicks = new EnumMap<>(ExemptType.class);

    // Dar exención por una cantidad de ticks
    public void addExempt(ExemptType type, int ticks) {
        exemptTicks.put(type, ticks);
    }

    // Verificar si el jugador está exento
    public boolean isExempt(ExemptType type, long currentTick) {
        return exemptTicks.getOrDefault(type, 0) > 0;
    }

    // Se llama desde el Main cada tick (20 veces por segundo)
    public void tick() {
        exemptTicks.entrySet().forEach(entry -> {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        });
    }
}