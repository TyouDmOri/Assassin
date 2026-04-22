/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A named mitigation profile defining VL-threshold → strategy cascades.
 *
 * <p>Each profile maps violation-level thresholds to ordered lists of
 * {@link MitigationStrategy} instances. When the {@link MitigationEngine}
 * processes a flag, it finds the highest threshold ≤ current VL and
 * executes the associated strategies in {@link MitigationPriority} order.
 *
 * <h2>Built-in profiles</h2>
 * <ul>
 *   <li>{@code soft}   — setback + alert (low VL)</li>
 *   <li>{@code medium} — setback + cancel + alert (mid VL)</li>
 *   <li>{@code hard}   — setback + cancel + freeze + kick (high VL)</li>
 *   <li>{@code macro}  — conservative: silent alert → cancel → kick</li>
 * </ul>
 * Profiles are loaded from {@code mitigation.yml} in FASE 18; until then
 * the defaults defined in {@link MitigationEngine} are used.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MitigationProfile {

    /** Profile name (e.g., "soft", "medium", "hard", "macro"). */
    private final String name;

    /**
     * VL threshold → strategies map.
     * NavigableMap allows efficient floor-key lookup.
     */
    private final NavigableMap<Double, List<MitigationStrategy>> cascades = new TreeMap<>();

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new empty profile.
     *
     * @param name the profile name
     */
    public MitigationProfile(final String name) {
        this.name = name;
    }

    // ─── Builder API ──────────────────────────────────────────────────────────

    /**
     * Adds a strategy to the cascade at the given VL threshold.
     *
     * <p>Multiple strategies at the same threshold are executed in
     * {@link MitigationPriority} order (ascending ordinal).
     *
     * @param vlThreshold the minimum VL required to trigger this strategy
     * @param strategy    the strategy to add
     * @return {@code this} for chaining
     */
    public MitigationProfile addStrategy(final double vlThreshold,
                                         final MitigationStrategy strategy) {
        cascades.computeIfAbsent(vlThreshold, k -> new ArrayList<>()).add(strategy);
        return this;
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    /**
     * Returns the strategies to execute for the given violation level.
     *
     * <p>Finds the highest threshold ≤ {@code vl} and returns its strategies
     * sorted by {@link MitigationPriority} (ascending ordinal = IMMEDIATE first).
     *
     * @param vl the current violation level
     * @return ordered list of strategies to execute (may be empty)
     */
    public List<MitigationStrategy> strategiesFor(final double vl) {
        final var entry = cascades.floorEntry(vl);
        if (entry == null) return Collections.emptyList();

        final List<MitigationStrategy> result = new ArrayList<>(entry.getValue());
        result.sort(Comparator.comparingInt(s -> s.priority().ordinal()));
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all strategies across all thresholds up to and including {@code vl},
     * sorted by priority. Used for "cumulative" cascade mode.
     *
     * @param vl the current violation level
     * @return ordered list of all applicable strategies
     */
    public List<MitigationStrategy> allStrategiesUpTo(final double vl) {
        final List<MitigationStrategy> result = new ArrayList<>();
        for (final var entry : cascades.headMap(vl, true).entrySet()) {
            result.addAll(entry.getValue());
        }
        result.sort(Comparator.comparingInt(s -> s.priority().ordinal()));
        return Collections.unmodifiableList(result);
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns the profile name. */
    public String getName() { return name; }

    /** Returns {@code true} if no cascades have been defined. */
    public boolean isEmpty() { return cascades.isEmpty(); }

    /** Returns the number of defined VL thresholds. */
    public int thresholdCount() { return cascades.size(); }
}
