package com.titanhammer;

import com.titanhammer.commands.TitanCommand;
import com.titanhammer.economy.EconomyManager;
import com.titanhammer.hooks.MythicMobsHook;
import com.titanhammer.listeners.*;
import com.titanhammer.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TitanHammerPro extends JavaPlugin {

    private static TitanHammerPro instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlayerDataManager playerDataManager;
    private ItemManager itemManager;
    private EconomyManager economyManager;
    private SkillManager skillManager;
    private BlockFilterManager blockFilterManager;
    private EffectsManager effectsManager;
    private MythicMobsHook mythicMobsHook;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers in order
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.itemManager = new ItemManager(this);
        this.economyManager = new EconomyManager(this);
        this.skillManager = new SkillManager(this);
        this.blockFilterManager = new BlockFilterManager(this);
        this.effectsManager = new EffectsManager(this);

        // Hook into optional dependencies
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            this.mythicMobsHook = new MythicMobsHook(this);
            getLogger().info("MythicMobs hook enabled!");
        }

        // Register listeners
        registerListeners();

        // Register commands
        TitanCommand titanCommand = new TitanCommand(this);
        getCommand("titanhammer").setExecutor(titanCommand);
        getCommand("titanhammer").setTabCompleter(titanCommand);

        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║   TitanHammer Pro v1.0.0 Enabled!    ║");
        getLogger().info("║   Paper 1.21 | by Blooddev            ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }
        getLogger().info("TitanHammer Pro disabled!");
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new GUIClickListener(this), this);
        pm.registerEvents(new PlayerJoinQuitListener(this), this);
        pm.registerEvents(new AntiExploitListener(this), this);
        pm.registerEvents(new EntityDamageListener(this), this);
        pm.registerEvents(new EntityDeathListener(this), this);
    }

    public void reload() {
        configManager.reload();
        messageManager.reload();
        getLogger().info("TitanHammer Pro reloaded!");
    }

    // Getters
    public static TitanHammerPro getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public ItemManager getItemManager() { return itemManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public BlockFilterManager getBlockFilterManager() { return blockFilterManager; }
    public EffectsManager getEffectsManager() { return effectsManager; }
    public MythicMobsHook getMythicMobsHook() { return mythicMobsHook; }
    public boolean hasMythicMobs() { return mythicMobsHook != null; }
}
