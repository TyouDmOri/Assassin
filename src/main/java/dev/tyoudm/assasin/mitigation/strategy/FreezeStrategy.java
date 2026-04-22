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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Temporarily freezes the player in place using Slowness 255 + Jump Boost -10.
 *
 * <p>Used for high-VL violations where the player needs to be stopped
 * immediately while evidence is gathered before a kick decision.
 * The freeze duration is short (default 3 seconds) to avoid griefing
 * legitimate players on false positives.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class FreezeStrategy implements MitigationStrategy {

    /** Default freeze duration in ticks (3 seconds). */
    public static final int DEFAULT_DURATION = 60;

    private final int durationTicks;

    public FreezeStrategy(final int durationTicks) {
        this.durationTicks = durationTicks;
    }

    /** Default freeze (3 seconds). */
    public static FreezeStrategy defaultFreeze() {
        return new FreezeStrategy(DEFAULT_DURATION);
    }

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        try {
            final var player = ctx.player();
            // Slowness 255 effectively stops horizontal movement
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, durationTicks, 254, false, false, false));
            // Jump Boost -10 prevents jumping (negative amplifier)
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP_BOOST, durationTicks, -10, false, false, false));
            return MitigationResult.ok("Player frozen for " + durationTicks + "t");
        } catch (final Exception ex) {
            return MitigationResult.failure("FreezeStrategy failed: " + ex.getMessage());
        }
    }

    @Override public MitigationPriority priority() { return MitigationPriority.HIGH; }
    @Override public String name() { return "Freeze"; }
}
