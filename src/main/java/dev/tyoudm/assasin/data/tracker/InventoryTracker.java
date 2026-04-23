package dev.tyoudm.assasin.data.tracker;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public final class InventoryTracker {
    private boolean windowOpen;
    private int openWindowId = -1;
    private long lastWindowCloseTick;

    public void onWindowOpen(int windowId, long tick) {
        this.windowOpen = true;
        this.openWindowId = windowId;
    }

    public void onWindowClose(long tick) {
        this.windowOpen = false;
        this.openWindowId = -1;
        this.lastWindowCloseTick = tick;
    }

    public void reset() {
        windowOpen = false;
        openWindowId = -1;
        lastWindowCloseTick = 0;
    }
}