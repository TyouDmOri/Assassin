package dev.tyoudm.assasin.check.impl.movement;

import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

@CheckInfo(
    name = "TimerA",
    type = CheckType.TIMER_A,
    category = CheckCategory.MOVEMENT,
    description = "Balance-based packet timer detection.",
    maxVl = 10.0,
    severity = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class TimerA extends Check {
    private long lastTime = -1L;
    private double balance = 0.0;

    public TimerA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.JOINED)) return;

        long now = System.currentTimeMillis();
        if (lastTime == -1L) {
            lastTime = now;
            return;
        }

        long diff = now - lastTime;
        balance += 50.0; // Añadimos 1 tick (50ms)
        balance -= diff; // Restamos el tiempo real pasado

        // Limitar balance positivo para evitar bypass después de lag masivo
        if (balance > 100.0) balance = 100.0;

        if (balance < -100.0) { // Si el jugador ha ganado más de 2 ticks de ventaja
            flag(player, data, 1.0, "balance=" + balance, tick);
            balance = 0; // Reset
        }
        lastTime = now;
    }
}