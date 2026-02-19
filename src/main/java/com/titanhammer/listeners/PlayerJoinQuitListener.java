package com.titanhammer.listeners;

import com.titanhammer.TitanHammerPro;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {

    private final TitanHammerPro plugin;

    public PlayerJoinQuitListener(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Pre-load player data
        plugin.getPlayerDataManager().getProfile(player);

        // Refresh hammer lore if they have one
        plugin.getItemManager().refreshPlayerHammer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Save and unload data
        plugin.getPlayerDataManager().unloadProfile(player.getUniqueId());
    }
}
