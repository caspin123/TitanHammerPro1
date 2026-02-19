package com.titanhammer.managers;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final TitanHammerPro plugin;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();
    private final File dataFolder;

    public PlayerDataManager(TitanHammerPro plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        // Auto-save task
        int interval = plugin.getConfigManager().getAutoSaveInterval() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllData, interval, interval);
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, this::loadProfile);
    }

    public PlayerProfile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    private PlayerProfile loadProfile(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerProfile profile = new PlayerProfile(uuid);

        if (!file.exists()) return profile;

        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);

        profile.setLevel(data.getInt("level", 1));
        profile.setXp(data.getDouble("xp", 0));
        profile.setTotalXp(data.getDouble("total-xp", 0));
        profile.setCustomBalance(data.getDouble("custom-balance", 0));
        profile.setHammerActive(data.getBoolean("hammer-active", false));

        // Filter mode
        String filterMode = data.getString("filter-mode", "DISABLED");
        try {
            profile.setFilterMode(PlayerProfile.FilterMode.valueOf(filterMode));
        } catch (IllegalArgumentException ignored) {}

        // Skills
        if (data.isConfigurationSection("skills")) {
            for (String key : data.getConfigurationSection("skills").getKeys(false)) {
                profile.setSkillLevel(key, data.getInt("skills." + key, 0));
            }
        }

        // Auto collect blocks
        List<String> collectList = data.getStringList("auto-collect-blocks");
        for (String mat : collectList) {
            try {
                profile.getAutoCollectBlocks().add(Material.valueOf(mat));
            } catch (IllegalArgumentException ignored) {}
        }

        // Auto delete blocks
        List<String> deleteList = data.getStringList("auto-delete-blocks");
        for (String mat : deleteList) {
            try {
                profile.getAutoDeleteBlocks().add(Material.valueOf(mat));
            } catch (IllegalArgumentException ignored) {}
        }

        return profile;
    }

    public void saveProfile(UUID uuid) {
        PlayerProfile profile = profiles.get(uuid);
        if (profile == null) return;

        File file = new File(dataFolder, uuid + ".yml");
        YamlConfiguration data = new YamlConfiguration();

        data.set("level", profile.getLevel());
        data.set("xp", profile.getXp());
        data.set("total-xp", profile.getTotalXp());
        data.set("custom-balance", profile.getCustomBalance());
        data.set("hammer-active", profile.isHammerActive());
        data.set("filter-mode", profile.getFilterMode().name());

        // Skills
        for (Map.Entry<String, Integer> entry : profile.getSkillLevels().entrySet()) {
            data.set("skills." + entry.getKey(), entry.getValue());
        }

        // Auto collect blocks
        List<String> collectList = new ArrayList<>();
        for (Material mat : profile.getAutoCollectBlocks()) {
            collectList.add(mat.name());
        }
        data.set("auto-collect-blocks", collectList);

        // Auto delete blocks
        List<String> deleteList = new ArrayList<>();
        for (Material mat : profile.getAutoDeleteBlocks()) {
            deleteList.add(mat.name());
        }
        data.set("auto-delete-blocks", deleteList);

        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAllData() {
        for (UUID uuid : profiles.keySet()) {
            saveProfile(uuid);
        }
    }

    public void unloadProfile(UUID uuid) {
        saveProfile(uuid);
        profiles.remove(uuid);
    }

    public Collection<PlayerProfile> getAllLoadedProfiles() {
        return profiles.values();
    }
}
