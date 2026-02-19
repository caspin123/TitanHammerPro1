package com.titanhammer.gui;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.models.Skill;
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

public class SkillTreeGUI {

    public static final String TITLE_EN = "§8§l✦ §d§lSkill Tree §8§l✦";
    public static final String TITLE_AR = "§8§l✦ §d§lشجرة المهارات §8§l✦";

    // Skill slot positions forming a tree layout
    private static final int[] TIER1_SLOTS = {10, 13, 16};       // damage, xp, fortune
    private static final int[] TIER2_SLOTS = {28, 31, 34};       // speed, haste, fire
    private static final int[] TIER3_SLOTS = {37, 39, 41, 43};   // area, smelt, lifesteal, drops

    public static void open(Player player, TitanHammerPro plugin) {
        String lang = plugin.getMessageManager().getPlayerLanguage(player);
        boolean isAr = lang.equals("ar");
        String title = isAr ? TITLE_AR : TITLE_EN;

        Inventory inv = Bukkit.createInventory(null, 54, title);
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);

        // Border
        ItemStack border = GUIUtils.createFiller(Material.PURPLE_STAINED_GLASS_PANE);
        for (int slot : GUIUtils.BORDER_SLOTS_54) {
            inv.setItem(slot, border);
        }

        // Connection indicators (glass between tiers)
        ItemStack connector = GUIUtils.createFiller(Material.MAGENTA_STAINED_GLASS_PANE);
        inv.setItem(19, connector); inv.setItem(22, connector); inv.setItem(25, connector);

        // Place skills
        List<Skill> allSkills = new ArrayList<>(plugin.getSkillManager().getAllSkills());

        // Tier 1
        String[] tier1Ids = {"damage_boost", "xp_boost", "fortune_boost"};
        for (int i = 0; i < tier1Ids.length && i < TIER1_SLOTS.length; i++) {
            Skill skill = plugin.getSkillManager().getSkill(tier1Ids[i]);
            if (skill != null) {
                inv.setItem(TIER1_SLOTS[i], createSkillItem(skill, profile, plugin, player, isAr));
            }
        }

        // Tier 2
        String[] tier2Ids = {"speed_boost", "haste_boost", "fire_aspect"};
        for (int i = 0; i < tier2Ids.length && i < TIER2_SLOTS.length; i++) {
            Skill skill = plugin.getSkillManager().getSkill(tier2Ids[i]);
            if (skill != null) {
                inv.setItem(TIER2_SLOTS[i], createSkillItem(skill, profile, plugin, player, isAr));
            }
        }

        // Tier 3
        String[] tier3Ids = {"area_break", "auto_smelt", "life_steal", "drop_multiplier"};
        for (int i = 0; i < tier3Ids.length && i < TIER3_SLOTS.length; i++) {
            Skill skill = plugin.getSkillManager().getSkill(tier3Ids[i]);
            if (skill != null) {
                inv.setItem(TIER3_SLOTS[i], createSkillItem(skill, profile, plugin, player, isAr));
            }
        }

        // Back button
        inv.setItem(49, GUIUtils.createItem(Material.ARROW,
                isAr ? "&c&lرجوع" : "&c&lBack",
                isAr ? "&7العودة إلى القائمة الرئيسية" : "&7Return to main menu"));

        // Info item
        inv.setItem(4, GUIUtils.createItem(Material.NETHER_STAR,
                isAr ? "&e&lمعلومات المهارات" : "&e&lSkill Info",
                "",
                isAr ? "&7انقر على مهارة لترقيتها" : "&7Click a skill to upgrade it",
                isAr ? "&7المهارات المقفلة تحتاج متطلبات أولية" : "&7Locked skills need prerequisites",
                "",
                isAr ? "&a● متاحة  &e● قابلة للترقية  &c● مقفلة" : "&a● Unlocked  &e● Upgradeable  &c● Locked"));

