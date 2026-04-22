/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationContext;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import dev.tyoudm.assasin.mitigation.buffer.ViolationBuffer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base class for all ASSASIN checks.
 *
 * <p>Provides the common lifecycle, violation buffer, exemption helpers,
 * and flag/mitigate plumbing that every check needs. Concrete checks
 * extend this class and implement {@link #process(Player, PlayerData, long)}.
 *
 * <h2>Check lifecycle</h2>
 * <ol>
 *   <li>Packet/event handler calls {@link #handle(Player, PlayerData, long)}.</li>
 *   <li>{@code handle} runs the early-exit guard (disabled, exempt, setback).</li>
 *   <li>If all guards pass, {@link #process} is called.</li>
 *   <li>{@code process} calls {@link #flag(Player, PlayerData, double, String, long)}
 *       when a violation is detected.</li>
 *   <li>{@code flag} updates the {@link ViolationBuffer} and calls
 *       {@link MitigationEngine#mitigate} if the VL threshold is met.</li>
 * </ol>
 *
 * <h2>Early-exit pattern</h2>
 * Every check must return immediately if:
 * <ul>
 *   <li>The check is disabled ({@link #enabled} = false).</li>
 *   <li>The player is exempt for a relevant {@link ExemptType}.</li>
 *   <li>A setback barrier is open ({@link dev.tyoudm.assasin.latency.LatencyTracker#isSetbackBlocked}).</li>
 *   <li>The player is in creative/spectator ({@link ExemptType#GAMEMODE}).</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public abstract class Check {

    // ─── Metadata ─────────────────────────────────────────────────────────────

    /** The {@link CheckInfo} annotation on this class. */
    protected final CheckInfo info;

    /** Short name from {@link CheckInfo#name()}. */
    protected final String checkName;

    // ─── State ────────────────────────────────────────────────────────────────

    /** Whether this check is currently enabled. */
    protected volatile boolean enabled = true;

    // NOTE: ViolationBuffer is per-player — stored in PlayerData, not here.
    // Access via data.getViolationBuffer(info.type(), info.maxVl())

    // ─── Dependencies ─────────────────────────────────────────────────────────

    /** The mitigation engine — shared across all checks. */
    protected final MitigationEngine mitigationEngine;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new check instance.
     *
     * @param mitigationEngine the shared mitigation engine
     * @throws IllegalStateException if the subclass is not annotated with {@link CheckInfo}
     */
    protected Check(final MitigationEngine mitigationEngine) {
        this.mitigationEngine = mitigationEngine;
        this.info = getClass().getAnnotation(CheckInfo.class);
        if (info == null) {
            throw new IllegalStateException(
                getClass().getSimpleName() + " is missing @CheckInfo annotation.");
        }
        this.checkName       = info.name();
        // ViolationBuffer is per-player — no longer stored here
    }

    // ─── Handle ───────────────────────────────────────────────────────────────

    /**
     * Entry point called by packet/event handlers.
     *
     * <p>Runs early-exit guards then delegates to {@link #process}.
     *
     * @param player      the player to check
     * @param data        the player's data
     * @param currentTick the current server tick
     */
    public final void handle(final Player player, final PlayerData data, final long currentTick) {
        // ── Early exits ──────────────────────────────────────────────────────
        if (!enabled) return;

        final var exempt = data.getExemptManager();

        // Global suppression exempts (BYPASS only applies if explicitly set,
        // not inherited from OP — permission default is false in paper-plugin.yml)
        if (exempt.isExemptAny(currentTick,
                ExemptType.GAMEMODE,
                ExemptType.DEAD,
                ExemptType.STAFF_EXEMPT,
                ExemptType.RESPAWN,
                ExemptType.WORLD_CHANGE)) return;

        // Bypass: only if explicitly granted via a permissions plugin.
        // Check the attachment directly — OP attachments have a null plugin.
        final var bypassAttachment = player.getEffectivePermissions().stream()
            .filter(info -> info.getPermission().equalsIgnoreCase("assasin.bypass")
                && info.getValue()
                && info.getAttachment() != null
                && info.getAttachment().getPlugin() != null)
            .findFirst().orElse(null);
        if (bypassAttachment != null) return;

        // Setback barrier — suppress until client confirms teleport
        if (data.getLatencyTracker() != null
                && data.getLatencyTracker().isSetbackBlocked(currentTick)) return;

        // Decay this player's VL for this check each call
        data.getViolationBuffer(info.type(), info.maxVl()).decay();

        // Delegate to concrete check
        process(player, data, currentTick);
    }

    /**
     * Overload for packet-based checks that also carry the raw event.
     *
     * @param player      the player
     * @param data        the player's data
     * @param event       the triggering packet event
     * @param currentTick the current server tick
     */
    public void handle(final Player player, final PlayerData data,
                       final PacketReceiveEvent event, final long currentTick) {
        handle(player, data, currentTick); // default: ignore event
    }

    // ─── Process ──────────────────────────────────────────────────────────────

    /**
     * Concrete check logic. Called only when all early-exit guards pass.
     *
     * @param player      the player to check
     * @param data        the player's data
     * @param currentTick the current server tick
     */
    protected abstract void process(Player player, PlayerData data, long currentTick);

    // ─── Flag ─────────────────────────────────────────────────────────────────

    /**
     * Records a violation and triggers mitigation if the VL threshold is met.
     *
     * @param player      the offending player
     * @param data        the player's data
     * @param points      violation points to add
     * @param details     human-readable details for alerts/logs
     * @param currentTick the current server tick
     * @return the new violation level after adding points
     */
    protected double flag(final Player player, final PlayerData data,
                          final double points, final String details,
                          final long currentTick) {
        return flag(player, data, points, details, null, currentTick);
    }

    /**
     * Records a violation with an optional packet event for packet-cancel strategies.
     *
     * @param player      the offending player
     * @param data        the player's data
     * @param points      violation points to add
     * @param details     human-readable details
     * @param event       the triggering packet event (may be null)
     * @param currentTick the current server tick
     * @return the new violation level
     */
    protected double flag(final Player player, final PlayerData data,
                          final double points, final String details,
                          @Nullable final PacketReceiveEvent event,
                          final long currentTick) {
        final ViolationBuffer buf = data.getViolationBuffer(info.type(), info.maxVl());
        final double newVl = buf.flag(points, currentTick);

        // Debug logging
        CheckDebug.logFlag(checkName, player.getName(), details, newVl, points);

        final MitigationContext ctx = event != null
            ? MitigationContext.ofPacket(player, data, checkName, newVl, details,
                                         info.severity(), event, currentTick)
            : MitigationContext.of(player, data, checkName, newVl, details,
                                   info.severity(), currentTick);

        mitigationEngine.mitigate(ctx);
        return newVl;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the player is exempt for the given type.
     *
     * @param data        the player's data
     * @param type        the exemption type
     * @param currentTick the current server tick
     * @return {@code true} if exempt
     */
    protected boolean isExempt(final PlayerData data, final ExemptType type,
                                final long currentTick) {
        return data.getExemptManager().isExempt(type, currentTick);
    }

    /**
     * Returns {@code true} if the player is exempt for any of the given types.
     *
     * @param data        the player's data
     * @param currentTick the current server tick
     * @param types       the exemption types to check
     * @return {@code true} if exempt for any
     */
    protected boolean isExemptAny(final PlayerData data, final long currentTick,
                                   final ExemptType... types) {
        return data.getExemptManager().isExemptAny(currentTick, types);
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns the check's display name. */
    public String getCheckName()              { return checkName; }

    /** Returns the check's {@link CheckInfo} annotation. */
    public CheckInfo getInfo()                { return info; }

    /** Returns the check's {@link CheckCategory}. */
    public CheckCategory getCategory()        { return info.category(); }

    /** Returns the check's {@link CheckType}. */
    public CheckType getType()                { return info.type(); }

    /** Returns the current violation level. NOTE: this is 0 — VL is per-player in PlayerData. */
    public double getVl()                     { return 0.0; }

    /** Returns the violation buffer. NOTE: use data.getViolationBuffer() for per-player VL. */
    public ViolationBuffer getViolationBuffer() { return null; }

    /** Returns {@code true} if this check is enabled. */
    public boolean isEnabled()                { return enabled; }

    /** Enables or disables this check. */
    public void setEnabled(final boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return checkName + "[vl=" + String.format("%.2f", getVl()) + "]";
    }
}
