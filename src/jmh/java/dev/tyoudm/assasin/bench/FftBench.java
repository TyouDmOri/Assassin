/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.bench;

import dev.tyoudm.assasin.util.FFT;
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
 * JMH benchmark for {@link FFT} — radix-2 n=32 vs n=64.
 *
 * <p>Verifies that FFT computation stays well under 1ms even for n=64,
 * since it's called asynchronously only when σ is suspicious.
 * Target: &lt;50µs for n=32, &lt;100µs for n=64.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*FftBench*"}
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
public class FftBench {

    private double[] re32, im32;
    private double[] re64, im64;

    @Setup
    public void setup() {
        re32 = new double[32];
        im32 = new double[32];
        re64 = new double[64];
        im64 = new double[64];

        // Simulate click intervals (ms) — realistic input
        for (int i = 0; i < 32; i++) re32[i] = 50.0 + (i % 5) * 2.0;
        for (int i = 0; i < 64; i++) re64[i] = 50.0 + (i % 5) * 2.0;
    }

    @Benchmark
    public void fftN32() {
        // Reset imaginary part (FFT is in-place)
        final double[] re = re32.clone();
        final double[] im = new double[32];
        FFT.fft(re, im);
    }

    @Benchmark
    public void fftN64() {
        final double[] re = re64.clone();
        final double[] im = new double[64];
        FFT.fft(re, im);
    }

    @Benchmark
    public double fftN32WithMagnitudesAndKurtosis() {
        final double[] re = re32.clone();
        final double[] im = new double[32];
        FFT.fft(re, im);
        final double[] mags = FFT.magnitudes(re, im);
        return FFT.kurtosis(mags);
    }
}
