package com.titanhammer.hooks;

import com.titanhammer.TitanHammerPro;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class MythicMobsHook {

    private final TitanHammerPro plugin;

    public MythicMobsHook(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if an entity is a MythicMob
     */
    public boolean isMythicMob(Entity entity) {
        try {
            return MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId()).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the MythicMob's internal name
     */
    public String getMobType(Entity entity) {
        try {
            Optional<ActiveMob> mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
            return mob.map(activeMob -> activeMob.getMobType()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the MythicMob's level for scaling
     */
    public double getMobLevel(Entity entity) {
        try {
            Optional<ActiveMob> mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
            return mob.map(activeMob -> activeMob.getLevel()).orElse(1.0);
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**
     * Calculate damage bonus multiplier for MythicMob targets
     */
    public double getDamageBonusMultiplier(Entity entity, double baseBonus) {
        if (!isMythicMob(entity)) return 1.0;
        double mobLevel = getMobLevel(entity);
        // Scale bonus with mob level - higher level mobs get less bonus effectiveness
        return 1.0 + (baseBonus * (1.0 / (1.0 + mobLevel * 0.1)));
    }

    /**
     * Calculate drop bonus multiplier for MythicMob kills
     */
    public double getDropBonusMultiplier(Entity entity, double baseBonus) {
        if (!isMythicMob(entity)) return 1.0;
        double mobLevel = getMobLevel(entity);
        // Higher level mobs give better drops
        return 1.0 + (baseBonus * (1.0 + mobLevel * 0.05));
    }
}
