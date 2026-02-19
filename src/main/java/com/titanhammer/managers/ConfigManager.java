package com.titanhammer.managers;

import com.titanhammer.TitanHammerPro;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final TitanHammerPro plugin;
    private FileConfiguration config;

    public ConfigManager(TitanHammerPro plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() { return config; }

    // Item Settings
    public int getCustomModelData() { return config.getInt("item.custom-model-data", 2025); }
    public String getItemName() { return config.getString("item.name", "&c&l⚡ TitanHammer &7&lPro"); }
    public String getItemNameAr() { return config.getString("item.name-ar", "&c&l⚡ مطرقة تايتن &7&lبرو"); }

    // Level Settings
    public int getMaxLevel() { return config.getInt("levels.max-level", 100); }
    public double getBaseXp() { return config.getDouble("levels.base-xp", 100); }
    public double getXpMultiplier() { return config.getDouble("levels.xp-multiplier", 1.5); }
    public double getBlockBreakXp() { return config.getDouble("levels.block-break-xp", 5.0); }
    public double getMobKillXp() { return config.getDouble("levels.mob-kill-xp", 15.0); }

    // Economy Settings
    public String getDefaultEconomy() { return config.getString("economy.default", "vault"); }

    // Effects
    public boolean isParticlesEnabled() { return config.getBoolean("effects.particles", true); }
    public boolean isSoundsEnabled() { return config.getBoolean("effects.sounds", true); }
    public boolean isLevelUpFirework() { return config.getBoolean("effects.level-up-firework", true); }

    // Anti-Exploit
    public boolean isAntiSilkTouch() { return config.getBoolean("anti-exploit.silk-touch-protection", true); }
    public boolean isAntiDupe() { return config.getBoolean("anti-exploit.anti-duplication", true); }
    public boolean isAntiCreative() { return config.getBoolean("anti-exploit.block-creative-mode", true); }

    // Data
    public String getStorageType() { return config.getString("storage.type", "yaml"); }
    public int getAutoSaveInterval() { return config.getInt("storage.auto-save-minutes", 5); }

    // Language
    public String getDefaultLanguage() { return config.getString("language.default", "en"); }
}
