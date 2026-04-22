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
import dev.tyoudm.assasin.data.tracker.CombatTracker;
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

/**
 * AttributeSwapA — Attribute-swap validator (informer check).
 *
 * <p>This is not a flagging check — it's a <em>validator</em> that detects
 * legitimate attribute-swap (held-item change between attacks) and sets the
 * {@link ExemptType#ATTRIBUTE_SWAP} exemption to inform other checks
 * ({@link VelocityA}, {@link MaceDmgA}, {@link AutoClickerA}) to re-baseline.
 *
 * <h2>Attribute-swap pattern</h2>
 * Player changes held item within 3 ticks of an attack. This is a legitimate
 * PvP technique (axe/sword/mace combo) and must NOT cause false positives.
 *
 * <p>This check never calls {@link #flag} — it only manages exemptions.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "AttributeSwapA",
    type              = CheckType.ATTRIBUTE_SWAP_A,
    category          = CheckCategory.COMBAT,
    description       = "Detects attribute-swap and informs VelocityA/MaceDmgA/AutoClickerA.",
    maxVl             = 1.0, // never actually flags
    severity          = CheckInfo.Severity.LOW,
    mitigationProfile = "soft"
)
public final class AttributeSwapA extends Check {

    /** Ticks within which a held-item change after an attack is considered attribute-swap. */
    private static final int SWAP_WINDOW_TICKS = 3;

    /** Duration of the ATTRIBUTE_SWAP exempt (ticks). */
    private static final int EXEMPT_DURATION_TICKS = 3;

    public AttributeSwapA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        final CombatTracker   ct = data.getCombatTracker();
        final InventoryTracker it = data.getInventoryTracker();
        if (ct == null || it == null) return;

        final long lastAttack    = ct.getLastAttackTick();
        final long lastHeldChange = it.getLastHeldChangeTick();

        // Attribute-swap: held change within SWAP_WINDOW_TICKS of last attack
        if (lastAttack > 0 && Math.abs(tick - lastAttack) <= SWAP_WINDOW_TICKS
                && Math.abs(lastHeldChange - lastAttack) <= SWAP_WINDOW_TICKS) {
            data.getExemptManager().add(ExemptType.ATTRIBUTE_SWAP, tick, EXEMPT_DURATION_TICKS);
        }
    }
}
