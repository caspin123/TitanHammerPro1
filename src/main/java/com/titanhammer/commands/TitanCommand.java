package com.titanhammer.commands;

import com.titanhammer.TitanHammerPro;
import com.titanhammer.gui.MainGUI;
import com.titanhammer.models.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TitanCommand implements CommandExecutor, TabCompleter {

    private final TitanHammerPro plugin;

    public TitanCommand(TitanHammerPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (!player.hasPermission("titanhammer.use")) {
                    plugin.getMessageManager().send(player, "no-permission");
                    return true;
                }
                MainGUI.open(player, plugin);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "give" -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            case "setlevel" -> handleSetLevel(sender, args);
            case "addxp" -> handleAddXp(sender, args);
            case "info" -> handleInfo(sender, args);
            case "lang", "language" -> handleLanguage(sender, args);
            case "help" -> sendHelp(sender);
            default -> {
                sender.sendMessage(c("&cUnknown subcommand. Use /titanhammer help"));
            }
        }

        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("titanhammer.admin")) {
            sender.sendMessage(c("&cNo permission!"));
            return;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(c("&cPlayer not found!"));
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(c("&cSpecify a player: /titanhammer give <player>"));
            return;
        }

        target.getInventory().addItem(plugin.getItemManager().createHammer(target));
        sender.sendMessage(c("&aGave TitanHammer to &e" + target.getName()));
        if (target != sender) {
            plugin.getMessageManager().send(target, "hammer-received");
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("titanhammer.admin")) {
            sender.sendMessage(c("&cNo permission!"));
            return;
        }

        plugin.reload();
        sender.sendMessage(c("&a&lTitanHammer Pro &areloaded successfully!"));
    }

    private void handleSetLevel(CommandSender sender, String[] args) {
        if (!sender.hasPermission("titanhammer.admin")) {
            sender.sendMessage(c("&cNo permission!"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(c("&cUsage: /titanhammer setlevel <player> <level>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(c("&cPlayer not found!"));
            return;
        }

        try {
            int level = Integer.parseInt(args[2]);
            PlayerProfile profile = plugin.getPlayerDataManager().getProfile(target);
            profile.setLevel(level);
            plugin.getItemManager().refreshPlayerHammer(target);
            sender.sendMessage(c("&aSet &e" + target.getName() + "&a's level to &e" + level));
        } catch (NumberFormatException e) {
            sender.sendMessage(c("&cInvalid number!"));
        }
    }

    private void handleAddXp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("titanhammer.admin")) {
            sender.sendMessage(c("&cNo permission!"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(c("&cUsage: /titanhammer addxp <player> <amount>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(c("&cPlayer not found!"));
            return;
        }

        try {
            double xp = Double.parseDouble(args[2]);
            PlayerProfile profile = plugin.getPlayerDataManager().getProfile(target);
            profile.addXp(xp);

            while (profile.canLevelUp() && profile.getLevel() < plugin.getConfigManager().getMaxLevel()) {
                profile.tryLevelUp();
                plugin.getEffectsManager().playLevelUp(target);
            }

            plugin.getItemManager().refreshPlayerHammer(target);
            sender.sendMessage(c("&aAdded &e" + xp + " XP &ato &e" + target.getName()));
        } catch (NumberFormatException e) {
            sender.sendMessage(c("&cInvalid number!"));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(c("&cPlayer not found!"));
                return;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(c("&cSpecify a player!"));
            return;
        }

        PlayerProfile profile = plugin.getPlayerDataManager().getProfile(target);
        sender.sendMessage(c("&8&m                                        "));
        sender.sendMessage(c("&c&l⚡ TitanHammer &7- &e" + target.getName()));
        sender.sendMessage(c("&8&m                                        "));
        sender.sendMessage(c("&7Level: &e" + profile.getLevel()));
        sender.sendMessage(c("&7XP: &b" + String.format("%.1f", profile.getXp()) + "/" + String.format("%.0f", profile.getXpForNextLevel())));
        sender.sendMessage(c("&7Total XP: &b" + String.format("%.0f", profile.getTotalXp())));
        sender.sendMessage(c("&7Filter Mode: &a" + profile.getFilterMode().name()));
        sender.sendMessage(c("&7Active Skills: &a" + profile.getSkillLevels().size()));
        sender.sendMessage(c("&8&m                                        "));
    }

    private void handleLanguage(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(c("&cPlayers only!"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(c("&cUsage: /titanhammer lang <en/ar>"));
            return;
        }

        String lang = args[1].toLowerCase();
        if (!lang.equals("en") && !lang.equals("ar")) {
            sender.sendMessage(c("&cAvailable languages: en, ar"));
            return;
        }

        plugin.getMessageManager().setPlayerLanguage(player.getUniqueId().toString(), lang);
        plugin.getMessageManager().send(player, "language-changed");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(c("&8&m                                        "));
        sender.sendMessage(c("&c&l⚡ TitanHammer Pro &7- Commands"));
        sender.sendMessage(c("&8&m                                        "));
        sender.sendMessage(c("&e/titanhammer &7- Open main menu"));
        sender.sendMessage(c("&e/titanhammer give [player] &7- Give hammer"));
        sender.sendMessage(c("&e/titanhammer setlevel <player> <level> &7- Set level"));
        sender.sendMessage(c("&e/titanhammer addxp <player> <amount> &7- Add XP"));
        sender.sendMessage(c("&e/titanhammer info [player] &7- View info"));
        sender.sendMessage(c("&e/titanhammer lang <en/ar> &7- Change language"));
        sender.sendMessage(c("&e/titanhammer reload &7- Reload config"));
        sender.sendMessage(c("&8&m                                        "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(args[0], "give", "reload", "setlevel", "addxp", "info", "lang", "help");
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("setlevel") || sub.equals("addxp") || sub.equals("info")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (sub.equals("lang") || sub.equals("language")) {
                return filterStartsWith(args[1], "en", "ar");
            }
        }
        return new ArrayList<>();
    }

    private List<String> filterStartsWith(String input, String... options) {
        return Arrays.stream(options)
                .filter(o -> o.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
