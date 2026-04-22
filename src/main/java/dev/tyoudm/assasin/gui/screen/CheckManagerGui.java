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
 * Check Manager GUI — 6 rows × 9 cols (54 slots), paginated.
 *
 * <p>Shows all registered checks with toggle actions. 28 checks per page.
 * Requires {@code assasin.admin} permission.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CheckManagerGui extends AssasinGui {

    private static final int CHECKS_PER_PAGE = 28;
    private static final int[] CHECK_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };

    private final AssasinPlugin plugin;
    private final Player        viewer;
    private final int           page;

    public CheckManagerGui(final AssasinPlugin plugin, final Player viewer, final int page) {
        this.plugin  = plugin;
        this.viewer  = viewer;
        this.page    = page;
        buildInventory();
    }

    @Override
    protected void buildInventory() {
        final CheckRegistry registry = plugin.getServiceContainer().getCheckProcessor().getRegistry();
        final List<Check>   allChecks = registry.getAllChecks();
        final int totalChecks = allChecks.size();
        final int totalPages  = Math.max(1, (int) Math.ceil((double) totalChecks / CHECKS_PER_PAGE));
        final int safePage    = Math.min(page, totalPages - 1);

        createInventory(6, Component.text(
            String.format("⚙ ASSASIN — Check Manager [%d/%d]", safePage + 1, totalPages))
            .decoration(TextDecoration.ITALIC, false));
        applyBorder();

        // Populate checks for this page
        final int start = safePage * CHECKS_PER_PAGE;
        final int end   = Math.min(start + CHECKS_PER_PAGE, totalChecks);
        for (int i = start; i < end; i++) {
            final Check check = allChecks.get(i);
            final int slot = CHECK_SLOTS[i - start];
            setItem(slot, buildCheckItem(check));
        }

        // Pagination
        final PaginationBar bar = new PaginationBar(safePage, totalPages, 46, 52,
            e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(),
                    new CheckManagerGui(plugin, (Player) e.getWhoClicked(), safePage - 1)),
            e -> plugin.getServiceContainer().getGuiManager()
                .open((Player) e.getWhoClicked(),
                    new CheckManagerGui(plugin, (Player) e.getWhoClicked(), safePage + 1)));
        bar.apply(this);

        // Reset ALL VLs (slot 53) — resets for all online players
        setItem(53, new GuiItem(Material.BARRIER)
            .name("§cReset ALL VLs")
            .lore("§7Reset VLs for all online players", "", "§cClick to reset")
            .action(e -> {
                final var pdm = plugin.getServiceContainer().getPlayerDataManager();
                pdm.all().forEach(pd ->
                    registry.getAllChecks().forEach(c ->
                        pd.getViolationBuffer(c.getType(), c.getInfo().maxVl()).reset()));
                ((Player) e.getWhoClicked()).sendMessage("§a[ASSASIN] All VLs reset.");
            }));
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private GuiItem buildCheckItem(final Check check) {
        final boolean enabled = check.isEnabled();
        final Material mat    = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        final String   status = enabled ? "§aEnabled" : "§cDisabled";
        final String   toggle = enabled ? "§eClick to disable" : "§eClick to enable";

        return new GuiItem(mat)
            .name((enabled ? "§a" : "§c") + check.getCheckName())
            .lore(
                "§7Category: §f" + check.getCategory().name(),
                "§7Severity: §f" + check.getInfo().severity().name(),
                "§7MaxVL: §f" + check.getInfo().maxVl(),
                "",
                "§7Status: " + status,
                toggle
            )
            .action(e -> {
                check.setEnabled(!check.isEnabled());
                // Rebuild to reflect new state
                plugin.getServiceContainer().getGuiManager()
                    .open((Player) e.getWhoClicked(),
                        new CheckManagerGui(plugin, (Player) e.getWhoClicked(), page));
            });
    }
}
