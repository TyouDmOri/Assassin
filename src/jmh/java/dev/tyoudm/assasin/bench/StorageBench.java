/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.bench;

import dev.tyoudm.assasin.storage.SQLiteProvider;
import dev.tyoudm.assasin.storage.StorageProvider;
import dev.tyoudm.assasin.storage.model.ViolationRecord;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * JMH benchmark for storage insert batching.
 *
 * <p>Uses an in-memory SQLite database (`:memory:`) to benchmark
 * violation record inserts. Verifies that async DB writes don't
 * block the main thread.
 *
 * <p>Run: {@code ./gradlew jmh --tests "*StorageBench*"}
 *
 * @author TyouDm
 * @version 1.0.0
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class StorageBench {

    private StorageProvider storage;
    private UUID            testUuid;
    private ViolationRecord sampleRecord;

    @Setup
    public void setup() throws Exception {
        testUuid = UUID.randomUUID();

        // Use in-memory SQLite for benchmarking
        storage = new SQLiteProvider(
            new File(":memory:"),
            Logger.getLogger("StorageBench"),
            ForkJoinPool.commonPool()
        );
        storage.init();

        sampleRecord = ViolationRecord.of(
            testUuid, "SpeedA", 3.5,
            System.currentTimeMillis(),
            50, 20.0, "world", 0.0, 64.0, 0.0,
            "SoftSetback", "{}"
        );
    }

    @TearDown
    public void tearDown() {
        storage.close();
    }

    // ─── Single insert ────────────────────────────────────────────────────────

    @Benchmark
    public long insertViolation() throws Exception {
        return storage.insertViolation(sampleRecord).get();
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    @Benchmark
    public int countViolations() throws Exception {
        return storage.countViolations(testUuid).get();
    }

    // ─── Profile load ─────────────────────────────────────────────────────────

    @Benchmark
    public Object loadProfile() throws Exception {
        return storage.loadProfile(testUuid).get();
    }
}
