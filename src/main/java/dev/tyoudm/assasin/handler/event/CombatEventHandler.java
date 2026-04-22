/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.handler.event;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.CombatTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 * Handles combat-related Bukkit events: damage, death, resurrect (totem),
 * and held-item change (attribute-swap detection).
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CombatEventHandler implements Listener {

    private final AssasinPlugin     plugin;
    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public CombatEventHandler(final AssasinPlugin plugin,
                              final PlayerDataManager dataManager,
                              final CheckProcessor checkProcessor) {
        this.plugin          = plugin;
        this.dataManager     = dataManager;
        this.checkProcessor  = checkProcessor;
    }

    // ─── Damage ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof final Player attacker)) return;

        final PlayerData data = dataManager.get(attacker);
        if (data == null) return;

        final Entity target   = event.getEntity();
        final long   tick     = plugin.getServer().getCurrentTick();
        final float  yaw      = attacker.getLocation().getYaw();
        final float  pitch    = attacker.getLocation().getPitch();
        final double distance = attacker.getLocation().distance(target.getLocation());
        final double motionY  = data.getVelocityY();
        final boolean isCrit  = motionY < 0 && !data.isOnGround() && !data.isElytraActive();

        // Approximate cooldown progress via attack speed attribute
        final float cooldown = attacker.getAttackCooldown();

        final CombatTracker ct = data.getCombatTracker();
        if (ct != null) {
            ct.recordAttack(target, tick, yaw, pitch, distance, motionY, cooldown, isCrit);
        }

        // Update ping from Bukkit API (main thread — safe here)
        data.setPing(attacker.getPing());

        checkProcessor.processCombatEvent(attacker, data, tick);
    }

    // ─── Death ────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(final EntityDeathEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;

        final PlayerData data = dataManager.get(player);
        if (data == null) return;

        final long tick = plugin.getServer().getCurrentTick();
        data.getExemptManager().addPermanent(ExemptType.DEAD);

        if (data.getCombatTracker()     != null) data.getCombatTracker().reset();
        if (data.getAttackTracker()     != null) data.getAttackTracker().reset();
        if (data.getActionTracker()     != null) data.getActionTracker().reset();
        if (data.getMacroStateTracker() != null) data.getMacroStateTracker().reset();
    }

    // ─── Resurrect (totem) ────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(final EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;

        final PlayerData data = dataManager.get(player);
        if (data == null) return;

        if (data.getInventoryTracker() != null) {
            data.getInventoryTracker().recordTotemSwap(System.currentTimeMillis());
        }
    }

    // ─── Held item change (attribute-swap) ────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHeldItemChange(final PlayerItemHeldEvent event) {
        final Player     player = event.getPlayer();
        final PlayerData data   = dataManager.get(player);
        if (data == null) return;

        final long tick = plugin.getServer().getCurrentTick();
        final CombatTracker ct = data.getCombatTracker();

        // Attribute-swap: held change within 3 ticks of last attack
        if (ct != null && (tick - ct.getLastAttackTick()) <= 3) {
            data.getExemptManager().add(ExemptType.ATTRIBUTE_SWAP, tick, 3L);
        }
    }
}
