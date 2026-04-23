package dev.tyoudm.assasin.mitigation.buffer;

public final class ViolationBuffer {
    private double vl = 0.0;
    private final double maxVl;

    public ViolationBuffer(double maxVl) {
        this.maxVl = maxVl;
    }

    public double add(double amount) {
        this.vl = Math.min(maxVl + 10.0, vl + amount);
        return vl;
    }

    public void decay(double amount) {
        this.vl = Math.max(0.0, vl - amount);
    }

    public double getVl() { return vl; }
}