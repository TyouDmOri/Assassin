package dev.tyoudm.assasin.mitigation.replay;

import java.util.LinkedList;

public final class ReplayBuffer {
    private final LinkedList<Snapshot> snapshots = new LinkedList<>();
    private static final int MAX_TICKS = 200; // 10 segundos de juego

    public void addSnapshot(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        if (snapshots.size() >= MAX_TICKS) {
            snapshots.removeFirst();
        }
        snapshots.add(new Snapshot(x, y, z, yaw, pitch, onGround, System.currentTimeMillis()));
    }

    public LinkedList<Snapshot> getSnapshots() {
        return snapshots;
    }

    public static record Snapshot(double x, double y, double z, float yaw, float pitch, boolean onGround, long timestamp) {}
}