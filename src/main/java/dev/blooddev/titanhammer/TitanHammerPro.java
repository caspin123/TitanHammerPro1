package dev.blooddev.titanhammer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TitanHammerPro extends JavaPlugin implements Listener {

    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("================================");
        getLogger().info("  TitanHammer Pro Enabled!");
        getLogger().info("  Version: 1.0.0");
        getLogger().info("  Author: Blooddev");
        getLogger().info("================================");
        
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("TitanHammer Pro Disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⚒ TitanHammer Pro " + ChatColor.YELLOW + "v1.0.0");
            player.sendMessage(ChatColor.YELLOW + "/th give - Get Titan Hammer");
            player.sendMessage(ChatColor.YELLOW + "/th stats - View your stats");
            player.sendMessage(ChatColor.GRAY + "By Blooddev - PrimeHost");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            ItemStack hammer = createTitanHammer();
            player.getInventory().addItem(hammer);
            player.sendMessage(ChatColor.GREEN + "✓ You received the Titan Hammer!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            PlayerStats stats = getPlayerStats(player.getUniqueId());
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== Your Stats ===");
            player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.GOLD + stats.level);
            player.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.AQUA + String.format("%.0f", stats.xp) + ChatColor.GRAY + "/" + ChatColor.AQUA + stats.getRequiredXP());
            player.sendMessage(ChatColor.YELLOW + "Blocks Destroyed: " + ChatColor.GREEN + stats.blocksDestroyed);
            player.sendMessage(ChatColor.YELLOW + "Damage Bonus: " + ChatColor.RED + "+" + (stats.level * 5) + "%");
            return true;
        }

        return true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isTitanHammer(item)) return;

        PlayerStats stats = getPlayerStats(player.getUniqueId());
        
        double xp = getXPForBlock(event.getBlock().getType());
        stats.addXP(xp);
        stats.blocksDestroyed++;

        while (stats.xp >= stats.getRequiredXP()) {
            stats.xp -= stats.getRequiredXP();
            stats.level++;
            
            player.sendTitle(
                ChatColor.GOLD + "" + ChatColor.BOLD + "LEVEL UP!",
                ChatColor.YELLOW + "You are now level " + stats.level,
                10, 40, 10
            );
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
            player.sendMessage(ChatColor.GREEN + "✦ " + ChatColor.YELLOW + "You leveled up to " + ChatColor.GOLD + "Level " + stats.level + ChatColor.YELLOW + "! " + ChatColor.GREEN + "✦");
        }

        player.getWorld().spawnParticle(
            Particle.EXPLOSION,
            event.getBlock().getLocation().add(0.5, 0.5, 0.5),
            5, 0.3, 0.3, 0.3, 0.1
        );

        if (stats.level >= 5) {
            breakNearbyBlocks(event.getBlock(), player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isTitanHammer(item)) return;

        if (player.isSneaking() && event.getAction().name().contains("RIGHT_CLICK")) {
            event.setCancelled(true);
            PlayerStats stats = getPlayerStats(player.getUniqueId());
            
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⚒ Titan Hammer Stats ⚒");
            player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.GOLD + stats.level);
            player.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.AQUA + String.format("%.1f", stats.xp) + ChatColor.GRAY + "/" + ChatColor.AQUA + stats.getRequiredXP());
            player.sendMessage(ChatColor.YELLOW + "Progress: " + ChatColor.GREEN + String.format("%.1f", (stats.xp / stats.getRequiredXP() * 100)) + "%");
            player.sendMessage(ChatColor.YELLOW + "Blocks Destroyed: " + ChatColor.GREEN + stats.blocksDestroyed);
            player.sendMessage(ChatColor.YELLOW + "Damage Bonus: " + ChatColor.RED + "+" + (stats.level * 5) + "%");
            player.sendMessage(ChatColor.YELLOW + "Range Mining: " + (stats.level >= 5 ? ChatColor.GREEN + "✓ Unlocked (3x3)" : ChatColor.RED + "✗ Level 5 Required"));
            
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
        }
    }

    private void breakNearbyBlocks(Block center, Player player) {
        Material centerType = center.getType();
        
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Block block = center.getRelative(x, y, z);
                    
                    if (block.getType() == Material.AIR || block.getType() == Material.BEDROCK) continue;
                    if (block.getType() != centerType) continue;
                    
                    block.breakNaturally(player.getInventory().getItemInMainHand());
                    
                    player.getWorld().spawnParticle(
                        Particle.BLOCK,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        10, 0.3, 0.3, 0.3,
                        block.getBlockData()
                    );
                }
            }
        }
    }

    private ItemStack createTitanHammer() {
        ItemStack hammer = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = hammer.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            "&6&l⚒ &e&lTitan Hammer &6&l⚒"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "A legendary hammer forged by titans");
        lore.add(ChatColor.GRAY + "that can reshape the world itself.");
        lore.add("");
        lore.add(ChatColor.GOLD + "✦ Damage Bonus: " + ChatColor.RED + "+5% per level");
        lore.add(ChatColor.GOLD + "✦ Range Mining: " + ChatColor.LIGHT_PURPLE + "Unlocked at Level 5");
        lore.add("");
        lore.add(ChatColor.YELLOW + "▸ Shift + Right Click for stats");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "© Blooddev - PrimeHost");
        
        meta.setLore(lore);
        meta.setCustomModelData(2025);
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        
        hammer.setItemMeta(meta);
        return hammer;
    }

    private boolean isTitanHammer(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_AXE) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == 2025;
    }

    private double getXPForBlock(Material material) {
        switch (material) {
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return 50.0;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return 40.0;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                return 30.0;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return 20.0;
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                return 10.0;
            case ANCIENT_DEBRIS:
                return 100.0;
            case OBSIDIAN:
                return 25.0;
            default:
                return 5.0;
        }
    }

    private PlayerStats getPlayerStats(UUID uuid) {
        return playerStats.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    private static class PlayerStats {
        int level = 1;
        double xp = 0;
        long blocksDestroyed = 0;

        void addXP(double amount) {
            xp += amount;
        }

        double getRequiredXP() {
            return 100 + (level * 50);
        }
    }
}
