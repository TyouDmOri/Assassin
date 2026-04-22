/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.macro;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.ActionTracker;
import dev.tyoudm.assasin.data.tracker.MacroStateTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.util.RingBuffer;
import dev.tyoudm.assasin.util.RollingHash;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * MacroSequenceA — N-gram Rabin-Karp sequence detection.
 *
 * <p>Detects repeated input sequences (trigrams and tetragrams) in the
 * {@link ActionTracker} ring buffer using Rabin-Karp rolling hash.
 * A macro produces the same sequence of actions repeatedly with near-zero
 * timing variance; legitimate players have diverse, non-repeating patterns.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Extract action type ordinals from the last 64 actions.</li>
 *   <li>Compute rolling trigram/tetragram hashes over the sequence.</li>
 *   <li>Count hash occurrences in a {@link HashMap}.</li>
 *   <li>If any n-gram appears more than {@link #MAX_REPEAT_COUNT} times,
 *       add evidence to the {@link MacroStateTracker}.</li>
 * </ol>
 *
 * <h2>Lazy computation</h2>
 * Only runs every {@link #COMPUTE_INTERVAL} ticks to keep per-tick cost near zero.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "MacroSequenceA",
    type              = CheckType.MACRO_SEQUENCE_A,
    category          = CheckCategory.MACRO,
    description       = "Detects repeated input sequences via Rabin-Karp n-gram hashing.",
    maxVl             = 20.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "macro"
)
public final class MacroSequenceA extends Check {

    /** N-gram window sizes to check (trigrams + tetragrams). */
    private static final int[] WINDOW_SIZES = {3, 4};

    /** Maximum allowed repetitions of any single n-gram before flagging. */
    private static final int MAX_REPEAT_COUNT = 5;

    /** Minimum actions in buffer before running the check. */
    private static final int MIN_ACTIONS = 20;

    /** Ticks between sequence analysis runs. */
    private static final int COMPUTE_INTERVAL = 20;

    private int ticksSinceCompute;

    public MacroSequenceA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick,
                ExemptType.HIGH_PING, ExemptType.LAG_SPIKE,
                ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        ticksSinceCompute++;
        if (ticksSinceCompute < COMPUTE_INTERVAL) return;
        ticksSinceCompute = 0;

        final ActionTracker at = data.getActionTracker();
        if (at == null || at.getActions().size() < MIN_ACTIONS) return;

        final RingBuffer.OfLong actions = at.getActions();
        final int n = actions.size();

        // Extract action type ordinals (strip timing info — only type matters)
        final long[] types = new long[n];
        for (int i = 0; i < n; i++) {
            types[i] = ActionTracker.decodeActionOrdinal(actions.get(i));
        }

        int maxRepeat = 0;
        int flagWindow = 0;

        for (final int w : WINDOW_SIZES) {
            if (n < w) continue;

            final RollingHash rh = new RollingHash(w);
            final Map<Long, Integer> counts = new HashMap<>();

            // Seed with first w-1 elements
            for (int i = 0; i < w - 1; i++) rh.seed(types[i]);

            // Roll through the rest
            for (int i = w - 1; i < n; i++) {
                final long hash;
                if (i == w - 1) {
                    rh.seed(types[i]); // seed the last element of the first window
                    hash = rh.current();
                } else {
                    hash = rh.roll(types[i - w], types[i]);
                }

                final int count = counts.merge(hash, 1, Integer::sum);
                if (count > maxRepeat) {
                    maxRepeat  = count;
                    flagWindow = w;
                }
            }
        }

        if (maxRepeat > MAX_REPEAT_COUNT) {
            final MacroStateTracker mst = data.getMacroStateTracker();
            if (mst != null) mst.addEvidence(15, tick);

            flag(player, data, 1.0,
                String.format("sequence repeat: maxRepeat=%d window=%d n=%d",
                    maxRepeat, flagWindow, n),
                tick);
        }
    }
}
