/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.handler.async;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Custom {@link ForkJoinPool} for offloading heavy async work.
 *
 * <p>Used for:
 * <ul>
 *   <li>FFT computation ({@code AutoClickerC}, {@code MacroClickerA})</li>
 *   <li>Database writes (storage layer)</li>
 *   <li>Discord webhook HTTP calls</li>
 *   <li>Rabin-Karp n-gram hashing ({@code MacroSequenceA})</li>
 * </ul>
 *
 * <h2>Pool configuration</h2>
 * <ul>
 *   <li>Parallelism: {@code max(2, availableProcessors - 1)}</li>
 *   <li>Thread name prefix: {@code assasin-async-}</li>
 *   <li>Daemon threads: {@code true} (won't block JVM shutdown)</li>
 * </ul>
 *
 * <h2>Metrics</h2>
 * Tracks submitted, completed, and rejected task counts for
 * {@code /assasin stats} and JMH benchmarks.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AsyncProcessor {

    // ─── Metrics ──────────────────────────────────────────────────────────────

    private final AtomicLong submitted = new AtomicLong();
    private final AtomicLong completed = new AtomicLong();
    private final AtomicLong rejected  = new AtomicLong();

    // ─── Pool ─────────────────────────────────────────────────────────────────

    private final ForkJoinPool pool;
    private final Logger       logger;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates and starts the async processor.
     *
     * @param logger plugin logger for error reporting
     */
    public AsyncProcessor(final Logger logger) {
        this.logger = logger;
        final int parallelism = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
        this.pool = new ForkJoinPool(
            parallelism,
            pool -> {
                final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("assasin-async-" + thread.getPoolIndex());
                thread.setDaemon(true);
                return thread;
            },
            (t, e) -> logger.severe("[ASSASIN] Uncaught exception in async thread " + t.getName() + ": " + e.getMessage()),
            false
        );
        logger.info("[ASSASIN] AsyncProcessor started with parallelism=" + parallelism);
    }

    // ─── Submit ───────────────────────────────────────────────────────────────

    /**
     * Submits a {@link Runnable} for async execution.
     *
     * <p>If the pool is shut down or the task is rejected, the rejection
     * counter is incremented and the error is logged.
     *
     * @param task the task to execute
     */
    public void submit(final Runnable task) {
        submitted.incrementAndGet();
        try {
            pool.execute(() -> {
                try {
                    task.run();
                } finally {
                    completed.incrementAndGet();
                }
            });
        } catch (final RejectedExecutionException ex) {
            rejected.incrementAndGet();
            logger.warning("[ASSASIN] AsyncProcessor rejected task: " + ex.getMessage());
        }
    }

    /**
     * Submits a {@link Supplier} for async execution and returns a
     * {@link java.util.concurrent.CompletableFuture} with the result.
     *
     * @param supplier the supplier to execute
     * @param <T>      the result type
     * @return a future that completes with the supplier's result
     */
    public <T> java.util.concurrent.CompletableFuture<T> supply(final Supplier<T> supplier) {
        submitted.incrementAndGet();
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } finally {
                completed.incrementAndGet();
            }
        }, pool);
    }

    // ─── Shutdown ─────────────────────────────────────────────────────────────

    /**
     * Shuts down the pool gracefully, waiting up to 5 seconds for pending tasks.
     * Call from {@link dev.tyoudm.assasin.core.ServiceContainer#disable()}.
     */
    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                logger.warning("[ASSASIN] AsyncProcessor forced shutdown after timeout.");
            }
        } catch (final InterruptedException ex) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info(String.format(
            "[ASSASIN] AsyncProcessor shutdown. submitted=%d completed=%d rejected=%d",
            submitted.get(), completed.get(), rejected.get()
        ));
    }

    // ─── Metrics ──────────────────────────────────────────────────────────────

    /** Returns the total number of tasks submitted. */
    public long getSubmitted() { return submitted.get(); }

    /** Returns the total number of tasks completed. */
    public long getCompleted() { return completed.get(); }

    /** Returns the total number of tasks rejected. */
    public long getRejected()  { return rejected.get(); }

    /** Returns the number of tasks currently queued or running. */
    public long getPending()   { return submitted.get() - completed.get() - rejected.get(); }

    /** Returns the pool's current parallelism level. */
    public int getParallelism() { return pool.getParallelism(); }

    /** Returns {@code true} if the pool has been shut down. */
    public boolean isShutdown() { return pool.isShutdown(); }
}
