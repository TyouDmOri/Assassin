/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.component;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * Functional interface for GUI slot click actions.
 *
 * <p>Each {@link GuiItem} carries a {@code GuiAction} that is invoked
 * when the player clicks the corresponding slot. The event is always
 * cancelled before the action is called.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@FunctionalInterface
public interface GuiAction extends Consumer<InventoryClickEvent> {

    /** A no-op action that does nothing. */
    GuiAction NOOP = event -> {};
}
