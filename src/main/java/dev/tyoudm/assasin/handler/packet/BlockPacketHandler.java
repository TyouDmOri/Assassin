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
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import dev.tyoudm.assasin.check.CheckProcessor;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.PlayerDataManager;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import org.bukkit.entity.Player;

/**
 * Handles inbound block interaction packets ({@code PLAYER_DIGGING}).
 *
 * <p>Feeds {@link BlockTracker} with break events for {@code FastBreakA}
 * and {@code NukerA}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class BlockPacketHandler extends PacketListenerAbstract {

    private final PlayerDataManager dataManager;
    private final CheckProcessor    checkProcessor;

    public BlockPacketHandler(final PlayerDataManager dataManager,
                              final CheckProcessor checkProcessor) {
        super(PacketListenerPriority.NORMAL);
        this.dataManager    = dataManager;
        this.checkProcessor = checkProcessor;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;

        final Player player = (Player) event.getPlayer();
        if (player == null) return;
        final PlayerData data = dataManager.get(player.getUniqueId());
        if (data == null) return;

        final var wrapper = new WrapperPlayClientPlayerDigging(event);
        final var action  = wrapper.getAction();

        // Only process finished digs
        if (action != DiggingAction.FINISHED_DIGGING) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null) return;

        // Material lookup deferred to event handler (Bukkit API on main thread)
        // Here we only record the tick-level break count
        bt.recordBreak(org.bukkit.Material.AIR, data.getPacketCount());

        checkProcessor.processBlock(player, data, player.getServer().getCurrentTick());
    }
}
