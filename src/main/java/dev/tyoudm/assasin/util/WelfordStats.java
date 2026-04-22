/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.util;

/**
 * Online mean and variance calculator using Welford's one-pass algorithm.
 *
 * <p>Computes running mean (μ) and population/sample variance (σ²) in O(1)
 * per update with numerically stable arithmetic. No buffer required — suitable
 * for continuous streams of click intervals, movement deltas, etc.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   WelfordStats stats = new WelfordStats();
 *   stats.add(interval);
 *   double mean   = stats.mean();
 *   double stdDev = stats.stdDev();   // population σ
 * }</pre>
 *
 * <h2>Algorithm</h2>
 * Welford (1962): for each new value {@code x}:
 * <pre>
 *   n    += 1
 *   delta = x - mean
 *   mean += delta / n
 *   M2   += delta * (x - mean)   // updated mean
 *   variance = M2 / n            // population variance
 * </pre>
 *
 * @author TyouDm
 * @version 1.0.0
 * @see <a href="https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm">Welford's online algorithm</a>
 */
public final class WelfordStats {

    private long   count;   // number of samples
    private double mean;    // running mean
    private double m2;      // sum of squared deviations from mean

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Adds a new sample to the running statistics.
     *
     * @param value the new data point
     */
    public void add(final double value) {
        count++;
        final double delta  = value - mean;
        mean               += delta / count;
        final double delta2 = value - mean;
        m2                 += delta * delta2;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Returns the number of samples added so far.
     *
     * @return sample count
     */
    public long count() {
        return count;
    }

    /**
     * Returns the running arithmetic mean.
     * Returns {@code 0.0} if no samples have been added.
     *
     * @return mean (μ)
     */
    public double mean() {
        return mean;
    }

    /**
     * Returns the population variance (M2 / n).
     * Returns {@code 0.0} if fewer than 2 samples have been added.
     *
     * @return population variance (σ²)
     */
    public double variance() {
        return count < 2 ? 0.0 : m2 / count;
    }

    /**
     * Returns the sample variance (M2 / (n-1), Bessel-corrected).
     * Returns {@code 0.0} if fewer than 2 samples have been added.
     *
     * @return sample variance
     */
    public double sampleVariance() {
        return count < 2 ? 0.0 : m2 / (count - 1);
    }

    /**
     * Returns the population standard deviation (√variance).
     * Returns {@code 0.0} if fewer than 2 samples have been added.
     *
     * @return population standard deviation (σ)
     */
    public double stdDev() {
        return Math.sqrt(variance());
    }

    /**
     * Returns the sample standard deviation (√sampleVariance).
     * Returns {@code 0.0} if fewer than 2 samples have been added.
     *
     * @return sample standard deviation
     */
    public double sampleStdDev() {
        return Math.sqrt(sampleVariance());
    }

    // ─── Reset ────────────────────────────────────────────────────────────────

    /**
     * Resets all statistics to their initial state.
     * Call on player death, respawn, or check reset.
     */
    public void reset() {
        count = 0;
        mean  = 0.0;
        m2    = 0.0;
    }

    // ─── Object ───────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("WelfordStats{n=%d, μ=%.4f, σ=%.4f}", count, mean, stdDev());
    }
}
