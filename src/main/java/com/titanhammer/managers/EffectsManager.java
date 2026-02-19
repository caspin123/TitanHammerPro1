package com.titanhammer.managers;

import com.titanhammer.TitanHammerPro;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class EffectsManager {

    private final TitanHammerPro plugin;

    public EffectsManager(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    public void playBlockBreak(Player player, Location loc) {
        if (!plugin.getConfigManager().isParticlesEnabled()) return;
        player.getWorld().spawnParticle(Particle.CRIT, loc.add(0.5, 0.5, 0.5), 8, 0.3, 0.3, 0.3, 0.05);

        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 0.5f, 1.2f);
        }
    }

    public void playAreaBreak(Player player, Location loc) {
        if (!plugin.getConfigManager().isParticlesEnabled()) return;
        player.getWorld().spawnParticle(Particle.EXPLOSION, loc.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5, 0);
        player.getWorld().spawnParticle(Particle.FLAME, loc, 15, 1.0, 1.0, 1.0, 0.02);

        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.5f);
        }
    }

    public void playAutoCollect(Player player) {
        if (!plugin.getConfigManager().isParticlesEnabled()) return;
        player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);

        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 1.4f);
        }
    }

    public void playAutoDelete(Player player, Location loc) {
        if (!plugin.getConfigManager().isParticlesEnabled()) return;
        player.getWorld().spawnParticle(Particle.SMOKE, loc.add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.02);

        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.3f, 1.0f);
        }
    }

    public void playLevelUp(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        if (plugin.getConfigManager().isParticlesEnabled()) {
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0),
                    50, 0.5, 1.0, 0.5, 0.3);
        }

        if (plugin.getConfigManager().isLevelUpFirework()) {
            spawnFirework(player.getLocation());
        }
    }

    public void playSkillUpgrade(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        }

        if (plugin.getConfigManager().isParticlesEnabled()) {
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1.5, 0),
                    30, 0.5, 0.5, 0.5, 1.0);
        }
    }

    public void playMythicDamage(Player player) {
        if (!plugin.getConfigManager().isParticlesEnabled()) return;
        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation().add(0, 1, 0),
                15, 0.4, 0.4, 0.4, 0.02);
    }

    public void playGUIOpen(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.2f);
        }
    }

    public void playGUIClick(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }

    public void playError(Player player) {
        if (plugin.getConfigManager().isSoundsEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }

    public void playLifeSteal(Player player) {
        if (!plugin.getConfigManager().isParticlesEnabled()) return;
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0),
                3, 0.3, 0.2, 0.3, 0);
    }

    public void playAutoSmelt(Location loc) {
        if (!plugin.getConfigManager().isParticlesEnabled()) return;
        loc.getWorld().spawnParticle(Particle.FLAME, loc.add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, 0.02);
    }

    private void spawnFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.fromRGB(255, 85, 85), Color.fromRGB(255, 170, 0), Color.fromRGB(85, 255, 85))
                .withFade(Color.WHITE)
                .withFlicker()
                .withTrail()
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }
}
