/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.data.tracker;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Tracks per-player inventory interaction state for inventory and macro checks.
 *
 * <p>Records window open/close events, click timing, and held-item changes
 * used by {@code InventoryA/B}, {@code AutoTotemA-D}, {@code ChestStealerA},
 * {@code AutoArmorA}, {@code MacroInventoryA}, and {@code AttributeSwapA}.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class InventoryTracker {

    // ─── Window state ─────────────────────────────────────────────────────────

    /** Whether an inventory window is currently open. */
    private boolean windowOpen;

    /** The window ID of the currently open inventory (-1 if none). */
    private int     openWindowId = -1;

    /** Server tick when the current window was opened. */
    private long    windowOpenTick;

    /** Number of clicks in the current window session. */
    private int     clicksThisWindow;

    /** System time (ms) of the last inventory click. */
    private long    lastClickMs;

    // ─── Held item ────────────────────────────────────────────────────────────

    /** Current held item slot (0–8). */
    private int      heldSlot;

    /** Material of the currently held item. */
    private Material heldMaterial = Material.AIR;

    /** Server tick of the last held-item change. */
    private long     lastHeldChangeTick;

    /** Number of held-item changes in the last 20 ticks. */
    private int      heldChanges20t;

    // ─── Totem tracking ───────────────────────────────────────────────────────

    /** System time (ms) of the last totem-swap event. */
    private long lastTotemSwapMs;

    /** Number of totem swaps observed. */
    private int  totemSwapCount;

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Records a window open event.
     *
     * @param windowId    the opened window ID
     * @param currentTick current server tick
     */
    public void onWindowOpen(final int windowId, final long currentTick) {
        windowOpen       = true;
        openWindowId     = windowId;
        windowOpenTick   = currentTick;
        clicksThisWindow = 0;
    }

    /**
     * Records a window close event.
     */
    public void onWindowClose() {
        windowOpen       = false;
        openWindowId     = -1;
        clicksThisWindow = 0;
    }

    /**
     * Records an inventory click.
     *
     * @param nowMs current system time in ms
     */
    public void recordClick(final long nowMs) {
        lastClickMs = nowMs;
        clicksThisWindow++;
    }

    /**
     * Records a held-item change.
     *
     * @param newSlot     the new held slot (0–8)
     * @param newItem     the item now held (may be null)
     * @param currentTick current server tick
     */
    public void onHeldChange(final int newSlot, final ItemStack newItem, final long currentTick) {
        heldSlot           = newSlot;
        heldMaterial       = (newItem == null || newItem.getType() == Material.AIR)
                             ? Material.AIR : newItem.getType();
        lastHeldChangeTick = currentTick;
        heldChanges20t++;
    }

    /**
     * Records a totem-swap event (offhand totem replaced after pop).
     *
     * @param nowMs current system time in ms
     */
    public void recordTotemSwap(final long nowMs) {
        lastTotemSwapMs = nowMs;
        totemSwapCount++;
    }

    /**
     * Decays rolling counters. Call once per tick.
     *
     * @param currentTick current server tick
     */
    public void decayCounters(final long currentTick) {
        if ((currentTick % 20) == 0) {
            heldChanges20t = 0;
        }
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public boolean  isWindowOpen()          { return windowOpen; }
    public int      getOpenWindowId()       { return openWindowId; }
    public long     getWindowOpenTick()     { return windowOpenTick; }
    public int      getClicksThisWindow()   { return clicksThisWindow; }
    public long     getLastClickMs()        { return lastClickMs; }
    public int      getHeldSlot()           { return heldSlot; }
    public Material getHeldMaterial()       { return heldMaterial; }
    public long     getLastHeldChangeTick() { return lastHeldChangeTick; }
    public int      getHeldChanges20t()     { return heldChanges20t; }
    public long     getLastTotemSwapMs()    { return lastTotemSwapMs; }
    public int      getTotemSwapCount()     { return totemSwapCount; }

    /** Resets all state. Call on respawn. */
    public void reset() {
        windowOpen         = false;
        openWindowId       = -1;
        windowOpenTick     = 0L;
        clicksThisWindow   = 0;
        lastClickMs        = 0L;
        heldSlot           = 0;
        heldMaterial       = Material.AIR;
        lastHeldChangeTick = 0L;
        heldChanges20t     = 0;
        lastTotemSwapMs    = 0L;
        totemSwapCount     = 0;
    }
}
