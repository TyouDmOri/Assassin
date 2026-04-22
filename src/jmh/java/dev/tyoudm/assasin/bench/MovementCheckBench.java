/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.bench;

import dev.tyoudm.assasin.data.prediction.MovementPredictor;
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
import dev.tyoudm.assasin.util.RingBuffer;
import dev.tyoudm.assasin.util.WelfordStats;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for movement check hot paths.
 *
 * <p>Benchmarks the core computations used by SpeedA/B, FlyA/B, and
 * the movement predictor. Target: &lt;0.1ms P99 per check.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*MovementCheckBench*"}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MovementCheckBench {

    private RingBuffer.OfDouble speedHistory;
    private WelfordStats        welford;

    // Simulated player state
    private double speedH     = 0.2806;
    private double prevSpeedH = 0.2806;
    private boolean sprinting = true;
    private boolean sneaking  = false;
    private int     pingMs    = 50;

    @Setup
    public void setup() {
        speedHistory = new RingBuffer.OfDouble(20);
        welford      = new WelfordStats();

        for (int i = 0; i < 20; i++) {
            final double s = 0.2806 + (i % 3) * 0.01;
            speedHistory.add(s);
            welford.add(s);
        }
    }

    // ─── SpeedA hot path ──────────────────────────────────────────────────────

    @Benchmark
    public double speedAMaxExpected() {
        return MovementPredictor.maxExpectedSpeedH(sprinting, sneaking, pingMs);
    }

    @Benchmark
    public boolean speedACheck() {
        final double maxSpeed = MovementPredictor.maxExpectedSpeedH(sprinting, sneaking, pingMs);
        return speedH > maxSpeed;
    }

    // ─── SpeedB hot path ──────────────────────────────────────────────────────

    @Benchmark
    public double speedBFrictionPrediction() {
        final double friction = PhysicsConstants.FRICTION_DEFAULT * PhysicsConstants.DRAG_H_GROUND;
        return prevSpeedH * friction;
    }

    // ─── FlyA hot path ────────────────────────────────────────────────────────

    @Benchmark
    public double flyAExpectedY() {
        final double prevY = -0.1;
        return (prevY - PhysicsConstants.GRAVITY) * PhysicsConstants.DRAG_AIR;
    }

    // ─── WelfordStats hot path ────────────────────────────────────────────────

    @Benchmark
    public void welfordAdd() {
        welford.add(speedH);
    }

    @Benchmark
    public double welfordStdDev() {
        return welford.stdDev();
    }

    // ─── RingBuffer hot path ──────────────────────────────────────────────────

    @Benchmark
    public void ringBufferAdd() {
        speedHistory.add(speedH);
    }

    @Benchmark
    public double ringBufferGet() {
        return speedHistory.get(speedHistory.size() - 1);
    }
}
