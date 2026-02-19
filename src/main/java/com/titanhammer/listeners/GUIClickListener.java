package com.titanhammer.listeners;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.gui.*;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.models.Skill;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

public class GUIClickListener implements Listener {

    private final TitanHammerPro plugin;

    public GUIClickListener(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        // Check if it's one of our GUIs
        if (isMainGUI(title)) {
            handleMainGUI(event, player);
        } else if (isSkillTreeGUI(title)) {
            handleSkillTreeGUI(event, player);
        } else if (isUpgradeGUI(title)) {
            handleUpgradeGUI(event, player);
        } else if (isBlockFilterGUI(title)) {
            handleBlockFilterGUI(event, player, title);
        }
    }

    // ==================== MAIN GUI ====================
    private void handleMainGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        plugin.getEffectsManager().playGUIClick(player);

        switch (slot) {
            case 20 -> { // Skill Tree
                player.closeInventory();
                SkillTreeGUI.open(player, plugin);
            }
            case 22 -> { // Upgrades
                player.closeInventory();
                UpgradeGUI.open(player, plugin);
            }
            case 24 -> { // Block Filter
                player.closeInventory();
                BlockFilterGUI.open(player, plugin, 0);
            }
            case 31 -> { // Toggle filter mode
                PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
                profile.cycleFilterMode();
                String modeName = plugin.getItemManager().getFilterModeName(profile.getFilterMode(),
                        plugin.getMessageManager().getPlayerLanguage(player).equals("ar"));
                plugin.getMessageManager().send(player, "filter-mode-changed",
                        Map.of("{mode}", modeName));
                player.closeInventory();
                MainGUI.open(player, plugin);
            }
            case 40 -> { // Get Hammer
                if (!player.hasPermission("titanhammer.give.self")) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return;
                }
                player.getInventory().addItem(plugin.getItemManager().createHammer(player));
                plugin.getMessageManager().send(player, "hammer-received");
                player.closeInventory();
            }
            case 49 -> { // Language toggle
                String currentLang = plugin.getMessageManager().getPlayerLanguage(player);
                String newLang = currentLang.equals("ar") ? "en" : "ar";
                plugin.getMessageManager().setPlayerLanguage(player.getUniqueId().toString(), newLang);
                player.closeInventory();
                MainGUI.open(player, plugin);
            }
        }
    }

    // ==================== SKILL TREE GUI ====================
    private void handleSkillTreeGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // Back button
        if (slot == 49) {
            player.closeInventory();
            MainGUI.open(player, plugin);
            return;
        }

        // Check if clicked on a skill
        String skillId = SkillTreeGUI.getSkillIdAtSlot(slot);
        if (skillId == null) return;

        Skill skill = plugin.getSkillManager().getSkill(skillId);
        if (skill == null) return;

        plugin.getEffectsManager().playGUIClick(player);

        // Try to upgrade
        if (plugin.getSkillManager().upgradeSkill(player, skill)) {
            boolean isAr = plugin.getMessageManager().getPlayerLanguage(player).equals("ar");
            String skillName = isAr ? skill.getDisplayNameAr() : skill.getDisplayName();
            int newLevel = plugin.getPlayerDataManager().getProfile(player).getSkillLevel(skillId);

            plugin.getEffectsManager().playSkillUpgrade(player);
            plugin.getMessageManager().send(player, "skill-upgraded",
                    Map.of("{skill}", skillName, "{level}", String.valueOf(newLevel)));

            // Refresh
            player.closeInventory();
            SkillTreeGUI.open(player, plugin);
        } else {
            boolean isAr = plugin.getMessageManager().getPlayerLanguage(player).equals("ar");
            if (!plugin.getSkillManager().canUnlock(player, skill)) {
                plugin.getMessageManager().send(player, "skill-locked");
            } else {
                PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
                if (profile.getSkillLevel(skillId) >= skill.getMaxLevel()) {
                    plugin.getMessageManager().send(player, "skill-maxed");
                } else {
                    plugin.getMessageManager().send(player, "not-enough-money");
                }
            }
            plugin.getEffectsManager().playError(player);
        }
    }

    // ==================== UPGRADE GUI ====================
    private void handleUpgradeGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // Back button
        if (slot == 49) {
            player.closeInventory();
            MainGUI.open(player, plugin);
            return;
        }

        // Quick upgrade buttons
        String skillId = UpgradeGUI.getSkillIdAtSlot(slot);
        if (skillId == null) return;

        Skill skill = plugin.getSkillManager().getSkill(skillId);
        if (skill == null) return;

        plugin.getEffectsManager().playGUIClick(player);

        if (plugin.getSkillManager().upgradeSkill(player, skill)) {
            boolean isAr = plugin.getMessageManager().getPlayerLanguage(player).equals("ar");
            String skillName = isAr ? skill.getDisplayNameAr() : skill.getDisplayName();
            int newLevel = plugin.getPlayerDataManager().getProfile(player).getSkillLevel(skillId);

            plugin.getEffectsManager().playSkillUpgrade(player);
            plugin.getMessageManager().send(player, "skill-upgraded",
                    Map.of("{skill}", skillName, "{level}", String.valueOf(newLevel)));

            player.closeInventory();
            UpgradeGUI.open(player, plugin);
        } else {
            plugin.getMessageManager().send(player, "not-enough-money");
            plugin.getEffectsManager().playError(player);
        }
    }

    // ==================== BLOCK FILTER GUI ====================
    private void handleBlockFilterGUI(InventoryClickEvent event, Player player, String title) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        int page = BlockFilterGUI.getPageFromTitle(title);
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);

        // Mode toggle - slot 4
        if (slot == 4) {
            profile.cycleFilterMode();
            player.closeInventory();
            BlockFilterGUI.open(player, plugin, page);
            return;
        }

        // Previous page - slot 45
        if (slot == 45 && page > 0) {
            player.closeInventory();
            BlockFilterGUI.open(player, plugin, page - 1);
            return;
        }

        // Next page - slot 53
        if (slot == 53) {
            player.closeInventory();
            BlockFilterGUI.open(player, plugin, page + 1);
            return;
        }

        // Back - slot 48
        if (slot == 48) {
            player.closeInventory();
            MainGUI.open(player, plugin);
            return;
        }

        // Clear all - slot 50
        if (slot == 50) {
            profile.getAutoCollectBlocks().clear();
            profile.getAutoDeleteBlocks().clear();
            plugin.getMessageManager().send(player, "filters-cleared");
            player.closeInventory();
            BlockFilterGUI.open(player, plugin, page);
            return;
        }

        // Block selection
        if (BlockFilterGUI.isContentSlot(slot)) {
            Material mat = BlockFilterGUI.getBlockAtSlot(slot, page, plugin);
            if (mat == null) return;

            plugin.getEffectsManager().playGUIClick(player);

            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                // Remove from both lists
                profile.getAutoCollectBlocks().remove(mat);
                profile.getAutoDeleteBlocks().remove(mat);
            } else if (event.isLeftClick()) {
                profile.toggleAutoCollect(mat);
            } else if (event.isRightClick()) {
                profile.toggleAutoDelete(mat);
            }

            player.closeInventory();
            BlockFilterGUI.open(player, plugin, page);
        }
    }

    // ==================== GUI TITLE CHECKS ====================
    private boolean isMainGUI(String title) {
        return title.contains("TitanHammer") || title.contains("مطرقة تايتن");
    }

    private boolean isSkillTreeGUI(String title) {
        return title.contains("Skill Tree") || title.contains("شجرة المهارات");
    }

    private boolean isUpgradeGUI(String title) {
        return title.contains("Upgrades") || title.contains("الترقيات");
    }

    private boolean isBlockFilterGUI(String title) {
        return title.contains("Block Filter") || title.contains("فلتر الكتل");
    }
}
