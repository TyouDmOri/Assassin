/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.movement;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.MovementTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * TimerA — Packet timer manipulation detection.
 *
 * <p>Detects when the client sends movement packets faster than the server
 * tick rate allows. Counts movement packets per 20-tick window and flags
 * when the count significantly exceeds 20 (one per tick).
 *
 * <p>State is stored in {@link MovementTracker} (per-player) to avoid
 * the shared-instance problem.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name             = "TimerA",
    type             = CheckType.TIMER_A,
    category         = CheckCategory.MOVEMENT,
    description      = "Detects packet timer manipulation (sending packets too fast).",
    maxVl            = 10.0,
    severity         = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class TimerA extends Check {

    // El balance máximo que permitimos acumular (en milisegundos)
    // 50ms es un tick. Permitimos un margen de 100ms para absorber lag.
    private static final long MAX_BALANCE = -100L;

    public TimerA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.LAG_SPIKE)) return;

        long currentTime = System.currentTimeMillis();
        long lastTime = data.getTimerTracker().getLastPacketTime();
        
        if (lastTime == 0) {
            data.getTimerTracker().setLastPacketTime(currentTime);
            return;
        }

        long diff = currentTime - lastTime;
        double balance = data.getTimerTracker().getBalance();

        // Un paquete "cuesta" 50ms. 
        // Si diff es 40ms, el balance baja 10ms (el cliente va rápido).
        balance += 50.0;
        balance -= diff;

        // Si el servidor laggea, el balance sube mucho. Lo limitamos a 50ms.
        if (balance > 50.0) balance = 50.0;

        if (balance < MAX_BALANCE) {
            flag(player, data, 1.0, String.format("balance=%.2fms limit=%dms", balance, MAX_BALANCE), tick);
            // Resetear balance tras flag para evitar spam
            balance = 0;
        }

        data.getTimerTracker().setLastPacketTime(currentTime);
        data.getTimerTracker().setBalance(balance);
    }
}