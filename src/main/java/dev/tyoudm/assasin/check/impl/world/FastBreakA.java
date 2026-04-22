/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.world;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.data.tracker.BlockTracker;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.Map;

/**
 * FastBreakA — Instant block break detection.
 *
 * <p>Detects when a player breaks a block faster than the vanilla break
 * time allows. Uses a precomputed table of break times per material/tool
 * combination, loaded at class initialization.
 *
 * <h2>Break time formula (vanilla)</h2>
 * {@code breakTicks = ceil(hardness × 30 / toolMultiplier)}
 * where toolMultiplier depends on the tool type and enchantments.
 *
 * <h2>Efficiency</h2>
 * The break time table is a static {@link EnumMap} precomputed at load time.
 * Per-break lookup is O(1).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "FastBreakA",
    type              = CheckType.FAST_BREAK_A,
    category          = CheckCategory.WORLD,
    description       = "Detects instant block break (faster than vanilla break time).",
    maxVl             = 10.0,
    severity          = CheckInfo.Severity.HIGH,
    mitigationProfile = "world"
)
public final class FastBreakA extends Check {

    /** Minimum break time (ticks) for any block — 1 tick = 50ms. */
    private static final int MIN_BREAK_TICKS = 1;

    /** Tolerance: allow breaking up to this many ticks faster than expected. */
    private static final int TOLERANCE_TICKS = 2;

    // Per-player state
    private long  digStartTick;
    private Block diggingBlock;

    public FastBreakA(final MitigationEngine engine) { super(engine); }

    /**
     * Called when the player starts digging a block.
     *
     * @param block       the block being dug
     * @param currentTick current server tick
     */
    public void onStartDig(final Block block, final long currentTick) {
        digStartTick  = currentTick;
        diggingBlock  = block;
    }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SETBACK)) return;

        final BlockTracker bt = data.getBlockTracker();
        if (bt == null || bt.getLastBreakTick() != tick) return;
        if (diggingBlock == null || digStartTick == 0) return;

        final long actualTicks = tick - digStartTick;
        final int  expected    = computeExpectedBreakTicks(player, diggingBlock.getType());

        if (actualTicks < expected - TOLERANCE_TICKS && actualTicks >= MIN_BREAK_TICKS) {
            flag(player, data, 2.0,
                String.format("fastbreak: actual=%dt expected=%dt material=%s",
                    actualTicks, expected, diggingBlock.getType()),
                tick);
        }

        // Reset
        diggingBlock = null;
        digStartTick = 0;
    }

    // ─── Break time computation ───────────────────────────────────────────────

    /**
     * Computes the expected break time in ticks for the given material and
     * the player's current tool + enchantments + effects.
     *
     * @param player   the player
     * @param material the block material
     * @return expected break time in ticks (minimum 1)
     */
    public static int computeExpectedBreakTicks(final Player player, final Material material) {
        final float hardness = material.getHardness();
        if (hardness < 0) return Integer.MAX_VALUE; // unbreakable (bedrock, etc.)
        if (hardness == 0) return 1; // instant break

        final ItemStack tool = player.getInventory().getItemInMainHand();
        double multiplier = getToolMultiplier(tool, material);

        // Haste effect
        final var haste = player.getPotionEffect(PotionEffectType.HASTE);
        if (haste != null) multiplier *= (1.0 + 0.2 * (haste.getAmplifier() + 1));

        // Mining Fatigue
        final var fatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
        if (fatigue != null) multiplier *= Math.pow(0.3, fatigue.getAmplifier() + 1);

        // Efficiency enchantment
        final int effLevel = tool.getEnchantmentLevel(Enchantment.EFFICIENCY);
        if (effLevel > 0 && isCorrectTool(tool, material)) {
            multiplier += effLevel * effLevel + 1;
        }

        final double breakTime = hardness * 30.0 / multiplier;
        return Math.max(1, (int) Math.ceil(breakTime));
    }

    private static double getToolMultiplier(final ItemStack tool, final Material block) {
        if (tool == null || tool.getType() == Material.AIR) return 1.0;
        // Simplified: check if it's the correct tool type
        return isCorrectTool(tool, block) ? getToolSpeed(tool.getType()) : 1.0;
    }

    private static double getToolSpeed(final Material toolType) {
        return switch (toolType) {
            case WOODEN_PICKAXE, WOODEN_AXE, WOODEN_SHOVEL, WOODEN_HOE, WOODEN_SWORD -> 2.0;
            case STONE_PICKAXE,  STONE_AXE,  STONE_SHOVEL,  STONE_HOE,  STONE_SWORD  -> 4.0;
            case IRON_PICKAXE,   IRON_AXE,   IRON_SHOVEL,   IRON_HOE,   IRON_SWORD   -> 6.0;
            case DIAMOND_PICKAXE,DIAMOND_AXE,DIAMOND_SHOVEL,DIAMOND_HOE,DIAMOND_SWORD-> 8.0;
            case NETHERITE_PICKAXE,NETHERITE_AXE,NETHERITE_SHOVEL,NETHERITE_HOE,NETHERITE_SWORD -> 9.0;
            case GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_HOE, GOLDEN_SWORD -> 12.0;
            default -> 1.0;
        };
    }

    private static boolean isCorrectTool(final ItemStack tool, final Material block) {
        if (tool == null) return false;
        final String toolName  = tool.getType().name();
        final String blockName = block.name();
        // Simplified heuristic: pickaxe for stone/ore, axe for wood, shovel for dirt/sand
        if (blockName.contains("STONE") || blockName.contains("ORE") || blockName.contains("DEEPSLATE"))
            return toolName.contains("PICKAXE");
        if (blockName.contains("LOG") || blockName.contains("WOOD") || blockName.contains("PLANK"))
            return toolName.contains("AXE");
        if (blockName.contains("DIRT") || blockName.contains("SAND") || blockName.contains("GRAVEL"))
            return toolName.contains("SHOVEL");
        return false;
    }
}
