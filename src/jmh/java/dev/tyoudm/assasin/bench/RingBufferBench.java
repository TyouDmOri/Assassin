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
 * JMH benchmark for {@link RingBuffer} primitive specializations.
 *
 * <p>Verifies zero-autoboxing and O(1) add/get performance.
 * Target: &lt;0.1ms P99 per operation.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*RingBufferBench*"}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class RingBufferBench {

    private RingBuffer.OfDouble doubleBuffer;
    private RingBuffer.OfLong   longBuffer;
    private RingBuffer.OfInt    intBuffer;

    @Setup
    public void setup() {
        doubleBuffer = new RingBuffer.OfDouble(64);
        longBuffer   = new RingBuffer.OfLong(64);
        intBuffer    = new RingBuffer.OfInt(64);

        // Pre-fill
        for (int i = 0; i < 64; i++) {
            doubleBuffer.add(i * 0.1);
            longBuffer.add(i);
            intBuffer.add(i);
        }
    }

    @Benchmark
    public void addDouble() {
        doubleBuffer.add(3.14);
    }

    @Benchmark
    public double getDouble() {
        return doubleBuffer.get(32);
    }

    @Benchmark
    public void addLong() {
        longBuffer.add(42L);
    }

    @Benchmark
    public long getLong() {
        return longBuffer.get(32);
    }

    @Benchmark
    public void addInt() {
        intBuffer.add(7);
    }

    @Benchmark
    public int getInt() {
        return intBuffer.get(32);
    }

    @Benchmark
    public double meanDouble() {
        return doubleBuffer.mean();
    }
}
