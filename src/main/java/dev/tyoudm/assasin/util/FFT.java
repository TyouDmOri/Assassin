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
 * Cooley-Tukey radix-2 iterative (in-place) FFT.
 *
 * <p>Used by {@code AutoClickerC} and {@code MacroClickerA} to detect
 * periodic click patterns. The FFT is computed asynchronously via
 * {@code AsyncProcessor} — only when preliminary Welford variance is
 * suspicious — to keep the main thread overhead near zero.
 *
 * <h2>Input requirements</h2>
 * <ul>
 *   <li>Input length must be a power of two (e.g., 32, 64).</li>
 *   <li>Real-valued input: pass the signal in {@code re[]} with {@code im[]} zeroed.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *   double[] re = new double[32];
 *   double[] im = new double[32];
 *   // fill re[] with click intervals (ms)
 *   FFT.fft(re, im);
 *   // re[k] and im[k] now hold the k-th frequency bin
 *   double[] magnitudes = FFT.magnitudes(re, im);
 *   double   kurtosis   = FFT.kurtosis(magnitudes);
 * }</pre>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class FFT {

    // ─── Core FFT ─────────────────────────────────────────────────────────────

    /**
     * Computes the in-place Cooley-Tukey radix-2 DIT FFT.
     *
     * <p>After this call, {@code re[k]} and {@code im[k]} hold the real and
     * imaginary parts of the k-th frequency bin.
     *
     * @param re real part of the input signal (length must be a power of 2)
     * @param im imaginary part of the input signal (zero for real-valued input)
     * @throws IllegalArgumentException if {@code re} and {@code im} have different
     *                                  lengths or the length is not a power of 2
     */
    public static void fft(final double[] re, final double[] im) {
        final int n = re.length;
        if (n != im.length) {
            throw new IllegalArgumentException("re and im arrays must have the same length.");
        }
        if (n == 0 || (n & (n - 1)) != 0) {
            throw new IllegalArgumentException("FFT length must be a power of 2, got: " + n);
        }

        // Bit-reversal permutation
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                double tmp = re[i]; re[i] = re[j]; re[j] = tmp;
                       tmp = im[i]; im[i] = im[j]; im[j] = tmp;
            }
        }

        // Butterfly stages
        for (int len = 2; len <= n; len <<= 1) {
            final double ang = -2.0 * Math.PI / len;
            final double wRe = Math.cos(ang);
            final double wIm = Math.sin(ang);

            for (int i = 0; i < n; i += len) {
                double curRe = 1.0;
                double curIm = 0.0;

                for (int j = 0; j < len / 2; j++) {
                    final int u = i + j;
                    final int v = i + j + len / 2;

                    final double uRe = re[u];
                    final double uIm = im[u];
                    final double vRe = re[v] * curRe - im[v] * curIm;
                    final double vIm = re[v] * curIm + im[v] * curRe;

                    re[u] = uRe + vRe;
                    im[u] = uIm + vIm;
                    re[v] = uRe - vRe;
                    im[v] = uIm - vIm;

                    // Rotate twiddle factor
                    final double nextRe = curRe * wRe - curIm * wIm;
                    curIm = curRe * wIm + curIm * wRe;
                    curRe = nextRe;
                }
            }
        }
    }

    // ─── Magnitude spectrum ───────────────────────────────────────────────────

    /**
     * Computes the magnitude spectrum from FFT output.
     *
     * <p>Returns only the first {@code n/2 + 1} bins (positive frequencies).
     * Bin 0 is the DC component; bin {@code n/2} is the Nyquist frequency.
     *
     * @param re real parts after {@link #fft}
     * @param im imaginary parts after {@link #fft}
     * @return magnitude array of length {@code n/2 + 1}
     */
    public static double[] magnitudes(final double[] re, final double[] im) {
        final int half = re.length / 2 + 1;
        final double[] mag = new double[half];
        for (int k = 0; k < half; k++) {
            mag[k] = Math.sqrt(re[k] * re[k] + im[k] * im[k]);
        }
        return mag;
    }

    // ─── Statistical descriptors ──────────────────────────────────────────────

    /**
     * Computes the excess kurtosis of the magnitude spectrum.
     *
     * <p>A low kurtosis (near 0) indicates a flat, noise-like spectrum (human).
     * A high kurtosis indicates sharp peaks (periodic / macro-like).
     *
     * <p>Formula: {@code kurtosis = μ4 / σ4 - 3}
     *
     * @param magnitudes magnitude spectrum (from {@link #magnitudes})
     * @return excess kurtosis, or {@code 0.0} if fewer than 4 values
     */
    public static double kurtosis(final double[] magnitudes) {
        final int n = magnitudes.length;
        if (n < 4) return 0.0;

        // Mean
        double mean = 0.0;
        for (final double v : magnitudes) mean += v;
        mean /= n;

        // Variance and 4th central moment
        double m2 = 0.0;
        double m4 = 0.0;
        for (final double v : magnitudes) {
            final double d  = v - mean;
            final double d2 = d * d;
            m2 += d2;
            m4 += d2 * d2;
        }
        m2 /= n;
        m4 /= n;

        if (m2 < MathUtil.EPSILON) return 0.0;
        return m4 / (m2 * m2) - 3.0;
    }

    /**
     * Returns the index of the dominant frequency bin (excluding DC at index 0).
     *
     * @param magnitudes magnitude spectrum (from {@link #magnitudes})
     * @return index of the bin with the highest magnitude (1 or above)
     */
    public static int dominantBin(final double[] magnitudes) {
        int    maxIdx = 1;
        double maxVal = magnitudes.length > 1 ? magnitudes[1] : 0.0;
        for (int k = 2; k < magnitudes.length; k++) {
            if (magnitudes[k] > maxVal) {
                maxVal = magnitudes[k];
                maxIdx = k;
            }
        }
        return maxIdx;
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Utility class — no instantiation. */
    private FFT() {
        throw new UnsupportedOperationException("FFT is a utility class.");
    }
}
