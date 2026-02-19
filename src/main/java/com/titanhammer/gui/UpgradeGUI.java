package com.titanhammer.gui;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.models.Skill;
import com.titanhammer.models.Skill.SkillType;
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

public class UpgradeGUI {

    public static final String TITLE_EN = "§8§l✦ §6§lUpgrades §8§l✦";
    public static final String TITLE_AR = "§8§l✦ §6§lالترقيات §8§l✦";

    public static void open(Player player, TitanHammerPro plugin) {
        String lang = plugin.getMessageManager().getPlayerLanguage(player);
        boolean isAr = lang.equals("ar");
        String title = isAr ? TITLE_AR : TITLE_EN;

        Inventory inv = Bukkit.createInventory(null, 54, title);
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);

        // Border
        ItemStack border = GUIUtils.createFiller(Material.ORANGE_STAINED_GLASS_PANE);
        for (int slot : GUIUtils.BORDER_SLOTS_54) {
            inv.setItem(slot, border);
        }

        // Hammer stats overview - slot 4
        inv.setItem(4, createStatsItem(profile, plugin, player, isAr));

        // Active skills display
        int[] skillSlots = {19, 20, 21, 22, 23, 24, 25};
        int slotIndex = 0;

        for (Skill skill : plugin.getSkillManager().getAllSkills()) {
            int level = profile.getSkillLevel(skill.getId());
            if (level > 0 && slotIndex < skillSlots.length) {
                inv.setItem(skillSlots[slotIndex], createActiveSkillItem(skill, level, isAr));
                slotIndex++;
            }
        }

        // Fill remaining skill slots
        for (int i = slotIndex; i < skillSlots.length; i++) {
            inv.setItem(skillSlots[i], GUIUtils.createItem(Material.GRAY_STAINED_GLASS_PANE,
                    isAr ? "&8فارغ" : "&8Empty",
                    isAr ? "&7لا توجد مهارة مفعّلة" : "&7No active skill"));
        }

        // Quick upgrade buttons
        // Damage section
        inv.setItem(29, createQuickUpgradeButton("damage_boost", plugin, player, isAr));
        inv.setItem(30, createQuickUpgradeButton("xp_boost", plugin, player, isAr));
        inv.setItem(31, createQuickUpgradeButton("fortune_boost", plugin, player, isAr));
        inv.setItem(32, createQuickUpgradeButton("speed_boost", plugin, player, isAr));
        inv.setItem(33, createQuickUpgradeButton("life_steal", plugin, player, isAr));

        // Total stats summary - slot 40
        inv.setItem(40, createTotalStatsItem(profile, plugin, player, isAr));

        // Back button
        inv.setItem(49, GUIUtils.createItem(Material.ARROW,
                isAr ? "&c&lرجوع" : "&c&lBack",
                isAr ? "&7العودة إلى القائمة الرئيسية" : "&7Return to main menu"));

