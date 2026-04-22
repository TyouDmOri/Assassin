/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.storage;

import dev.tyoudm.assasin.storage.model.AlertLog;
import dev.tyoudm.assasin.storage.model.AlertPreference;
import dev.tyoudm.assasin.storage.model.MacroEvidence;
import dev.tyoudm.assasin.storage.model.MitigationLog;
import dev.tyoudm.assasin.storage.model.PlayerProfile;
import dev.tyoudm.assasin.storage.model.ViolationRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Storage provider contract for ASSASIN.
 *
 * <p>All methods return {@link CompletableFuture} to allow async execution
 * via the {@link dev.tyoudm.assasin.handler.async.AsyncProcessor}. Callers
 * must not block the main thread waiting for results.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>{@link #init()} — open connection pool, run migrations.</li>
 *   <li>Normal operation — read/write via async methods.</li>
 *   <li>{@link #close()} — flush pending writes, close pool.</li>
 * </ol>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public interface StorageProvider {

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    /**
     * Initializes the storage provider: opens the connection pool and
     * runs pending schema migrations.
     *
     * @throws Exception if initialization fails
     */
    void init() throws Exception;

    /**
     * Closes the storage provider, flushing any pending writes and
     * releasing all connections.
     */
    void close();

    // ─── Player profiles ──────────────────────────────────────────────────────

    /**
     * Loads the player profile for the given UUID, or {@code null} if not found.
     *
     * @param uuid the player UUID
     * @return future resolving to the profile, or {@code null}
     */
    @NotNull CompletableFuture<@Nullable PlayerProfile> loadProfile(@NotNull UUID uuid);

    /**
     * Saves (insert or update) a player profile.
     *
     * @param profile the profile to save
     * @return future that completes when the save is done
     */
    @NotNull CompletableFuture<Void> saveProfile(@NotNull PlayerProfile profile);

    // ─── Violations ───────────────────────────────────────────────────────────

    /**
     * Inserts a violation record and returns the generated row ID.
     *
     * @param record the violation to insert
     * @return future resolving to the generated ID
     */
    @NotNull CompletableFuture<Long> insertViolation(@NotNull ViolationRecord record);

    /**
     * Returns the last {@code limit} violations for the given player.
     *
     * @param uuid  the player UUID
     * @param limit maximum number of records to return
     * @return future resolving to the violation list (oldest first)
     */
    @NotNull CompletableFuture<List<ViolationRecord>> getViolations(
            @NotNull UUID uuid, int limit);

    /**
     * Returns the total violation count for the given player.
     *
     * @param uuid the player UUID
     * @return future resolving to the count
     */
    @NotNull CompletableFuture<Integer> countViolations(@NotNull UUID uuid);

    // ─── Mitigation log ───────────────────────────────────────────────────────

    /**
     * Inserts a mitigation log entry.
     *
     * @param log the log entry to insert
     * @return future that completes when the insert is done
     */
    @NotNull CompletableFuture<Void> insertMitigationLog(@NotNull MitigationLog log);

    // ─── Alerts ───────────────────────────────────────────────────────────────

    /**
     * Inserts an alert log entry.
     *
     * @param alert the alert to insert
     * @return future that completes when the insert is done
     */
    @NotNull CompletableFuture<Void> insertAlertLog(@NotNull AlertLog alert);

    /**
     * Loads alert preferences for the given staff member.
     *
     * @param staffUuid the staff UUID
     * @return future resolving to the preference list (may be empty)
     */
    @NotNull CompletableFuture<List<AlertPreference>> loadAlertPreferences(
            @NotNull UUID staffUuid);

    /**
     * Saves (insert or update) an alert preference.
     *
     * @param pref the preference to save
     * @return future that completes when the save is done
     */
    @NotNull CompletableFuture<Void> saveAlertPreference(@NotNull AlertPreference pref);

    // ─── Macro evidence ───────────────────────────────────────────────────────

    /**
     * Inserts or updates macro evidence for the given player and pattern hash.
     *
     * @param evidence the evidence to upsert
     * @return future that completes when the upsert is done
     */
    @NotNull CompletableFuture<Void> upsertMacroEvidence(@NotNull MacroEvidence evidence);

    /**
     * Returns all macro evidence records for the given player.
     *
     * @param uuid the player UUID
     * @return future resolving to the evidence list
     */
    @NotNull CompletableFuture<List<MacroEvidence>> getMacroEvidence(@NotNull UUID uuid);

    // ─── Maintenance ──────────────────────────────────────────────────────────

    /**
     * Deletes violation records older than {@code olderThanMs} milliseconds.
     * Used for periodic cleanup to prevent unbounded table growth.
     *
     * @param olderThanMs cutoff timestamp in ms
     * @return future resolving to the number of deleted rows
     */
    @NotNull CompletableFuture<Integer> purgeOldViolations(long olderThanMs);

    /**
     * Returns the backend type identifier (e.g., "sqlite", "mysql", "mariadb").
     *
     * @return backend type string
     */
    @NotNull String getType();
}
