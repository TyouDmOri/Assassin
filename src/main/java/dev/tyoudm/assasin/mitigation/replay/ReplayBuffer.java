package dev.tyoudm.assasin.mitigation.replay;

public final class ReplayBuffer {
    private final Snapshot[] snapshots = new Snapshot[200];
    private int cursor = 0;

    public void addSnapshot(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        // Reutilizamos el espacio del array sin crear nuevos objetos si es posible
        snapshots[cursor] = new Snapshot(x, y, z, yaw, pitch, onGround, System.currentTimeMillis());
        cursor = (cursor + 1) % snapshots.length;
    }

    public Snapshot[] getSnapshots() { return snapshots; }

    public record Snapshot(double x, double y, double z, float yaw, float pitch, boolean onGround, long timestamp) {}
}