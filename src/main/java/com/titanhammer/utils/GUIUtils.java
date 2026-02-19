package com.titanhammer.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIUtils {

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(c(name));
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(c(line));
            }
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGlowItem(Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createProgressBar(String name, int current, int max, Material full, Material empty) {
        double percentage = max > 0 ? (double) current / max : 0;
        int filled = (int) (percentage * 10);

        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "&a█" : "&7░");
        }
        bar.append("&8]");

        return createItem(percentage >= 1.0 ? full : empty, name,
                bar.toString(),
                "&7" + current + "&8/&7" + max + " &8(" + String.format("%.0f", percentage * 100) + "%)");
    }

    public static String createProgressBarString(double current, double max) {
        double percentage = max > 0 ? current / max : 0;
        int filled = (int) (percentage * 20);

        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < 20; i++) {
            bar.append(i < filled ? "&a|" : "&7|");
        }
        bar.append("&8] &e" + String.format("%.0f", percentage * 100) + "%");
        return bar.toString();
    }

    public static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Filler patterns
    public static final int[] BORDER_SLOTS_54 = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 35,
            36, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
    };

    public static final int[] BORDER_SLOTS_27 = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
    };
}
