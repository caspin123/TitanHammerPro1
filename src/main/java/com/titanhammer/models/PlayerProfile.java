package com.titanhammer.models;

import org.bukkit.Material;

import java.util.*;

public class PlayerProfile {

    private final UUID uuid;
    private int level;
    private double xp;
    private double totalXp;
    private Map<String, Integer> skillLevels;
    private Set<Material> autoCollectBlocks;
    private Set<Material> autoDeleteBlocks;
    private FilterMode filterMode;
    private boolean hammerActive;
    private double customBalance;

    public enum FilterMode {
        AUTO_COLLECT,
        AUTO_DELETE,
        DISABLED
    }

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.xp = 0;
        this.totalXp = 0;
        this.skillLevels = new HashMap<>();
        this.autoCollectBlocks = new HashSet<>();
        this.autoDeleteBlocks = new HashSet<>();
        this.filterMode = FilterMode.DISABLED;
        this.hammerActive = false;
        this.customBalance = 0;
    }

    public UUID getUuid() { return uuid; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }

    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = Math.max(0, xp); }
    public void addXp(double amount) { this.xp += amount; this.totalXp += amount; }

    public double getTotalXp() { return totalXp; }
    public void setTotalXp(double totalXp) { this.totalXp = totalXp; }

    public int getSkillLevel(String skillId) {
        return skillLevels.getOrDefault(skillId, 0);
    }

    public void setSkillLevel(String skillId, int level) {
        skillLevels.put(skillId, level);
    }

    public Map<String, Integer> getSkillLevels() { return skillLevels; }

    public Set<Material> getAutoCollectBlocks() { return autoCollectBlocks; }
    public Set<Material> getAutoDeleteBlocks() { return autoDeleteBlocks; }

    public void toggleAutoCollect(Material mat) {
        if (autoCollectBlocks.contains(mat)) {
            autoCollectBlocks.remove(mat);
        } else {
            autoDeleteBlocks.remove(mat);
            autoCollectBlocks.add(mat);
        }
    }

    public void toggleAutoDelete(Material mat) {
        if (autoDeleteBlocks.contains(mat)) {
            autoDeleteBlocks.remove(mat);
        } else {
            autoCollectBlocks.remove(mat);
            autoDeleteBlocks.add(mat);
        }
    }

    public FilterMode getFilterMode() { return filterMode; }
    public void setFilterMode(FilterMode mode) { this.filterMode = mode; }

    public void cycleFilterMode() {
        switch (filterMode) {
            case DISABLED -> filterMode = FilterMode.AUTO_COLLECT;
            case AUTO_COLLECT -> filterMode = FilterMode.AUTO_DELETE;
            case AUTO_DELETE -> filterMode = FilterMode.DISABLED;
        }
    }

    public boolean isHammerActive() { return hammerActive; }
    public void setHammerActive(boolean active) { this.hammerActive = active; }

    public double getCustomBalance() { return customBalance; }
    public void setCustomBalance(double balance) { this.customBalance = balance; }
    public void addCustomBalance(double amount) { this.customBalance += amount; }
    public boolean withdrawCustom(double amount) {
        if (customBalance >= amount) {
            customBalance -= amount;
            return true;
        }
        return false;
    }

    public double getXpForNextLevel() {
        return 100 * Math.pow(level, 1.5);
    }

    public boolean canLevelUp() {
        return xp >= getXpForNextLevel();
    }

    public boolean tryLevelUp() {
        if (canLevelUp()) {
            xp -= getXpForNextLevel();
            level++;
            return true;
        }
        return false;
    }
}
