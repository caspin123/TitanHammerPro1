package com.titanhammer.listeners;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.Skill.SkillType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {

    private final TitanHammerPro plugin;

    public EntityDamageListener(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!plugin.getItemManager().isTitanHammer(player.getInventory().getItemInMainHand())) return;

        double originalDamage = event.getDamage();
        double damageMultiplier = 1.0;

        // Damage boost skill
        double damageBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.DAMAGE_BOOST);
        damageMultiplier += damageBoost;

        // MythicMobs damage bonus
        if (plugin.hasMythicMobs() && plugin.getMythicMobsHook().isMythicMob(event.getEntity())) {
            double mythicBonus = plugin.getMythicMobsHook()
                    .getDamageBonusMultiplier(event.getEntity(), damageBoost);
            damageMultiplier *= mythicBonus;
            plugin.getEffectsManager().playMythicDamage(player);
        }

        // Apply final damage
        event.setDamage(originalDamage * damageMultiplier);

        // Fire aspect skill
        double fireAspect = plugin.getSkillManager().getSkillEffect(player, SkillType.FIRE_ASPECT);
        if (fireAspect > 0 && event.getEntity() instanceof LivingEntity target) {
            target.setFireTicks((int) (fireAspect * 40)); // 2 seconds per level
        }

        // Life steal skill
        double lifeSteal = plugin.getSkillManager().getSkillEffect(player, SkillType.LIFE_STEAL);
        if (lifeSteal > 0) {
            double healAmount = event.getFinalDamage() * lifeSteal;
            double newHealth = Math.min(player.getHealth() + healAmount,
                    player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
            player.setHealth(newHealth);
            plugin.getEffectsManager().playLifeSteal(player);
        }

        // XP from combat
        double baseXp = plugin.getConfigManager().getMobKillXp() * 0.3; // Partial XP per hit
        double xpBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.XP_BOOST);
        double finalXp = baseXp * (1.0 + xpBoost);
        plugin.getPlayerDataManager().getProfile(player).addXp(finalXp);
    }
}
