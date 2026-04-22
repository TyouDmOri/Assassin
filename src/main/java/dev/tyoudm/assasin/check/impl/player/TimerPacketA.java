/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.player;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * TimerPacketA — Overall packet rate check.
 *
 * <p>Detects when the player sends packets at a rate significantly higher
 * than the server tick rate. Complements {@code TimerA} (movement-specific)
 * by checking the total packet count across all packet types.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "TimerPacketA",
    type              = CheckType.TIMER_PACKET_A,
    category          = CheckCategory.PLAYER,
    description       = "Detects overall packet rate violation.",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "medium"
)
public final class TimerPacketA extends Check {

    /** Maximum allowed packets per second across all types. */
    private static final int MAX_PACKETS_PER_SECOND = 500;

    /** Window size in ticks (1 second = 20 ticks). */
    private static final int WINDOW_TICKS = 20;

    private long lastWindowTick;
    private long lastPacketCount;

    public TimerPacketA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.LAG_SPIKE)) return;

        if (lastWindowTick == 0) {
            lastWindowTick  = tick;
            lastPacketCount = data.getPacketCount();
            return;
        }

        final long elapsed = tick - lastWindowTick;
        if (elapsed < WINDOW_TICKS) return;

        final long packets = data.getPacketCount() - lastPacketCount;
        final double rate  = packets * 20.0 / elapsed; // normalize to per-second

        if (rate > MAX_PACKETS_PER_SECOND) {
            flag(player, data, (rate - MAX_PACKETS_PER_SECOND) / 100.0,
                String.format("packet rate: %.0f/s max=%d", rate, MAX_PACKETS_PER_SECOND),
                tick);
        }

        lastWindowTick  = tick;
        lastPacketCount = data.getPacketCount();
    }
}
