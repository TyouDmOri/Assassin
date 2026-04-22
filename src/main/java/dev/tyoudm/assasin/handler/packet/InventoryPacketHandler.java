/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.handler.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.ActionTracker;
import dev.tyoudm.assasin.data.tracker.InventoryTracker;
import org.bukkit.entity.Player;

/**
 * Handles inbound inventory packets ({@code CLICK_WINDOW}, {@code HELD_ITEM_CHANGE}).
 *
 * <p>Feeds {@link InventoryTracker} and {@link ActionTracker} with click
 * and hotbar-key events for inventory and macro checks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class InventoryPacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public InventoryPacketHandler(final PlayerDataManager dataManager,
                                  final CheckProcessor checkProcessor) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager    = dataManager;
        this.checkProcessor = checkProcessor;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final var type = event.getPacketType();

        if (type == PacketType.Play.Client.CLICK_WINDOW) {
            handleClickWindow(event);
        } else if (type == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            handleHeldItemChange(event);
        } else if (type == PacketType.Play.Client.CLOSE_WINDOW) {
            handleCloseWindow(event);
        }
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.OPEN_WINDOW) return;

        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper  = new WrapperPlayServerOpenWindow(event);
        final long tick    = player.getServer().getCurrentTick();

        final InventoryTracker it = data.getInventoryTracker();
        if (it != null) it.onWindowOpen(wrapper.getContainerId(), tick);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void handleClickWindow(final PacketReceiveEvent event) {
        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final long nowMs = System.currentTimeMillis();
        final long tick  = data.getPacketCount();

        final InventoryTracker it = data.getInventoryTracker();
        if (it != null) it.recordClick(nowMs);

        final ActionTracker at = data.getActionTracker();
        if (at != null) at.record(nowMs, tick, ActionTracker.Action.WINDOW_CLICK, 0);

        checkProcessor.processInventory(player, data, player.getServer().getCurrentTick());
    }

    private void handleHeldItemChange(final PacketReceiveEvent event) {
        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientHeldItemChange(event);
        final int newSlot = wrapper.getSlot();
        final long nowMs  = System.currentTimeMillis();
        final long tick   = data.getPacketCount();

        final InventoryTracker it = data.getInventoryTracker();
        if (it != null) it.onHeldChange(newSlot, player.getInventory().getItem(newSlot), tick);

        final ActionTracker at = data.getActionTracker();
        if (at != null) at.record(nowMs, tick, ActionTracker.Action.HOTBAR_KEY, newSlot);
    }

    private void handleCloseWindow(final PacketReceiveEvent event) {
        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final InventoryTracker it = data.getInventoryTracker();
        if (it != null) it.onWindowClose();
    }
}
