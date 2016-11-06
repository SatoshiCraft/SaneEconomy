package com.satoshicraft.economy.utils;

import com.google.common.collect.ImmutableMap;
import com.satoshicraft.economy.SatoshiEconomy;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AppleDash on 8/5/2016.
 * Blackjack is still best pony.
 */
public class I18n {
    private static final I18n INSTANCE = new I18n(SatoshiEconomy.getInstance());
    private final SatoshiEconomy plugin;
    private final Map<String, String> translations = new HashMap<>();

    private I18n(SatoshiEconomy plugin) {
        this.plugin = plugin;
    }

    public void loadTranslations() {
        File configFile = new File(plugin.getDataFolder(), "messages.yml");

        if (configFile.exists()) { // Attempt to merge any new keys from the JAR's messages.yml into the copy in the plugin's data folder
            YamlConfiguration configFileYaml = YamlConfiguration.loadConfiguration(configFile);
            for (Map jarMap : configFileYaml.getMapList("messages")) {
                boolean has = false;
                String key = jarMap.get("message").toString();

                for (Map fileMap : configFileYaml.getMapList("messages")) {
                    if (fileMap.get("message").toString().equals(key)) {
                        has = true;
                        break;
                    }
                }

                if (!has) { // Folder messages.yml does not have this key, add it.
                    List<Map> map = new ArrayList<>(configFileYaml.getMapList("messages"));
                    map.add(ImmutableMap.of("message", key));
                    configFileYaml.set("messages", map);
                }
            }

            try {
                configFileYaml.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save translations file.", e);
            }
        } else {
        }

        YamlConfiguration configFileYaml = YamlConfiguration.loadConfiguration(configFile);
        configFileYaml.getMapList("messages").stream().filter(map -> map.containsKey("translation")).forEach(map -> {
            translations.put(map.get("message").toString(), map.get("translation").toString());
        });
    }

    private String translate(String input) {
        return translations.containsKey(input) ? translations.get(input) : input;
    }

    public static String _(String s) {
        return INSTANCE.translate(s);
    }

    public static I18n getInstance() {
        return INSTANCE;
    }
}
