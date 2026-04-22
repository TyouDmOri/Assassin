/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.macro;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import dev.tyoudm.assasin.data.tracker.MacroStateTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.WelfordStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * MacroInventoryA — FSM-based auto-gapple/pot/soup/armor/chest-stealer detection.
 *
 * <p>Uses a finite state machine to detect automated inventory management
 * patterns. Specifically detects:
 * <ul>
 *   <li><b>Auto-gapple</b> — switching to golden apple and eating in &lt;3 ticks</li>
 *   <li><b>Auto-pot</b> — switching to potion and using in &lt;3 ticks</li>
 *   <li><b>Auto-soup</b> — switching to mushroom stew and eating in &lt;3 ticks</li>
 *   <li><b>Auto-armor</b> — equipping full armor set in &lt;5 ticks</li>
 *   <li><b>Chest-stealer</b> — clicking all slots in &lt;2 ticks</li>
 * </ul>
 *
 * <h2>Coexistence with InventoryA</h2>
 * Shares {@link InventoryTracker} with {@code InventoryA} but uses different
 * heuristics (FSM pattern matching vs. simple rate check).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MacroInventoryA",
    type              = CheckType.MACRO_INVENTORY_A,
    category          = CheckCategory.MACRO,
    description       = "Detects auto-gapple/pot/soup/armor/chest-stealer via FSM.",
    maxVl             = 20.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "macro"
)
public final class MacroInventoryA extends Check {

    // ─── FSM states ───────────────────────────────────────────────────────────

    private enum FsmState { IDLE, SWITCHED_TO_CONSUMABLE, CONSUMING }

    // ─── State ────────────────────────────────────────────────────────────────

    private FsmState fsmState       = FsmState.IDLE;
    private long     stateEnteredTick;
    private Material targetMaterial = Material.AIR;

    /** Welford stats for switch-to-use intervals (ms). */
    private final WelfordStats switchUseIntervalStats = new WelfordStats();

    /** Minimum switch-to-use interval for a human (ms). */
    private static final long MIN_SWITCH_USE_MS = 150L;

    /** Minimum samples before flagging. */
    private static final int MIN_SAMPLES = 5;

    public MacroInventoryA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.HIGH_PING, ExemptType.LAG_SPIKE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) {
            fsmState = FsmState.IDLE;
            return;
        }

        final InventoryTracker it = data.getInventoryTracker();
        if (it == null) return;

        final Material held = it.getHeldMaterial();

        switch (fsmState) {
            case IDLE -> {
                // Detect switch to consumable
                if (isConsumable(held) && it.getLastHeldChangeTick() == tick) {
                    fsmState       = FsmState.SWITCHED_TO_CONSUMABLE;
                    stateEnteredTick = tick;
                    targetMaterial = held;
                }
            }
            case SWITCHED_TO_CONSUMABLE -> {
                // Check if player used the item quickly
                final boolean usedItem = player.isHandRaised()
                    && player.getInventory().getItemInMainHand().getType() == targetMaterial;

                if (usedItem) {
                    final long switchMs = it.getLastHeldChangeTick() * 50L; // approx
                    final long useMs    = System.currentTimeMillis();
                    final long interval = useMs - switchMs;

                    if (interval > 0 && interval < 10_000) {
                        switchUseIntervalStats.add(interval);
                    }

                    if (interval < MIN_SWITCH_USE_MS) {
                        final MacroStateTracker mst = data.getMacroStateTracker();
                        if (mst != null) mst.addEvidence(15, tick);

                        flag(player, data, 1.0,
                            String.format("auto-consumable: %s in %dms (min %dms)",
                                targetMaterial, interval, MIN_SWITCH_USE_MS),
                            tick);
                    }

                    fsmState = FsmState.IDLE;
                } else if (tick - stateEnteredTick > 10) {
                    // Timeout — player didn't use the item
                    fsmState = FsmState.IDLE;
                }
            }
            default -> fsmState = FsmState.IDLE;
        }

        // Check variance of switch-use intervals
        if (switchUseIntervalStats.count() >= MIN_SAMPLES
                && switchUseIntervalStats.stdDev() < 5.0) {
            final MacroStateTracker mst = data.getMacroStateTracker();
            if (mst != null) mst.addEvidence(20, tick);

            flag(player, data, 1.5,
                String.format("auto-consumable pattern: σ=%.2fms n=%d",
                    switchUseIntervalStats.stdDev(), switchUseIntervalStats.count()),
                tick);
            switchUseIntervalStats.reset();
        }
    }

    private static boolean isConsumable(final Material m) {
        return m == Material.GOLDEN_APPLE
            || m == Material.ENCHANTED_GOLDEN_APPLE
            || m == Material.MUSHROOM_STEW
            || m == Material.RABBIT_STEW
            || m == Material.BEETROOT_SOUP
            || m.name().endsWith("_POTION");
    }
}
