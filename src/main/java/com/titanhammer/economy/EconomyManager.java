package com.titanhammer.economy;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.models.PlayerProfile;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final TitanHammerPro plugin;
    private Economy vaultEconomy;
    private PlayerPointsAPI playerPointsAPI;

    public EconomyManager(TitanHammerPro plugin) {
        this.plugin = plugin;
        setupVault();
        setupPlayerPoints();
    }

    private void setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found - Vault economy disabled");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            vaultEconomy = rsp.getProvider();
            plugin.getLogger().info("Vault economy hooked successfully!");
        }
    }

    private void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            plugin.getLogger().info("PlayerPoints not found - PlayerPoints economy disabled");
            return;
        }
        PlayerPoints pp = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (pp != null) {
            playerPointsAPI = pp.getAPI();
            plugin.getLogger().info("PlayerPoints hooked successfully!");
        }
    }

    public boolean has(Player player, double amount, String type) {
        return switch (type.toLowerCase()) {
            case "vault" -> vaultEconomy != null && vaultEconomy.has(player, amount);
            case "playerpoints" -> playerPointsAPI != null && playerPointsAPI.look(player.getUniqueId()) >= (int) amount;
            case "custom" -> plugin.getPlayerDataManager().getProfile(player).getCustomBalance() >= amount;
            default -> false;
        };
    }

    public boolean withdraw(Player player, double amount, String type) {
        return switch (type.toLowerCase()) {
            case "vault" -> {
                if (vaultEconomy != null && vaultEconomy.has(player, amount)) {
                    yield vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
                }
                yield false;
            }
            case "playerpoints" -> {
                if (playerPointsAPI != null) {
                    yield playerPointsAPI.take(player.getUniqueId(), (int) amount);
                }
                yield false;
            }
            case "custom" -> plugin.getPlayerDataManager().getProfile(player).withdrawCustom(amount);
            default -> false;
        };
    }

    public boolean deposit(Player player, double amount, String type) {
        return switch (type.toLowerCase()) {
            case "vault" -> {
                if (vaultEconomy != null) {
                    yield vaultEconomy.depositPlayer(player, amount).transactionSuccess();
                }
                yield false;
            }
            case "playerpoints" -> {
                if (playerPointsAPI != null) {
                    yield playerPointsAPI.give(player.getUniqueId(), (int) amount);
                }
                yield false;
            }
            case "custom" -> {
                plugin.getPlayerDataManager().getProfile(player).addCustomBalance(amount);
                yield true;
            }
            default -> false;
        };
    }

    public double getBalance(Player player, String type) {
        return switch (type.toLowerCase()) {
            case "vault" -> vaultEconomy != null ? vaultEconomy.getBalance(player) : 0;
            case "playerpoints" -> playerPointsAPI != null ? playerPointsAPI.look(player.getUniqueId()) : 0;
            case "custom" -> plugin.getPlayerDataManager().getProfile(player).getCustomBalance();
            default -> 0;
        };
    }

    public String formatCurrency(double amount, String type) {
        return switch (type.toLowerCase()) {
            case "vault" -> vaultEconomy != null ? vaultEconomy.format(amount) : "$" + String.format("%.2f", amount);
            case "playerpoints" -> (int) amount + " Points";
            case "custom" -> String.format("%.0f", amount) + " Coins";
            default -> String.format("%.2f", amount);
        };
    }

    public boolean hasVault() { return vaultEconomy != null; }
    public boolean hasPlayerPoints() { return playerPointsAPI != null; }
}
