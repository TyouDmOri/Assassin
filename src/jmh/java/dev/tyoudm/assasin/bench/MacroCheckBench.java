/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.bench;

import dev.tyoudm.assasin.util.RingBuffer;
import dev.tyoudm.assasin.util.RollingHash;
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
 * JMH benchmark for macro detection hot paths.
 *
 * <p>Benchmarks Rabin-Karp rolling hash (MacroSequenceA),
 * Welford variance (MacroVarianceA), and action buffer operations.
 * Target: &lt;0.1ms P99 per check.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*MacroCheckBench*"}
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
public class MacroCheckBench {

    private RollingHash         trigramHash;
    private RollingHash         tetragramHash;
    private RingBuffer.OfLong   actionBuffer;
    private WelfordStats        intervalStats;

    // Simulated action sequence (action type ordinals 0-6)
    private final long[] actions = {0, 1, 0, 2, 0, 1, 0, 2, 0, 1};

    @Setup
    public void setup() {
        trigramHash   = new RollingHash(3);
        tetragramHash = new RollingHash(4);
        actionBuffer  = new RingBuffer.OfLong(64);
        intervalStats = new WelfordStats();

        // Seed hashes
        trigramHash.seed(0L);
        trigramHash.seed(1L);
        tetragramHash.seed(0L);
        tetragramHash.seed(1L);
        tetragramHash.seed(2L);

        // Fill action buffer
        for (int i = 0; i < 64; i++) {
            actionBuffer.add(actions[i % actions.length]);
        }

        // Fill interval stats
        for (int i = 0; i < 30; i++) {
            intervalStats.add(50.0 + (i % 3) * 1.5);
        }
    }

    // ─── MacroSequenceA — Rabin-Karp O(1) roll ────────────────────────────────

    @Benchmark
    public long rollingHashTrigramRoll() {
        return trigramHash.roll(0L, 2L);
    }

    @Benchmark
    public long rollingHashTetragramRoll() {
        return tetragramHash.roll(0L, 3L);
    }

    // ─── MacroVarianceA — Welford O(1) add + stdDev ───────────────────────────

    @Benchmark
    public void welfordAdd() {
        intervalStats.add(51.0);
    }

    @Benchmark
    public double welfordStdDev() {
        return intervalStats.stdDev();
    }

    // ─── ActionTracker — ring buffer O(1) ────────────────────────────────────

    @Benchmark
    public void actionBufferAdd() {
        actionBuffer.add(1L);
    }

    @Benchmark
    public long actionBufferGet() {
        return actionBuffer.get(32);
    }

    // ─── Full sequence scan (20 actions, trigrams) ────────────────────────────

    @Benchmark
    public int sequenceScan() {
        final RollingHash rh = new RollingHash(3);
        rh.seed(actionBuffer.get(0));
        rh.seed(actionBuffer.get(1));

        int maxCount = 0;
        final java.util.HashMap<Long, Integer> counts = new java.util.HashMap<>();

        for (int i = 2; i < 20; i++) {
            final long hash  = rh.roll(actionBuffer.get(i - 2), actionBuffer.get(i));
            final int  count = counts.merge(hash, 1, Integer::sum);
            if (count > maxCount) maxCount = count;
        }
        return maxCount;
    }
}
