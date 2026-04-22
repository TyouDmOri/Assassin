/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation.strategy;

import dev.tyoudm.assasin.mitigation.MitigationContext;
import dev.tyoudm.assasin.mitigation.MitigationPriority;
import dev.tyoudm.assasin.mitigation.MitigationResult;
import dev.tyoudm.assasin.mitigation.MitigationStrategy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Forces a client-server inventory resynchronization.
 *
 * <p>Used after macro inventory checks (AutoTotemA-D, MacroInventoryA)
 * to ensure the client's inventory state matches the server's. Sends
 * the player's current inventory contents back to the client.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class ResyncStrategy implements MitigationStrategy {

    public static final ResyncStrategy INSTANCE = new ResyncStrategy();

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        try {
            final Player player = ctx.player();
            // Force inventory update — sends all slots back to the client
            player.updateInventory();
            return MitigationResult.ok("Inventory resynced");
        } catch (final Exception ex) {
            return MitigationResult.failure("ResyncStrategy failed: " + ex.getMessage());
        }
    }

    @Override public MitigationPriority priority() { return MitigationPriority.HIGH; }
    @Override public String name() { return "Resync"; }
}
