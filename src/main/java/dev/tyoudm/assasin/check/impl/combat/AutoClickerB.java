/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.AttackTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.RingBuffer;
import org.bukkit.entity.Player;

/**
 * AutoClickerB — Artificial double-click detection.
 *
 * <p>Detects autoclickers that produce artificial double-clicks by checking
 * for suspiciously short inter-click intervals (< 10ms) that are physically
 * impossible with a real mouse button.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AutoClickerB",
    type              = CheckType.AUTO_CLICKER_B,
    category          = CheckCategory.COMBAT,
    description       = "Detects artificial double-clicks (interval < 10ms).",
    maxVl             = 8.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "combat"
)
public final class AutoClickerB extends Check {

    /** Minimum physically possible click interval (ms). */
    private static final long MIN_INTERVAL_MS = 10L;

    /** Consecutive suspicious intervals required before flagging. */
    private static final int MIN_CONSECUTIVE = 3;

    private int suspiciousCount;

    public AutoClickerB(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.ATTRIBUTE_SWAP,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final AttackTracker at = data.getAttackTracker();
        if (at == null || at.getIntervals().size() < 2) return;

        final RingBuffer.OfLong intervals = at.getIntervals();
        final long lastInterval = intervals.newest();

        if (lastInterval < MIN_INTERVAL_MS) {
            suspiciousCount++;
            if (suspiciousCount >= MIN_CONSECUTIVE) {
                flag(player, data, 2.0,
                    String.format("interval=%dms min=%dms consecutive=%d",
                        lastInterval, MIN_INTERVAL_MS, suspiciousCount),
                    tick);
            }
        } else {
            suspiciousCount = 0;
        }
    }
}
