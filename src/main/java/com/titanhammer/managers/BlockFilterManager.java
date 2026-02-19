package com.titanhammer.managers;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlockFilterManager {

    private final TitanHammerPro plugin;
    private final List<Material> filterableBlocks;

    public BlockFilterManager(TitanHammerPro plugin) {
        this.plugin = plugin;
        this.filterableBlocks = loadFilterableBlocks();
    }

    private List<Material> loadFilterableBlocks() {
        List<Material> blocks = new ArrayList<>();
        // Common mining blocks
        blocks.add(Material.STONE);
        blocks.add(Material.COBBLESTONE);
        blocks.add(Material.DIRT);
        blocks.add(Material.GRAVEL);
        blocks.add(Material.SAND);
        blocks.add(Material.NETHERRACK);
        blocks.add(Material.GRANITE);
        blocks.add(Material.DIORITE);
        blocks.add(Material.ANDESITE);
        blocks.add(Material.TUFF);
        blocks.add(Material.DEEPSLATE);
        blocks.add(Material.COBBLED_DEEPSLATE);
        blocks.add(Material.CALCITE);
        blocks.add(Material.SANDSTONE);
        blocks.add(Material.RED_SANDSTONE);
        blocks.add(Material.BASALT);
        blocks.add(Material.BLACKSTONE);
        blocks.add(Material.END_STONE);
        blocks.add(Material.SOUL_SAND);
        blocks.add(Material.SOUL_SOIL);
        blocks.add(Material.CLAY);
        blocks.add(Material.TERRACOTTA);
        blocks.add(Material.MOSSY_COBBLESTONE);
        blocks.add(Material.SMOOTH_BASALT);

        // Ore blocks
        blocks.add(Material.COAL_ORE);
        blocks.add(Material.DEEPSLATE_COAL_ORE);
        blocks.add(Material.IRON_ORE);
        blocks.add(Material.DEEPSLATE_IRON_ORE);
        blocks.add(Material.GOLD_ORE);
        blocks.add(Material.DEEPSLATE_GOLD_ORE);
        blocks.add(Material.DIAMOND_ORE);
        blocks.add(Material.DEEPSLATE_DIAMOND_ORE);
        blocks.add(Material.EMERALD_ORE);
        blocks.add(Material.DEEPSLATE_EMERALD_ORE);
        blocks.add(Material.LAPIS_ORE);
        blocks.add(Material.DEEPSLATE_LAPIS_ORE);
        blocks.add(Material.REDSTONE_ORE);
        blocks.add(Material.DEEPSLATE_REDSTONE_ORE);
        blocks.add(Material.COPPER_ORE);
        blocks.add(Material.DEEPSLATE_COPPER_ORE);
        blocks.add(Material.NETHER_GOLD_ORE);
        blocks.add(Material.NETHER_QUARTZ_ORE);
        blocks.add(Material.ANCIENT_DEBRIS);

        return blocks;
    }

    public List<Material> getFilterableBlocks() {
        return filterableBlocks;
    }

    /**
     * Processes a block break based on filter settings.
     * Returns: true if the event should be modified, false for normal behavior.
     */
    public BlockFilterResult processBlock(Player player, Material blockType) {
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        PlayerProfile.FilterMode mode = profile.getFilterMode();

        if (mode == PlayerProfile.FilterMode.DISABLED) {
            return BlockFilterResult.NORMAL;
        }

        if (mode == PlayerProfile.FilterMode.AUTO_COLLECT) {
            if (profile.getAutoCollectBlocks().contains(blockType)) {
                return BlockFilterResult.COLLECT;
            }
        }

        if (mode == PlayerProfile.FilterMode.AUTO_DELETE) {
            if (profile.getAutoDeleteBlocks().contains(blockType)) {
                return BlockFilterResult.DELETE;
            }
        }

        return BlockFilterResult.NORMAL;
    }

    /**
     * Adds drops directly to player inventory, dropping overflow on ground.
     */
    public void collectToInventory(Player player, Collection<ItemStack> drops) {
        for (ItemStack drop : drops) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(drop);
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    public boolean isAutoCollecting(Player player, Material mat) {
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        return profile.getFilterMode() == PlayerProfile.FilterMode.AUTO_COLLECT
                && profile.getAutoCollectBlocks().contains(mat);
    }

    public boolean isAutoDeleting(Player player, Material mat) {
        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(player);
        return profile.getFilterMode() == PlayerProfile.FilterMode.AUTO_DELETE
                && profile.getAutoDeleteBlocks().contains(mat);
    }

    public enum BlockFilterResult {
        NORMAL,
        COLLECT,
        DELETE
    }
}
