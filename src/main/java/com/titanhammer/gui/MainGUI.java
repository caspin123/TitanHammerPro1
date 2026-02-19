package com.titanhammer.gui;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.managers.MessageManager;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.utils.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainGUI {

    public static final String TITLE_EN = "§8§l✦ §c§lTitanHammer §8§l✦ §7Main Menu";
    public static final String TITLE_AR = "§8§l✦ §c§lمطرقة تايتن §8§l✦ §7القائمة الرئيسية";

    public static void open(Player player, TitanHammerPro plugin) {
        String lang = plugin.getMessageManager().getPlayerLanguage(player);
        boolean isAr = lang.equals("ar");
        String title = isAr ? TITLE_AR : TITLE_EN;

        Inventory inv = Bukkit.createInventory(null, 54, title);
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);

        // Border
        ItemStack border = GUIUtils.createFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int slot : GUIUtils.BORDER_SLOTS_54) {
            inv.setItem(slot, border);
        }

        // Player info head - slot 4
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(c("&e&l" + player.getName()));
        List<String> headLore = new ArrayList<>();
        if (isAr) {
            headLore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            headLore.add(c("&7⚔ المستوى: &e" + profile.getLevel() + "&7/&e" + plugin.getConfigManager().getMaxLevel()));
            headLore.add(c("&7✦ الخبرة: &b" + String.format("%.1f", profile.getXp()) + "&7/&b" + String.format("%.0f", profile.getXpForNextLevel())));
            headLore.add(c("  " + GUIUtils.createProgressBarString(profile.getXp(), profile.getXpForNextLevel())));
            headLore.add(c("&7💰 الرصيد: &a" + plugin.getEconomyManager().formatCurrency(
                    plugin.getEconomyManager().getBalance(player, plugin.getConfigManager().getDefaultEconomy()),
                    plugin.getConfigManager().getDefaultEconomy())));
            headLore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        } else {
            headLore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            headLore.add(c("&7⚔ Level: &e" + profile.getLevel() + "&7/&e" + plugin.getConfigManager().getMaxLevel()));
            headLore.add(c("&7✦ XP: &b" + String.format("%.1f", profile.getXp()) + "&7/&b" + String.format("%.0f", profile.getXpForNextLevel())));
            headLore.add(c("  " + GUIUtils.createProgressBarString(profile.getXp(), profile.getXpForNextLevel())));
            headLore.add(c("&7💰 Balance: &a" + plugin.getEconomyManager().formatCurrency(
                    plugin.getEconomyManager().getBalance(player, plugin.getConfigManager().getDefaultEconomy()),
                    plugin.getConfigManager().getDefaultEconomy())));
            headLore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        }
        skullMeta.setLore(headLore);
        head.setItemMeta(skullMeta);
        inv.setItem(4, head);

        // Skill Tree - slot 20
        inv.setItem(20, GUIUtils.createGlowItem(Material.ENCHANTING_TABLE,
                isAr ? "&d&lشجرة المهارات" : "&d&lSkill Tree",
                "",
                isAr ? "&7اعرض وطور مهاراتك" : "&7View and upgrade your skills",
                isAr ? "&7افتح القدرات القوية" : "&7Unlock powerful abilities",
                "",
                isAr ? "&eانقر للفتح!" : "&eClick to open!"));

        // Upgrades - slot 22
        inv.setItem(22, GUIUtils.createGlowItem(Material.ANVIL,
                isAr ? "&6&lالترقيات" : "&6&lUpgrades",
                "",
                isAr ? "&7رقِّ مطرقتك" : "&7Upgrade your hammer",
                isAr ? "&7حسِّن الإحصائيات والقدرات" : "&7Improve stats and abilities",
                "",
                isAr ? "&eانقر للفتح!" : "&eClick to open!"));

        // Block Filter - slot 24
        String filterModeName = plugin.getItemManager().getFilterModeName(profile.getFilterMode(), isAr);
        inv.setItem(24, GUIUtils.createGlowItem(Material.HOPPER,
                isAr ? "&b&lفلتر الكتل" : "&b&lBlock Filter",
                "",
                isAr ? "&7الوضع الحالي: &a" + filterModeName : "&7Current Mode: &a" + filterModeName,
                isAr ? "&7اختر كتل للجمع أو الحذف" : "&7Select blocks to collect or delete",
                "",
                isAr ? "&eانقر للفتح!" : "&eClick to open!"));

        // Toggle Filter Mode - slot 31
        Material filterIcon = switch (profile.getFilterMode()) {
            case AUTO_COLLECT -> Material.LIME_DYE;
            case AUTO_DELETE -> Material.RED_DYE;
            case DISABLED -> Material.GRAY_DYE;
        };
        inv.setItem(31, GUIUtils.createItem(filterIcon,
                isAr ? "&e&lتبديل وضع الفلتر" : "&e&lToggle Filter Mode",
                "",
                isAr ? "&7الوضع الحالي: &a" + filterModeName : "&7Current: &a" + filterModeName,
                "",
                isAr ? "&eانقر للتبديل!" : "&eClick to cycle!"));

        // Language Toggle - slot 49
        inv.setItem(49, GUIUtils.createItem(Material.BOOK,
                isAr ? "&a&lتغيير اللغة" : "&a&lChange Language",
                "",
                isAr ? "&7اللغة الحالية: &eالعربية" : "&7Current: &eEnglish",
                "",
                isAr ? "&eانقر للتبديل إلى English" : "&eClick to switch to العربية"));

        // Close button - slot 53 (already border, override)
        inv.setItem(49, GUIUtils.createItem(Material.BOOK,
                isAr ? "&a&lتغيير اللغة" : "&a&lChange Language",
                isAr ? "&7الحالية: &eالعربية | &eانقر للتبديل" : "&7Current: &eEnglish | &eClick to switch"));

        // Get Hammer - slot 40
        inv.setItem(40, GUIUtils.createGlowItem(Material.NETHERITE_AXE,
                isAr ? "&c&l⚡ احصل على المطرقة" : "&c&l⚡ Get Hammer",
                "",
                isAr ? "&7احصل على مطرقة تايتن الخاصة بك" : "&7Receive your TitanHammer",
                "",
                isAr ? "&eانقر للحصول عليها!" : "&eClick to receive!"));

        player.openInventory(inv);
        plugin.getEffectsManager().playGUIOpen(player);
    }

    private static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
