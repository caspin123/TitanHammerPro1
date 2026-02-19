package com.titanhammer.listeners;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.managers.BlockFilterManager;
import com.titanhammer.models.PlayerProfile;
import com.titanhammer.models.Skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlockBreakListener implements Listener {

    private final TitanHammerPro plugin;
    private final Set<UUID> processingAreaBreak = new HashSet<>();

    // Auto-smelt mappings
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();

    static {
        SMELT_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.COBBLESTONE, Material.STONE);
        SMELT_MAP.put(Material.SAND, Material.GLASS);
        SMELT_MAP.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    }

    public BlockBreakListener(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Only process TitanHammer breaks
        if (!plugin.getItemManager().isTitanHammer(item)) return;

        // Anti-creative exploit
        if (plugin.getConfigManager().isAntiCreative() && player.getGameMode() == GameMode.CREATIVE) return;

        // Skip if we're processing an area break sub-block
        if (processingAreaBreak.contains(player.getUniqueId())) return;

        Block block = event.getBlock();
        Material blockType = block.getType();
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);

        // ---- XP Gain ----
        double baseXp = plugin.getConfigManager().getBlockBreakXp();
        double xpBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.XP_BOOST);
        double finalXp = baseXp * (1.0 + xpBoost);
        profile.addXp(finalXp);

        // Check level up
        while (profile.canLevelUp() && profile.getLevel() < plugin.getConfigManager().getMaxLevel()) {
            profile.tryLevelUp();
            plugin.getEffectsManager().playLevelUp(player);
            plugin.getMessageManager().send(player, "level-up",
                    Map.of("{level}", String.valueOf(profile.getLevel())));
        }

        // ---- Block Filter Processing ----
        BlockFilterManager.BlockFilterResult filterResult = plugin.getBlockFilterManager().processBlock(player, blockType);

        switch (filterResult) {
            case COLLECT -> {
                event.setDropItems(false);
                Collection<ItemStack> drops = processDrops(block, item, player);
                plugin.getBlockFilterManager().collectToInventory(player, drops);
                plugin.getEffectsManager().playAutoCollect(player);
            }
            case DELETE -> {
                event.setDropItems(false);
                plugin.getEffectsManager().playAutoDelete(player, block.getLocation());
            }
            case NORMAL -> {
                // Apply auto-smelt if active
                double autoSmelt = plugin.getSkillManager().getSkillEffect(player, SkillType.AUTO_SMELT);
                if (autoSmelt > 0 && SMELT_MAP.containsKey(blockType)) {
                    event.setDropItems(false);
                    Material smelted = SMELT_MAP.get(blockType);
                    int amount = calculateFortune(player);
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, amount));
                    plugin.getEffectsManager().playAutoSmelt(block.getLocation());
                }
            }
        }

        // ---- Area Break ----
        double areaBreak = plugin.getSkillManager().getSkillEffect(player, SkillType.AREA_BREAK);
        if (areaBreak > 0 && !processingAreaBreak.contains(player.getUniqueId())) {
            int radius = (int) areaBreak;
            processAreaBreak(player, block, radius);
        }

        // ---- Effects ----
        plugin.getEffectsManager().playBlockBreak(player, block.getLocation());

        // ---- Update hammer lore ----
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getItemManager().refreshPlayerHammer(player), 1L);
    }

    private void processAreaBreak(Player player, Block center, int radius) {
        processingAreaBreak.add(player.getUniqueId());

        try {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block nearby = center.getRelative(x, y, z);
                        if (nearby.getType().isAir() || nearby.getType() == Material.BEDROCK) continue;
                        if (!nearby.getType().isSolid()) continue;

                        // Create a block break event to respect protections
                        BlockBreakEvent subEvent = new BlockBreakEvent(nearby, player);
                        Bukkit.getPluginManager().callEvent(subEvent);

                        if (!subEvent.isCancelled()) {
                            // Process filter for area blocks too
                            BlockFilterManager.BlockFilterResult result =
                                    plugin.getBlockFilterManager().processBlock(player, nearby.getType());

                            switch (result) {
                                case COLLECT -> {
                                    Collection<ItemStack> drops = nearby.getDrops(
                                            player.getInventory().getItemInMainHand());
                                    plugin.getBlockFilterManager().collectToInventory(player, drops);
                                    nearby.setType(Material.AIR);
                                }
                                case DELETE -> nearby.setType(Material.AIR);
                                case NORMAL -> {
                                    nearby.breakNaturally(player.getInventory().getItemInMainHand());
                                }
                            }
                        }
                    }
                }
            }

            plugin.getEffectsManager().playAreaBreak(player, center.getLocation());
        } finally {
            processingAreaBreak.remove(player.getUniqueId());
        }
    }

    private Collection<ItemStack> processDrops(Block block, ItemStack tool, Player player) {
        Collection<ItemStack> drops = new ArrayList<>(block.getDrops(tool));

        // Fortune boost
        double fortuneBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.FORTUNE_BOOST);
        if (fortuneBoost > 0) {
            Random random = new Random();
            for (ItemStack drop : drops) {
                if (random.nextDouble() < fortuneBoost) {
                    drop.setAmount(drop.getAmount() + 1);
                }
            }
        }

        // Auto-smelt
        double autoSmelt = plugin.getSkillManager().getSkillEffect(player, SkillType.AUTO_SMELT);
        if (autoSmelt > 0) {
            List<ItemStack> smelted = new ArrayList<>();
            for (ItemStack drop : drops) {
                Material smeltResult = SMELT_MAP.get(drop.getType());
                if (smeltResult != null) {
                    smelted.add(new ItemStack(smeltResult, drop.getAmount()));
                } else {
                    smelted.add(drop);
                }
            }
            return smelted;
        }

        return drops;
    }

    private int calculateFortune(Player player) {
        double fortuneBoost = plugin.getSkillManager().getSkillEffect(player, SkillType.FORTUNE_BOOST);
        Random random = new Random();
        int amount = 1;
        if (fortuneBoost > 0 && random.nextDouble() < fortuneBoost) {
            amount += 1 + random.nextInt(2);
        }
        return amount;
    }
}
