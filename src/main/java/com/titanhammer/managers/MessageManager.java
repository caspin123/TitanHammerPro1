package com.titanhammer.managers;

import com.titanhammer.TitanHammerPro;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final TitanHammerPro plugin;
    private FileConfiguration messagesEn;
    private FileConfiguration messagesAr;
    private final Map<String, String> playerLanguage = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageManager(TitanHammerPro plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        // English messages
        File enFile = new File(plugin.getDataFolder(), "messages_en.yml");
        if (!enFile.exists()) plugin.saveResource("messages_en.yml", false);
        messagesEn = YamlConfiguration.loadConfiguration(enFile);

        // Arabic messages
        File arFile = new File(plugin.getDataFolder(), "messages_ar.yml");
        if (!arFile.exists()) plugin.saveResource("messages_ar.yml", false);
        messagesAr = YamlConfiguration.loadConfiguration(arFile);
    }

    public void reload() {
        loadMessages();
    }

    public void setPlayerLanguage(String uuid, String lang) {
        playerLanguage.put(uuid, lang);
    }

    public String getPlayerLanguage(Player player) {
        return playerLanguage.getOrDefault(player.getUniqueId().toString(),
                plugin.getConfigManager().getDefaultLanguage());
    }

    public String getRaw(Player player, String key) {
        String lang = getPlayerLanguage(player);
        FileConfiguration messages = lang.equals("ar") ? messagesAr : messagesEn;
        return messages.getString(key, "§cMissing: " + key);
    }

    public String getRaw(Player player, String key, Map<String, String> placeholders) {
        String msg = getRaw(player, key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace(entry.getKey(), entry.getValue());
        }
        return msg;
    }

    public Component get(Player player, String key) {
        return colorize(getRaw(player, key));
    }

    public Component get(Player player, String key, Map<String, String> placeholders) {
        return colorize(getRaw(player, key, placeholders));
    }

    public void send(Player player, String key) {
        player.sendMessage(get(player, key));
    }

    public void send(Player player, String key, Map<String, String> placeholders) {
        player.sendMessage(get(player, key, placeholders));
    }

    public void sendActionBar(Player player, String key, Map<String, String> placeholders) {
        player.sendActionBar(get(player, key, placeholders));
    }

    public Component colorize(String text) {
        // Support both legacy & color codes and MiniMessage
        if (text.contains("&") || text.contains("§")) {
            String colored = ChatColor.translateAlternateColorCodes('&', text);
            return LegacyComponentSerializer.legacySection().deserialize(colored);
        }
        return miniMessage.deserialize(text);
    }

    public String colorizeString(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Quick placeholder builder
    public static Map<String, String> placeholders(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
