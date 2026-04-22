/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.bench;

import dev.tyoudm.assasin.mitigation.MitigationProfile;
import dev.tyoudm.assasin.mitigation.MitigationStrategy;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for the mitigation engine hot paths.
 *
 * <p>Benchmarks {@link MitigationProfile#strategiesFor(double)} lookup
 * (NavigableMap floor-key) and the ViolationBuffer flag/decay cycle.
 * Target: &lt;0.1ms P99 per lookup.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*MitigationEngineBench*"}
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
public class MitigationEngineBench {

    private MitigationProfile mediumProfile;
    private MitigationProfile macroProfile;

    // Simulated VL values
    private double vl1  = 1.5;
    private double vl8  = 8.5;
    private double vl15 = 15.0;
    private double vl20 = 20.0;

    @Setup
    public void setup() {
        // Build profiles matching MitigationEngine defaults
        mediumProfile = new MitigationProfile("medium");
        // Stub strategies (no-op) — we only benchmark the lookup, not execution
        final MitigationStrategy noop = ctx -> dev.tyoudm.assasin.mitigation.MitigationResult.noop();

        mediumProfile
            .addStrategy(1.0,  new StubStrategy("CancelPacket", dev.tyoudm.assasin.mitigation.MitigationPriority.IMMEDIATE, noop))
            .addStrategy(1.0,  new StubStrategy("SetbackSoft",  dev.tyoudm.assasin.mitigation.MitigationPriority.NORMAL,    noop))
            .addStrategy(8.0,  new StubStrategy("Freeze",       dev.tyoudm.assasin.mitigation.MitigationPriority.HIGH,      noop))
            .addStrategy(15.0, new StubStrategy("Kick",         dev.tyoudm.assasin.mitigation.MitigationPriority.DEFERRED,  noop));

        macroProfile = new MitigationProfile("macro");
        macroProfile
            .addStrategy(10.0, new StubStrategy("CancelPacket", dev.tyoudm.assasin.mitigation.MitigationPriority.IMMEDIATE, noop))
            .addStrategy(15.0, new StubStrategy("Resync",       dev.tyoudm.assasin.mitigation.MitigationPriority.HIGH,      noop))
            .addStrategy(20.0, new StubStrategy("Kick",         dev.tyoudm.assasin.mitigation.MitigationPriority.DEFERRED,  noop));
    }

    // ─── Profile lookup — NavigableMap floor-key ──────────────────────────────

    @Benchmark
    public List<MitigationStrategy> mediumProfileVl1() {
        return mediumProfile.strategiesFor(vl1);
    }

    @Benchmark
    public List<MitigationStrategy> mediumProfileVl8() {
        return mediumProfile.strategiesFor(vl8);
    }

    @Benchmark
    public List<MitigationStrategy> mediumProfileVl15() {
        return mediumProfile.strategiesFor(vl15);
    }

    @Benchmark
    public List<MitigationStrategy> macroProfileVl20() {
        return macroProfile.strategiesFor(vl20);
    }

    // ─── ViolationBuffer flag + decay cycle ───────────────────────────────────

    @Benchmark
    public double violationBufferCycle() {
        final var buf = new dev.tyoudm.assasin.mitigation.buffer.ViolationBuffer(10.0);
        buf.flag(1.5, 1L);
        buf.decay();
        return buf.getVl();
    }

    // ─── Stub strategy ────────────────────────────────────────────────────────

    private record StubStrategy(
        String name,
        dev.tyoudm.assasin.mitigation.MitigationPriority priority,
        MitigationStrategy delegate
    ) implements MitigationStrategy {
        @Override
        public dev.tyoudm.assasin.mitigation.MitigationResult execute(
                final dev.tyoudm.assasin.mitigation.MitigationContext ctx) {
            return delegate.execute(ctx);
        }
    }
}
