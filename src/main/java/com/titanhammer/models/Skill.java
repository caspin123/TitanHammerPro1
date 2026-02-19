package com.titanhammer.models;

import org.bukkit.Material;

import java.util.List;

public class Skill {

    private final String id;
    private final String displayName;
    private final String displayNameAr;
    private final String description;
    private final String descriptionAr;
    private final Material icon;
    private final int maxLevel;
    private final double baseCost;
    private final double costMultiplier;
    private final String economyType; // vault, playerpoints, custom
    private final List<String> prerequisites;
    private final SkillType type;

    public enum SkillType {
        DAMAGE_BOOST,
        XP_BOOST,
        FORTUNE_BOOST,
        SPEED_BOOST,
        HASTE_BOOST,
        FIRE_ASPECT,
        AREA_BREAK,
        AUTO_SMELT,
        LIFE_STEAL,
        DROP_MULTIPLIER
    }

    public Skill(String id, String displayName, String displayNameAr,
                 String description, String descriptionAr, Material icon,
                 int maxLevel, double baseCost, double costMultiplier,
                 String economyType, List<String> prerequisites, SkillType type) {
        this.id = id;
        this.displayName = displayName;
        this.displayNameAr = displayNameAr;
        this.description = description;
        this.descriptionAr = descriptionAr;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.economyType = economyType;
        this.prerequisites = prerequisites;
        this.type = type;
    }

    public double getCostForLevel(int level) {
        return baseCost * Math.pow(costMultiplier, level - 1);
    }

    public double getEffectValue(int level) {
        return switch (type) {
            case DAMAGE_BOOST -> 0.10 * level;       // +10% per level
            case XP_BOOST -> 0.15 * level;            // +15% per level
            case FORTUNE_BOOST -> 0.20 * level;       // +20% per level
            case SPEED_BOOST -> 0.05 * level;         // +5% per level
            case HASTE_BOOST -> level;                 // +1 haste level per level
            case FIRE_ASPECT -> level;                 // +1 fire aspect per level
            case AREA_BREAK -> level;                  // +1 radius per level
            case AUTO_SMELT -> level > 0 ? 1.0 : 0;   // on/off
            case LIFE_STEAL -> 0.05 * level;           // +5% per level
            case DROP_MULTIPLIER -> 0.25 * level;      // +25% per level
        };
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDisplayNameAr() { return displayNameAr; }
    public String getDescription() { return description; }
    public String getDescriptionAr() { return descriptionAr; }
    public Material getIcon() { return icon; }
    public int getMaxLevel() { return maxLevel; }
    public double getBaseCost() { return baseCost; }
    public double getCostMultiplier() { return costMultiplier; }
    public String getEconomyType() { return economyType; }
    public List<String> getPrerequisites() { return prerequisites; }
    public SkillType getType() { return type; }
}