        player.openInventory(inv);
        plugin.getEffectsManager().playGUIOpen(player);
    }

    private static ItemStack createStatsItem(PlayerProfile profile, TitanHammerPro plugin,
                                              Player player, boolean isAr) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (isAr) {
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(c("&7⚔ المستوى: &e" + profile.getLevel()));
            lore.add(c("&7✦ إجمالي الخبرة: &b" + String.format("%.0f", profile.getTotalXp())));
            lore.add(c("&7🔧 المهارات المفعّلة: &a" + countActiveSkills(profile, plugin)));
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        } else {
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(c("&7⚔ Level: &e" + profile.getLevel()));
            lore.add(c("&7✦ Total XP: &b" + String.format("%.0f", profile.getTotalXp())));
            lore.add(c("&7🔧 Active Skills: &a" + countActiveSkills(profile, plugin)));
            lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        }

        return GUIUtils.createItem(Material.NETHERITE_AXE,
                isAr ? "&c&l⚡ إحصائيات المطرقة" : "&c&l⚡ Hammer Stats",
                lore.toArray(new String[0]));
    }

    private static ItemStack createActiveSkillItem(Skill skill, int level, boolean isAr) {
        String name = isAr ? skill.getDisplayNameAr() : skill.getDisplayName();
        return GUIUtils.createGlowItem(skill.getIcon(),
                "&a" + name + " &7[Lv." + level + "/" + skill.getMaxLevel() + "]",
                "",
                c("&7" + (isAr ? "التأثير: " : "Effect: ") + "&a+" +
                        String.format("%.0f", skill.getEffectValue(level) * 100) + "%"));
    }

    private static ItemStack createQuickUpgradeButton(String skillId, TitanHammerPro plugin,
                                                       Player player, boolean isAr) {
        Skill skill = plugin.getSkillManager().getSkill(skillId);
        if (skill == null) return GUIUtils.createFiller(Material.GRAY_STAINED_GLASS_PANE);

        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        int level = profile.getSkillLevel(skillId);
        String name = isAr ? skill.getDisplayNameAr() : skill.getDisplayName();

        if (level >= skill.getMaxLevel()) {
            return GUIUtils.createGlowItem(Material.LIME_DYE,
                    "&a&l" + name + " &8[MAX]",
                    isAr ? "&7أقصى مستوى" : "&7Maximum level reached");
        }

        boolean canUpgrade = plugin.getSkillManager().canUpgrade(player, skill);
        double cost = skill.getCostForLevel(level + 1);
        String formattedCost = plugin.getEconomyManager().formatCurrency(cost, skill.getEconomyType());

        return GUIUtils.createItem(canUpgrade ? Material.LIME_DYE : Material.RED_DYE,
                (canUpgrade ? "&a" : "&c") + "&l⬆ " + name,
                "",
                c("&7Lv." + level + " &8→ &eLv." + (level + 1)),
                c((isAr ? "&7التكلفة: " : "&7Cost: ") + (canUpgrade ? "&a" : "&c") + formattedCost),
                "",
                c(canUpgrade ?
                        (isAr ? "&e&lانقر للترقية!" : "&e&lClick to upgrade!") :
                        (isAr ? "&c&lلا يمكن الترقية" : "&c&lCannot upgrade")));
    }

    private static ItemStack createTotalStatsItem(PlayerProfile profile, TitanHammerPro plugin,
                                                   Player player, boolean isAr) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        double dmgBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.DAMAGE_BOOST);
        double xpBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.XP_BOOST);
        double fortuneBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.FORTUNE_BOOST);
        double speedBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.SPEED_BOOST);
        double lifeSteal = plugin.getSkillManager().getSkillEffect(player, SkillType.LIFE_STEAL);

        if (isAr) {
            lore.add(c("&c⚔ ضرر إضافي: &f+" + String.format("%.0f", dmgBoost * 100) + "%"));
            lore.add(c("&b✦ خبرة إضافية: &f+" + String.format("%.0f", xpBoost * 100) + "%"));
            lore.add(c("&a💎 حظ إضافي: &f+" + String.format("%.0f", fortuneBoost * 100) + "%"));
            lore.add(c("&e⚡ سرعة إضافية: &f+" + String.format("%.0f", speedBoost * 100) + "%"));
            lore.add(c("&d❤ سرقة حياة: &f+" + String.format("%.0f", lifeSteal * 100) + "%"));
        } else {
            lore.add(c("&c⚔ Damage Bonus: &f+" + String.format("%.0f", dmgBoost * 100) + "%"));
            lore.add(c("&b✦ XP Bonus: &f+" + String.format("%.0f", xpBoost * 100) + "%"));
            lore.add(c("&a💎 Fortune Bonus: &f+" + String.format("%.0f", fortuneBoost * 100) + "%"));
            lore.add(c("&e⚡ Speed Bonus: &f+" + String.format("%.0f", speedBoost * 100) + "%"));
            lore.add(c("&d❤ Life Steal: &f+" + String.format("%.0f", lifeSteal * 100) + "%"));
        }

        lore.add(c("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        return GUIUtils.createItem(Material.NETHER_STAR,
                isAr ? "&e&l📊 إجمالي الإحصائيات" : "&e&l📊 Total Stats",
                lore.toArray(new String[0]));
    }

    private static int countActiveSkills(PlayerProfile profile, TitanHammerPro plugin) {
        int count = 0;
        for (Skill skill : plugin.getSkillManager().getAllSkills()) {
            if (profile.getSkillLevel(skill.getId()) > 0) count++;
        }
        return count;
    }

    // Map upgrade slots to skill IDs
    public static String getSkillIdAtSlot(int slot) {
        return switch (slot) {
            case 29 -> "damage_boost";
            case 30 -> "xp_boost";
            case 31 -> "fortune_boost";
            case 32 -> "speed_boost";
            case 33 -> "life_steal";
            default -> null;
        };
    }

    private static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
