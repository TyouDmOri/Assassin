/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.mitigation.strategy;

import dev.tyoudm.assasin.mitigation.MitigationContext;
import dev.tyoudm.assasin.mitigation.MitigationPriority;
import dev.tyoudm.assasin.mitigation.MitigationResult;
import dev.tyoudm.assasin.mitigation.MitigationStrategy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Kicks the player with a generic message that does not reveal detection.
 *
 * <p>The kick message is intentionally vague to avoid giving cheaters
 * information about which check triggered the kick. The actual reason
 * is logged internally.
 *
 * <p>Must be scheduled back to the main thread — {@link MitigationPriority#DEFERRED}
 * ensures the {@link MitigationEngine} handles this correctly.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class KickStrategy implements MitigationStrategy {

    /** Default generic kick message. */
    private static final Component DEFAULT_MESSAGE = Component.text(
        "You have been disconnected from the server.", NamedTextColor.RED);

    private final Component message;

    public KickStrategy(final Component message) {
        this.message = message;
    }

    /** Kick with the default generic message. */
    public static KickStrategy generic() {
        return new KickStrategy(DEFAULT_MESSAGE);
    }

    /** Kick with a custom message. */
    public static KickStrategy withMessage(final Component message) {
        return new KickStrategy(message);
    }

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        try {
            if (!ctx.player().isOnline()) {
                return MitigationResult.noop();
            }
            ctx.player().kick(message);
            return MitigationResult.ok("Player kicked: " + ctx.checkName()
                + " VL=" + String.format("%.1f", ctx.violationLevel()));
        } catch (final Exception ex) {
            return MitigationResult.failure("KickStrategy failed: " + ex.getMessage());
        }
    }

    @Override public MitigationPriority priority() { return MitigationPriority.DEFERRED; }
    @Override public String name() { return "Kick"; }
}
