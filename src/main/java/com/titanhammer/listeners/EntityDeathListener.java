package com.titanhammer.listeners;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.models.Skill.SkillType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EntityDeathListener implements Listener {

    private final TitanHammerPro plugin;
    private final Random random = new Random();

    public EntityDeathListener(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;
        if (!plugin.getItemManager().isTitanHammer(killer.getInventory().getItemInMainHand())) return;

        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(killer);

        // ---- XP Gain ----
        double baseXp = plugin.getConfigManager().getMobKillXp();
        double xpBoost = plugin.getSkillManager().getSkillEffect(killer, SkillType.XP_BOOST);
        double finalXp = baseXp * (1.0 + xpBoost);

        // Bonus XP for MythicMobs
        if (plugin.hasMythicMobs() && plugin.getMythicMobsHook().isMythicMob(entity)) {
            double mobLevel = plugin.getMythicMobsHook().getMobLevel(entity);
            finalXp *= (1.0 + mobLevel * 0.1);
        }

        profile.addXp(finalXp);

        // Check level up
        while (profile.canLevelUp() && profile.getLevel() < plugin.getConfigManager().getMaxLevel()) {
            profile.tryLevelUp();
            plugin.getEffectsManager().playLevelUp(killer);
            plugin.getMessageManager().send(killer, "level-up",
                    Map.of("{level}", String.valueOf(profile.getLevel())));
        }

        // ---- Drop Multiplier ----
        double dropMultiplier = plugin.getSkillManager().getSkillEffect(killer, SkillType.DROP_MULTIPLIER);

        // MythicMobs drop bonus
        if (plugin.hasMythicMobs() && plugin.getMythicMobsHook().isMythicMob(entity)) {
            double mythicDropBonus = plugin.getMythicMobsHook()
                    .getDropBonusMultiplier(entity, dropMultiplier);
            dropMultiplier = mythicDropBonus - 1.0; // Convert back to bonus percentage
        }

        if (dropMultiplier > 0) {
            List<ItemStack> extraDrops = new ArrayList<>();
            for (ItemStack drop : event.getDrops()) {
                if (random.nextDouble() < dropMultiplier) {
                    ItemStack extra = drop.clone();
                    extra.setAmount(1 + random.nextInt(Math.max(1, drop.getAmount())));
                    extraDrops.add(extra);
                }
            }
            event.getDrops().addAll(extraDrops);
        }

        // ---- XP Orb Multiplier ----
        if (xpBoost > 0) {
            int bonusXpOrbs = (int) (event.getDroppedExp() * xpBoost);
            event.setDroppedExp(event.getDroppedExp() + bonusXpOrbs);
        }

        // Update hammer
        plugin.getItemManager().refreshPlayerHammer(killer);
    }
}
