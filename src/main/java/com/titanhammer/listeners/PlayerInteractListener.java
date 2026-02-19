package com.titanhammer.listeners;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.gui.MainGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final TitanHammerPro plugin;

    public PlayerInteractListener(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only handle main hand
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Check for right click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Check if holding TitanHammer
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.getItemManager().isTitanHammer(item)) return;

        // Check for sneak + right click
        if (player.isSneaking()) {
            event.setCancelled(true);

            // Check permission
            if (!player.hasPermission("titanhammer.use")) {
                plugin.getMessageManager().send(player, "no-permission");
                return;
            }

            // Open main GUI
            MainGUI.open(player, plugin);
        }
    }
}
