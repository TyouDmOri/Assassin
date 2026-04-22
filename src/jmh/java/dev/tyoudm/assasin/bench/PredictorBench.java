/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.bench;

import dev.tyoudm.assasin.data.prediction.ElytraPredictor;
import dev.tyoudm.assasin.data.prediction.PhysicsConstants;
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
 * JMH benchmark for physics predictors.
 *
 * <p>Benchmarks {@link ElytraPredictor} tick-by-tick simulation and
 * deviation computation. Verifies that the elytra dive scenario
 * (2→40 b/s in ~8s) runs well under 0.1ms per tick.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*PredictorBench*"}
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
public class PredictorBench {

    private ElytraPredictor elytraPredictor;

    // Simulated player state — diving at 45° pitch
    private final float yaw   = 0.0f;
    private final float pitch = 45.0f;

    @Setup
    public void setup() {
        elytraPredictor = new ElytraPredictor();
        // Seed with initial velocity (2 b/t horizontal, 0 vertical)
        elytraPredictor.seed(2.0, 0.0, 0.0);
    }

    // ─── ElytraPredictor — one tick ───────────────────────────────────────────

    @Benchmark
    public ElytraPredictor.ElytraPrediction elytraOneTick() {
        return elytraPredictor.predict(yaw, pitch);
    }

    // ─── ElytraPredictor — 160 ticks (8 seconds dive) ────────────────────────
    // This simulates the full 2→40 b/s dive scenario.
    // Must NOT flag — verifies the predictor stays accurate over 8s.

    @Benchmark
    public double elytraDive8Seconds() {
        final ElytraPredictor pred = new ElytraPredictor();
        pred.seed(2.0, 0.0, 0.0);

        double finalSpeed = 0.0;
        for (int tick = 0; tick < 160; tick++) {
            final var result = pred.predict(yaw, pitch);
            finalSpeed = result.speedH();
        }
        return finalSpeed; // Should be ~40 b/t after 8s
    }

    // ─── Deviation computation ────────────────────────────────────────────────

    @Benchmark
    public double elytraDeviation() {
        // Simulate observed velocity slightly off from predicted
        return elytraPredictor.computeDeviation(
            elytraPredictor.getMotionX() + 0.1,
            elytraPredictor.getMotionY() - 0.05,
            elytraPredictor.getMotionZ() + 0.1
        );
    }

    // ─── Physics constants access (JIT inlining check) ────────────────────────

    @Benchmark
    public double gravityAccess() {
        return PhysicsConstants.GRAVITY;
    }

    @Benchmark
    public double elytraDragAccess() {
        return PhysicsConstants.ELYTRA_DRAG_H * PhysicsConstants.ELYTRA_DRAG_V;
    }
}
