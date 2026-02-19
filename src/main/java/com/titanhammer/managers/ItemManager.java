package com.titanhammer.managers;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    private final TitanHammerPro plugin;
    private final NamespacedKey titanKey;
    private final NamespacedKey ownerKey;

    public ItemManager(TitanHammerPro plugin) {
        this.plugin = plugin;
        this.titanKey = new NamespacedKey(plugin, "titan_hammer");
        this.ownerKey = new NamespacedKey(plugin, "titan_owner");
    }

    public ItemStack createHammer(Player player) {
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();

        // Set display name
        String lang = plugin.getMessageManager().getPlayerLanguage(player);
        String name = lang.equals("ar") ?
                plugin.getConfigManager().getItemNameAr() :
                plugin.getConfigManager().getItemName();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        // Custom Model Data
        meta.setCustomModelData(plugin.getConfigManager().getCustomModelData());

        // Persistent data tags
        meta.getPersistentDataContainer().set(titanKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        // Lore
        updateLore(meta, player);

        // Item flags
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

        item.setItemMeta(meta);
        return item;
    }

    public void updateLore(ItemMeta meta, Player player) {
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        String lang = plugin.getMessageManager().getPlayerLanguage(player);
        boolean isAr = lang.equals("ar");

        List<String> lore = new ArrayList<>();
        lore.add("");

        if (isAr) {
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(c("&7⚔ المستوى: &e" + profile.getLevel()));
            lore.add(c("&7✦ الخبرة: &b" + String.format("%.1f", profile.getXp()) + "&7/&b" + String.format("%.0f", profile.getXpForNextLevel())));
            lore.add(c("&7⚡ الوضع: &a" + getFilterModeName(profile.getFilterMode(), true)));
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add("");
            lore.add(c("&eShift + Right Click &7لفتح القائمة"));
        } else {
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(c("&7⚔ Level: &e" + profile.getLevel()));
            lore.add(c("&7✦ XP: &b" + String.format("%.1f", profile.getXp()) + "&7/&b" + String.format("%.0f", profile.getXpForNextLevel())));
            lore.add(c("&7⚡ Mode: &a" + getFilterModeName(profile.getFilterMode(), false)));
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add("");
            lore.add(c("&eShift + Right Click &7to open menu"));
        }

        meta.setLore(lore);
    }

    public void refreshPlayerHammer(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isTitanHammer(item)) {
                ItemMeta meta = item.getItemMeta();
                updateLore(meta, player);
                item.setItemMeta(meta);
            }
        }
    }

    public boolean isTitanHammer(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(titanKey, PersistentDataType.BYTE);
    }

    public boolean isOwner(ItemStack item, Player player) {
        if (!isTitanHammer(item)) return false;
        String owner = item.getItemMeta().getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        return player.getUniqueId().toString().equals(owner);
    }

    public String getFilterModeName(PlayerProfile.FilterMode mode, boolean arabic) {
        if (arabic) {
            return switch (mode) {
                case AUTO_COLLECT -> "جمع تلقائي";
                case AUTO_DELETE -> "حذف تلقائي";
                case DISABLED -> "معطل";
            };
        }
        return switch (mode) {
            case AUTO_COLLECT -> "Auto Collect";
            case AUTO_DELETE -> "Auto Delete";
            case DISABLED -> "Disabled";
        };
    }

    public NamespacedKey getTitanKey() { return titanKey; }
    public NamespacedKey getOwnerKey() { return ownerKey; }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
