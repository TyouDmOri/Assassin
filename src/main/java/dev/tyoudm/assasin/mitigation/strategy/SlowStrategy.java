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
 * Applies a temporary Slowness effect to the player.
 *
 * <p>Used as a soft mitigation for minor speed violations where a setback
 * would be too disruptive. The effect duration and amplifier are configurable.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class SlowStrategy implements MitigationStrategy {

    /** Default slowness duration in ticks. */
    public static final int DEFAULT_DURATION  = 20;

    /** Default slowness amplifier (0 = Slowness I). */
    public static final int DEFAULT_AMPLIFIER = 0;

    private final int durationTicks;
    private final int amplifier;

    public SlowStrategy(final int durationTicks, final int amplifier) {
        this.durationTicks = durationTicks;
        this.amplifier     = amplifier;
    }

    /** Default slow (Slowness I for 20 ticks). */
    public static SlowStrategy defaultSlow() {
        return new SlowStrategy(DEFAULT_DURATION, DEFAULT_AMPLIFIER);
    }

    @Override
    public MitigationResult execute(final MitigationContext ctx) {
        try {
            ctx.player().addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, durationTicks, amplifier, false, false, false));
            return MitigationResult.ok(
                "Slowness " + (amplifier + 1) + " applied for " + durationTicks + "t");
        } catch (final Exception ex) {
            return MitigationResult.failure("SlowStrategy failed: " + ex.getMessage());
        }
    }

    @Override public MitigationPriority priority() { return MitigationPriority.NORMAL; }
    @Override public String name() { return "Slow"; }
}
