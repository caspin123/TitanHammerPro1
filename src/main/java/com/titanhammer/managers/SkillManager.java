package com.titanhammer.managers;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.models.Skill;
import com.titanhammer.models.Skill.SkillType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class SkillManager {

    private final TitanHammerPro plugin;
    private final Map<String, Skill> skills = new LinkedHashMap<>();

    public SkillManager(TitanHammerPro plugin) {
        this.plugin = plugin;
        loadDefaultSkills();
    }

    private void loadDefaultSkills() {
        // Tier 1 Skills - No prerequisites
        register(new Skill("damage_boost", "Damage Boost", "زيادة الضرر",
                "Increases damage dealt", "يزيد الضرر الموجه",
                Material.DIAMOND_SWORD, 10, 100, 1.5, "vault",
                List.of(), SkillType.DAMAGE_BOOST));

        register(new Skill("xp_boost", "XP Boost", "زيادة الخبرة",
                "Increases XP earned", "يزيد الخبرة المكتسبة",
                Material.EXPERIENCE_BOTTLE, 10, 80, 1.4, "vault",
                List.of(), SkillType.XP_BOOST));

        register(new Skill("fortune_boost", "Fortune", "الحظ",
                "Increases block drops", "يزيد قطرات الكتل",
                Material.EMERALD, 5, 200, 1.8, "vault",
                List.of(), SkillType.FORTUNE_BOOST));

        // Tier 2 Skills - Require Tier 1
        register(new Skill("speed_boost", "Swift Miner", "المنقب السريع",
                "Mining speed boost", "تعزيز سرعة التنقيب",
                Material.GOLDEN_PICKAXE, 5, 150, 1.6, "vault",
                List.of("xp_boost"), SkillType.SPEED_BOOST));

        register(new Skill("haste_boost", "Haste Aura", "هالة السرعة",
                "Grants Haste effect", "يمنح تأثير السرعة",
                Material.BEACON, 3, 300, 2.0, "vault",
                List.of("speed_boost"), SkillType.HASTE_BOOST));

        register(new Skill("fire_aspect", "Inferno Strike", "ضربة النار",
                "Sets enemies on fire", "يشعل الأعداء بالنار",
                Material.BLAZE_POWDER, 3, 250, 1.7, "vault",
                List.of("damage_boost"), SkillType.FIRE_ASPECT));

        // Tier 3 Skills - Require Tier 2
        register(new Skill("area_break", "Earthquake", "الزلزال",
                "Break blocks in area", "كسر الكتل في منطقة",
                Material.TNT, 3, 500, 2.0, "vault",
                List.of("fortune_boost", "speed_boost"), SkillType.AREA_BREAK));

        register(new Skill("auto_smelt", "Auto Smelt", "الصهر التلقائي",
                "Auto smelts mined ores", "صهر الخامات تلقائياً",
                Material.FURNACE, 1, 1000, 1.0, "vault",
                List.of("fortune_boost"), SkillType.AUTO_SMELT));

        register(new Skill("life_steal", "Vampiric Strike", "ضربة مصاص الدماء",
                "Heal on damage dealt", "شفاء عند إلحاق الضرر",
                Material.NETHER_STAR, 5, 400, 1.8, "vault",
                List.of("damage_boost", "fire_aspect"), SkillType.LIFE_STEAL));

        register(new Skill("drop_multiplier", "Drop Master", "سيد القطرات",
                "Multiplies mob drops", "يضاعف قطرات الوحوش",
                Material.CHEST, 5, 350, 1.6, "vault",
                List.of("fortune_boost"), SkillType.DROP_MULTIPLIER));
    }

    private void register(Skill skill) {
        skills.put(skill.getId(), skill);
    }

    public Skill getSkill(String id) {
        return skills.get(id);
    }

    public Collection<Skill> getAllSkills() {
        return skills.values();
    }

    public boolean canUnlock(Player player, Skill skill) {
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);

        // Check prerequisites
        for (String prereq : skill.getPrerequisites()) {
            if (profile.getSkillLevel(prereq) <= 0) return false;
        }
        return true;
    }

    public boolean canUpgrade(Player player, Skill skill) {
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        int currentLevel = profile.getSkillLevel(skill.getId());

        if (currentLevel >= skill.getMaxLevel()) return false;
        if (!canUnlock(player, skill)) return false;

        double cost = skill.getCostForLevel(currentLevel + 1);
        return plugin.getEconomyManager().has(player, cost, skill.getEconomyType());
    }

    public boolean upgradeSkill(Player player, Skill skill) {
        if (!canUpgrade(player, skill)) return false;

        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        int currentLevel = profile.getSkillLevel(skill.getId());
        double cost = skill.getCostForLevel(currentLevel + 1);

        if (plugin.getEconomyManager().withdraw(player, cost, skill.getEconomyType())) {
            profile.setSkillLevel(skill.getId(), currentLevel + 1);
            return true;
        }
        return false;
    }

    public double getSkillEffect(Player player, SkillType type) {
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        double total = 0;

        for (Skill skill : skills.values()) {
            if (skill.getType() == type) {
                int level = profile.getSkillLevel(skill.getId());
                if (level > 0) {
                    total += skill.getEffectValue(level);
                }
            }
        }
        return total;
    }

    public Map<String, Skill> getSkillMap() {
        return skills;
    }
}