        player.openInventory(inv);
        plugin.getEffectsManager().playGUIOpen(player);
    }

    private static ItemStack createSkillItem(Skill skill, PlayerProfile profile,
                                              TitanHammerPro plugin, Player player, boolean isAr) {
        int currentLevel = profile.getSkillLevel(skill.getId());
        boolean unlocked = plugin.getSkillManager().canUnlock(player, skill);
        boolean maxed = currentLevel >= skill.getMaxLevel();

        Material icon = skill.getIcon();
        String name = isAr ? skill.getDisplayNameAr() : skill.getDisplayName();
        String desc = isAr ? skill.getDescriptionAr() : skill.getDescription();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(c("&7" + desc));
        lore.add("");

        // Level display
        lore.add(c((isAr ? "&7المستوى: " : "&7Level: ") + "&e" + currentLevel + "&7/&e" + skill.getMaxLevel()));

        // Progress bar
        lore.add(c("  " + GUIUtils.createProgressBarString(currentLevel, skill.getMaxLevel())));
        lore.add("");

        if (currentLevel > 0) {
            lore.add(c((isAr ? "&7التأثير الحالي: " : "&7Current Effect: ") +
                    "&a+" + String.format("%.0f", skill.getEffectValue(currentLevel) * 100) + "%"));
        }

        if (!unlocked) {
            // Locked - show prerequisites
            lore.add(c("&c&l✘ " + (isAr ? "مقفل" : "LOCKED")));
            lore.add("");
            lore.add(c(isAr ? "&7المتطلبات:" : "&7Requirements:"));
            for (String prereqId : skill.getPrerequisites()) {
                Skill prereq = plugin.getSkillManager().getSkill(prereqId);
                if (prereq != null) {
                    boolean met = profile.getSkillLevel(prereqId) > 0;
                    String prereqName = isAr ? prereq.getDisplayNameAr() : prereq.getDisplayName();
                    lore.add(c("  " + (met ? "&a✔ " : "&c✘ ") + prereqName));
                }
            }
            // Make it look locked
            ItemStack item = new ItemStack(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(c("&c&l" + name + " &8[" + (isAr ? "مقفل" : "LOCKED") + "]"));
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;

        } else if (maxed) {
            lore.add("");
            lore.add(c("&a&l✔ " + (isAr ? "أقصى مستوى!" : "MAX LEVEL!")));
            return GUIUtils.createGlowItem(icon, "&a&l" + name + " &8[MAX]", lore.toArray(new String[0]));

        } else {
            double cost = skill.getCostForLevel(currentLevel + 1);
            String formattedCost = plugin.getEconomyManager().formatCurrency(cost, skill.getEconomyType());
            boolean canAfford = plugin.getEconomyManager().has(player, cost, skill.getEconomyType());

            lore.add("");
            lore.add(c((isAr ? "&7التكلفة: " : "&7Cost: ") + (canAfford ? "&a" : "&c") + formattedCost));
            lore.add("");
            lore.add(c(canAfford ?
                    (isAr ? "&e&lانقر للترقية!" : "&e&lClick to upgrade!") :
                    (isAr ? "&c&lلا تملك ما يكفي!" : "&c&lNot enough funds!")));

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(c("&e&l" + name + " &7[Lv." + currentLevel + "]"));
            List<String> finalLore = new ArrayList<>();
            for (String l : lore) finalLore.add(c(l));
            meta.setLore(finalLore);
            item.setItemMeta(meta);
            return item;
        }
    }

    // Helper to get the skill at a specific slot
    public static String getSkillIdAtSlot(int slot) {
        String[] tier1 = {"damage_boost", "xp_boost", "fortune_boost"};
        String[] tier2 = {"speed_boost", "haste_boost", "fire_aspect"};
        String[] tier3 = {"area_break", "auto_smelt", "life_steal", "drop_multiplier"};

        for (int i = 0; i < TIER1_SLOTS.length; i++) {
            if (TIER1_SLOTS[i] == slot && i < tier1.length) return tier1[i];
        }
        for (int i = 0; i < TIER2_SLOTS.length; i++) {
            if (TIER2_SLOTS[i] == slot && i < tier2.length) return tier2[i];
        }
        for (int i = 0; i < TIER3_SLOTS.length; i++) {
            if (TIER3_SLOTS[i] == slot && i < tier3.length) return tier3[i];
        }
        return null;
    }

    private static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
