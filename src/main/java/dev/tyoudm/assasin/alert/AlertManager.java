/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.alert;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.storage.StorageProvider;
import dev.tyoudm.assasin.storage.model.AlertLog;
import dev.tyoudm.assasin.storage.model.AlertPreference;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central alert manager — dispatches violation alerts to all eligible
 * staff members across all configured channels.
 *
 * <h2>Channel delivery</h2>
 * <ul>
 *   <li><b>CHAT</b> — rich component with hover + click</li>
 *   <li><b>ACTION_BAR</b> — compact one-liner</li>
 *   <li><b>TITLE</b> — brief title + subtitle</li>
 *   <li><b>SOUND</b> — {@code UI_BUTTON_CLICK} at 0.5 volume</li>
 *   <li><b>DISCORD</b> — async embed via {@link DiscordWebhook}</li>
 * </ul>
 *
 * <h2>Preference filtering</h2>
 * Each staff member's preferences are cached in memory (loaded on join,
 * invalidated on toggle). A staff member only receives alerts for checks
 * they have enabled.
 *
 * <h2>Permission</h2>
 * Staff members must have {@code assasin.alerts} to receive alerts.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class AlertManager {

    /** Permission required to receive alerts. */
    public static final String ALERTS_PERMISSION = "assasin.alerts";

    // ─── Dependencies ─────────────────────────────────────────────────────────

    private final AssasinPlugin    plugin;
    private final StorageProvider  storage;
    private final DiscordWebhook   discord;

    // ─── Preference cache ─────────────────────────────────────────────────────

    /**
     * UUID → list of preferences.
     * Loaded on join, invalidated when the player toggles a preference.
     */
    private final Map<UUID, List<AlertPreference>> prefCache = new ConcurrentHashMap<>();

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates the alert manager.
     *
     * @param plugin  the owning plugin
     * @param storage the storage provider (for logging alerts + loading prefs)
     * @param discord the Discord webhook sender
     */
    public AlertManager(final AssasinPlugin plugin, final StorageProvider storage,
                        final DiscordWebhook discord) {
        this.plugin  = plugin;
        this.storage = storage;
        this.discord = discord;
    }

    // ─── Dispatch ─────────────────────────────────────────────────────────────

    /**
     * Dispatches an alert to all eligible staff members and logs it to storage.
     *
     * <p>Must be called from the main thread (Bukkit API access).
     *
     * @param ctx the alert context
     */
    public void dispatch(final AlertContext ctx) {
        // Log to storage (async)
        storage.insertAlertLog(AlertLog.broadcast(
            UUID.fromString(ctx.playerUuid()),
            ctx.checkName(),
            ctx.violationLevel(),
            ctx.timestampMs()
        ));

        // Discord (async)
        discord.send(ctx);

        // Deliver to online staff
        for (final Player staff : plugin.getServer().getOnlinePlayers()) {
            if (!staff.hasPermission(ALERTS_PERMISSION)) continue;
            deliverToStaff(staff, ctx);
        }
    }

    // ─── Preference management ────────────────────────────────────────────────

    /**
     * Loads and caches alert preferences for the given staff member.
     * Call on player join.
     *
     * @param staffUuid the staff UUID
     */
    public void loadPreferences(final UUID staffUuid) {
        storage.loadAlertPreferences(staffUuid).thenAccept(prefs -> {
            if (prefs.isEmpty()) {
                // Insert default preference
                final AlertPreference def = AlertPreference.defaultFor(staffUuid);
                storage.saveAlertPreference(def);
                prefCache.put(staffUuid, List.of(def));
            } else {
                prefCache.put(staffUuid, prefs);
            }
        });
    }

    /**
     * Removes cached preferences for the given staff member.
     * Call on player quit.
     *
     * @param staffUuid the staff UUID
     */
    public void unloadPreferences(final UUID staffUuid) {
        prefCache.remove(staffUuid);
    }

    /**
     * Toggles alerts on/off for the given staff member and check.
     * Persists the change to storage.
     *
     * @param staffUuid the staff UUID
     * @param checkName the check name, or {@code "*"} for all
     * @param enabled   the new enabled state
     * @param channels  the new channel bitmask
     */
    public void setPreference(final UUID staffUuid, final String checkName,
                              final boolean enabled, final int channels) {
        final AlertPreference pref = new AlertPreference(staffUuid, checkName, enabled, channels);
        storage.saveAlertPreference(pref);
        // Invalidate cache — will be reloaded on next dispatch
        prefCache.remove(staffUuid);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void deliverToStaff(final Player staff, final AlertContext ctx) {
        final List<AlertPreference> prefs = prefCache.get(staff.getUniqueId());

        // Determine effective preference for this check
        final AlertPreference pref = resolvePreference(prefs, ctx.checkName());
        if (pref == null || !pref.enabled()) return;

        final int channels = pref.channelBitmask();

        // CHAT
        if ((channels & AlertPreference.CHANNEL_CHAT) != 0) {
            staff.sendMessage(AlertFormatter.buildChatAlert(ctx));
        }

        // ACTION_BAR
        if ((channels & AlertPreference.CHANNEL_ACTION_BAR) != 0) {
            staff.sendActionBar(AlertFormatter.buildActionBarAlert(ctx));
        }

        // TITLE
        if ((channels & AlertPreference.CHANNEL_TITLE) != 0) {
            staff.showTitle(Title.title(
                AlertFormatter.buildTitleAlert(ctx),
                AlertFormatter.buildSubtitleAlert(ctx),
                Title.Times.times(
                    Duration.ofMillis(200),
                    Duration.ofMillis(2000),
                    Duration.ofMillis(500)
                )
            ));
        }

        // SOUND
        if ((channels & AlertPreference.CHANNEL_SOUND) != 0) {
            staff.playSound(staff.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    /**
     * Resolves the most specific preference for the given check name.
     * Prefers an exact check-name match over the wildcard {@code "*"}.
     *
     * @param prefs     the staff member's preference list (may be null)
     * @param checkName the check name to resolve
     * @return the resolved preference, or {@code null} if none found
     */
    private static AlertPreference resolvePreference(
            final List<AlertPreference> prefs, final String checkName) {
        if (prefs == null || prefs.isEmpty()) {
            // Default: all channels enabled
            return new AlertPreference(null, "*", true, AlertPreference.DEFAULT_CHANNELS);
        }

        AlertPreference wildcard = null;
        for (final AlertPreference p : prefs) {
            if (p.checkName().equals(checkName)) return p;
            if ("*".equals(p.checkName())) wildcard = p;
        }
        return wildcard != null ? wildcard
            : new AlertPreference(null, "*", true, AlertPreference.DEFAULT_CHANNELS);
    }
}
