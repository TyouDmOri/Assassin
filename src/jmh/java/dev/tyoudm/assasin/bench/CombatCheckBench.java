/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.bench;

import dev.tyoudm.assasin.latency.PingCompensator;
import dev.tyoudm.assasin.util.MathUtil;
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
 * JMH benchmark for combat check hot paths.
 *
 * <p>Benchmarks ReachA distance check, AimA GCD computation,
 * AutoClickerA Welford variance, and ping compensation.
 * Target: &lt;0.1ms P99 per check.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*CombatCheckBench*"}
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
public class CombatCheckBench {

    private RingBuffer.OfDouble yawDeltas;
    private WelfordStats        clickIntervalStats;
    private int                 pingMs = 80;

    // Simulated attack data
    private double attackerX = 0.0, attackerY = 64.0, attackerZ = 0.0;
    private double targetX   = 2.5, targetY   = 64.0, targetZ   = 0.0;

    @Setup
    public void setup() {
        yawDeltas          = new RingBuffer.OfDouble(40);
        clickIntervalStats = new WelfordStats();

        for (int i = 0; i < 40; i++) {
            yawDeltas.add(2.5 + (i % 4) * 0.3);
        }
        for (int i = 0; i < 30; i++) {
            clickIntervalStats.add(55.0 + (i % 5) * 2.0);
        }
    }

    // ─── ReachA hot path — distanceSq (no sqrt) ───────────────────────────────

    @Benchmark
    public double reachADistanceSq() {
        return MathUtil.distanceSq(
            attackerX, attackerY + 1.62, attackerZ,
            targetX,   targetY,          targetZ);
    }

    @Benchmark
    public double reachAWithPingComp() {
        final double comp     = PingCompensator.reachCompensation(pingMs);
        final double maxReach = 3.0 + comp;
        final double distSq   = MathUtil.distanceSq(
            attackerX, attackerY + 1.62, attackerZ,
            targetX,   targetY,          targetZ);
        return distSq - maxReach * maxReach; // positive = violation
    }

    // ─── AimA hot path — GCD over 40 yaw deltas ──────────────────────────────

    @Benchmark
    public long aimAGcd() {
        final int n = yawDeltas.size();
        long gcd = 0L;
        for (int i = 0; i < n; i++) {
            final long scaled = Math.abs(Math.round(yawDeltas.get(i) * 1000.0));
            if (scaled == 0) continue;
            gcd = MathUtil.gcd(gcd, scaled);
            if (gcd == 1L) break;
        }
        return gcd;
    }

    // ─── AutoClickerA hot path — Welford stdDev ───────────────────────────────

    @Benchmark
    public void autoClickerAAdd() {
        clickIntervalStats.add(55.0);
    }

    @Benchmark
    public double autoClickerAStdDev() {
        return clickIntervalStats.stdDev();
    }

    // ─── Ping compensation ────────────────────────────────────────────────────

    @Benchmark
    public double pingCompReach() {
        return PingCompensator.reachCompensation(pingMs);
    }

    @Benchmark
    public int pingCompVelocityTicks() {
        return PingCompensator.velocityCompensationTicks(pingMs);
    }
}
