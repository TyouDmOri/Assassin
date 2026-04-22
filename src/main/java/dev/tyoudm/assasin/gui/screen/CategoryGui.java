/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.gui.screen;

import dev.tyoudm.assasin.AssasinPlugin;
import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckRegistry;
import dev.tyoudm.assasin.gui.AssasinGui;
import dev.tyoudm.assasin.gui.component.GuiItem;
import dev.tyoudm.assasin.gui.component.PaginationBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Category GUI — shows all checks for a given {@link CheckCategory}, paginated.
 *
 * <p>4 rows × 9 cols (36 slots). 14 check slots per page.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CategoryGui extends AssasinGui {

    private static final int CHECKS_PER_PAGE = 14;
    private static final int[] CHECK_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25
    };

    private final AssasinPlugin plugin;
    private final Player        viewer;
    private final CheckCategory category;
    private final int           page;

    public CategoryGui(final AssasinPlugin plugin, final Player viewer,
                       final String categoryKey, final int page) {
        this.plugin    = plugin;
        this.viewer    = viewer;
        this.category  = resolveCategory(categoryKey);
        this.page      = page;
        buildInventory();
    }

    /** Convenience constructor — page 0. */
    public CategoryGui(final AssasinPlugin plugin, final Player viewer,
                       final String categoryKey) {
        this(plugin, viewer, categoryKey, 0);
    }

    @Override
    protected void buildInventory() {
        final CheckRegistry registry   = plugin.getServiceContainer().getCheckProcessor().getRegistry();
        final List<Check>   checks     = registry.getChecks(category);
        final int totalChecks = checks.size();
        final int totalPages  = Math.max(1, (int) Math.ceil((double) totalChecks / CHECKS_PER_PAGE));
        final int safePage    = Math.min(page, totalPages - 1);

        createInventory(4, Component.text("⚔ ASSASIN — " + category.name())
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();

        // Populate checks for this page
        final int start = safePage * CHECKS_PER_PAGE;
        final int end   = Math.min(start + CHECKS_PER_PAGE, totalChecks);
        for (int i = start; i < end; i++) {
            final Check check = checks.get(i);
            final int slot = CHECK_SLOTS[i - start];
            setItem(slot, buildCheckItem(check, safePage));
        }

        // Pagination (slots 28 = prev, 34 = next)
        final PaginationBar bar = new PaginationBar(safePage, totalPages, 28, 34,
            e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(),
                    new CategoryGui(plugin, (Player) e.getWhoClicked(),
                        category.name(), safePage - 1)),
            e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(),
                    new CategoryGui(plugin, (Player) e.getWhoClicked(),
                        category.name(), safePage + 1)));
        bar.apply(inventory);

        // Back button (slot 31)
        setItem(31, new GuiItem(Material.ARROW)
            .name("§7Back")
            .lore("§7Return to main menu")
            .action(e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(),
                    new MainGui(plugin, (Player) e.getWhoClicked()))));

        // Reset VLs for category (slot 33) — resets for all online players
        setItem(33, new GuiItem(Material.CLOCK)
            .name("§aReset VLs")
            .lore("§7Reset VLs for this category", "", "§eClick to reset")
            .action(e -> {
                final var pdm = plugin.getServiceContainer().getPlayerDataManager();
                pdm.all().forEach(pd ->
                    checks.forEach(c ->
                        pd.getViolationBuffer(c.getType(), c.getInfo().maxVl()).reset()));
                ((Player) e.getWhoClicked())
                    .sendMessage("§a[ASSASIN] VLs reset for " + category.name());
            }));
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private GuiItem buildCheckItem(final Check check, final int currentPage) {
        final boolean enabled = check.isEnabled();
        final Material mat    = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        final String   status = enabled ? "§aEnabled" : "§cDisabled";
        final String   toggle = enabled ? "§eClick to disable" : "§eClick to enable";

        return new GuiItem(mat)
            .name((enabled ? "§a" : "§c") + check.getCheckName())
            .lore(
                "§7Severity: §f" + check.getInfo().severity().name(),
                "§7MaxVL: §f" + check.getInfo().maxVl(),
                "§7" + check.getInfo().description(),
                "",
                "§7Status: " + status,
                toggle
            )
            .action(e -> {
                check.setEnabled(!check.isEnabled());
                plugin.getServiceContainer().getGuiManager()
                    .open((Player) e.getWhoClicked(),
                        new CategoryGui(plugin, (Player) e.getWhoClicked(),
                            category.name(), currentPage));
            });
    }

    /**
     * Maps the string key used in {@link MainGui} to a {@link CheckCategory}.
     * Falls back to {@link CheckCategory#COMBAT} if unknown.
     */
    private static CheckCategory resolveCategory(final String key) {
        return switch (key.toUpperCase()) {
            case "MOVEMENT"  -> CheckCategory.MOVEMENT;
            case "MOUNT"     -> CheckCategory.MOUNT;
            case "WORLD", "SCAFFOLD" -> CheckCategory.WORLD;
            case "PLAYER"    -> CheckCategory.PLAYER;
            case "MACRO"     -> CheckCategory.MACRO;
            case "MISC"      -> CheckCategory.MISC;
            default          -> CheckCategory.COMBAT; // KILL_AURA, AIM, COMBAT
        };
    }
}
