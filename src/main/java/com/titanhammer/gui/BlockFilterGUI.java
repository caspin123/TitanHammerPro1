package com.titanhammer.gui;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockFilterGUI {

    public static final String TITLE_EN = "§8§l✦ §b§lBlock Filter §8§l✦";
    public static final String TITLE_AR = "§8§l✦ §b§lفلتر الكتل §8§l✦";

    // Page size (excluding borders)
    private static final int ITEMS_PER_PAGE = 28; // 4 rows of 7

    public static void open(Player player, TitanHammerPro plugin, int page) {
        String lang = plugin.getMessageManager().getPlayerLanguage(player);
        boolean isAr = lang.equals("ar");
        String title = (isAr ? TITLE_AR : TITLE_EN) + " §8P." + (page + 1);

        Inventory inv = Bukkit.createInventory(null, 54, title);
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);

        // Border
        ItemStack border = GUIUtils.createFiller(Material.CYAN_STAINED_GLASS_PANE);
        for (int slot : GUIUtils.BORDER_SLOTS_54) {
            inv.setItem(slot, border);
        }

        // Get filterable blocks
        List<Material> blocks = plugin.getBlockFilterManager().getFilterableBlocks();
        int totalPages = (int) Math.ceil((double) blocks.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;

        // Content slots (inside the border)
        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        Set<Material> collectBlocks = profile.getAutoCollectBlocks();
        Set<Material> deleteBlocks = profile.getAutoDeleteBlocks();

        for (int i = 0; i < contentSlots.length; i++) {
            int blockIndex = startIndex + i;
            if (blockIndex >= blocks.size()) break;

            Material mat = blocks.get(blockIndex);
            boolean isCollecting = collectBlocks.contains(mat);
            boolean isDeleting = deleteBlocks.contains(mat);

            List<String> lore = new ArrayList<>();
            lore.add("");

            if (isCollecting) {
                lore.add(c("&a&l✔ " + (isAr ? "جمع تلقائي" : "Auto Collect")));
            } else if (isDeleting) {
                lore.add(c("&c&l✘ " + (isAr ? "حذف تلقائي" : "Auto Delete")));
            } else {
                lore.add(c("&7○ " + (isAr ? "عادي" : "Normal")));
            }

            lore.add("");
            lore.add(c(isAr ? "&eنقرة يسار: &7جمع تلقائي" : "&eLeft Click: &7Auto Collect"));
            lore.add(c(isAr ? "&eنقرة يمين: &7حذف تلقائي" : "&eRight Click: &7Auto Delete"));
            lore.add(c(isAr ? "&eShift+نقر: &7إزالة" : "&eShift+Click: &7Remove"));

            String blockName = formatBlockName(mat);
            String prefix = isCollecting ? "&a" : isDeleting ? "&c" : "&f";

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(c(prefix + "&l" + blockName));
            List<String> finalLore = new ArrayList<>();
            for (String l : lore) finalLore.add(c(l));
            meta.setLore(finalLore);
            item.setItemMeta(meta);

            inv.setItem(contentSlots[i], item);
        }

        // Mode display - slot 4
        String modeName = plugin.getItemManager().getFilterModeName(profile.getFilterMode(), isAr);
        Material modeIcon = switch (profile.getFilterMode()) {
            case AUTO_COLLECT -> Material.LIME_STAINED_GLASS_PANE;
            case AUTO_DELETE -> Material.RED_STAINED_GLASS_PANE;
            case DISABLED -> Material.GRAY_STAINED_GLASS_PANE;
        };
        inv.setItem(4, GUIUtils.createItem(modeIcon,
                (isAr ? "&e&lالوضع: " : "&e&lMode: ") + "&a" + modeName,
                isAr ? "&7انقر لتبديل الوضع" : "&7Click to cycle mode"));

        // Previous page - slot 45
        if (page > 0) {
            inv.setItem(45, GUIUtils.createItem(Material.ARROW,
                    isAr ? "&e&l◄ الصفحة السابقة" : "&e&l◄ Previous Page"));
        }

        // Page info - slot 49
        inv.setItem(49, GUIUtils.createItem(Material.PAPER,
                "&7" + (isAr ? "صفحة " : "Page ") + (page + 1) + "/" + Math.max(totalPages, 1)));

        // Next page - slot 53
        if (page < totalPages - 1) {
            inv.setItem(53, GUIUtils.createItem(Material.ARROW,
                    isAr ? "&e&lالصفحة التالية ►" : "&e&lNext Page ►"));
        }

        // Back button - slot 48
        inv.setItem(48, GUIUtils.createItem(Material.BARRIER,
                isAr ? "&c&lرجوع" : "&c&lBack",
                isAr ? "&7العودة إلى القائمة الرئيسية" : "&7Return to main menu"));

        // Clear all - slot 50
        inv.setItem(50, GUIUtils.createItem(Material.TNT,
                isAr ? "&c&lمسح الكل" : "&c&lClear All",
                isAr ? "&7إزالة جميع الفلاتر" : "&7Remove all filters"));

        player.openInventory(inv);
        plugin.getEffectsManager().playGUIOpen(player);
    }

    public static int getPageFromTitle(String title) {
        try {
            int idx = title.lastIndexOf("P.");
            if (idx >= 0) {
                return Integer.parseInt(title.substring(idx + 2).trim()) - 1;
            }
        } catch (NumberFormatException ignored) {}
        return 0;
    }

    public static Material getBlockAtSlot(int slot, int page, TitanHammerPro plugin) {
        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        for (int i = 0; i < contentSlots.length; i++) {
            if (contentSlots[i] == slot) {
                int blockIndex = page * ITEMS_PER_PAGE + i;
                List<Material> blocks = plugin.getBlockFilterManager().getFilterableBlocks();
                if (blockIndex < blocks.size()) {
                    return blocks.get(blockIndex);
                }
                return null;
            }
        }
        return null;
    }

    public static boolean isContentSlot(int slot) {
        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
        for (int s : contentSlots) {
            if (s == slot) return true;
        }
        return false;
    }

    private static String formatBlockName(Material mat) {
        String name = mat.name().replace("_", " ");
        StringBuilder result = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!result.isEmpty()) result.append(" ");
            result.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

    private static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
