package dev.tyoudm.assasin.data;

public class CheckData {
    // Buffers de combate
    private double reachBuffer;
    private double killauraBuffer;

    // Buffers de movimiento
    private double speedBBuffer;
    private double motionABuffer;
    private double timerBalance;

    // Getters y Setters
    public double getReachBuffer() { return reachBuffer; }
    public void setReachBuffer(double reachBuffer) { this.reachBuffer = reachBuffer; }

    public double getSpeedBBuffer() { return speedBBuffer; }
    public void setSpeedBBuffer(double speedBBuffer) { this.speedBBuffer = speedBBuffer; }

    public double getMotionABuffer() { return motionABuffer; }
    public void setMotionABuffer(double motionABuffer) { this.motionABuffer = motionABuffer; }

    public double getTimerBalance() { return timerBalance; }
    public void setTimerBalance(double timerBalance) { this.timerBalance = timerBalance; }
}