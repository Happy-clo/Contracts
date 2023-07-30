package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.Contracts;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class ConfigFile {
    private final File file;
    private final String name;
    private final FileConfiguration config;

    public ConfigFile(Player player) {
        this(player.getUniqueId());
    }

    public ConfigFile(UUID uuid) {
        this(Contracts.plugin, "/userdata", uuid.toString());
    }


    public ConfigFile(String name) {
        this(Contracts.plugin, "", name);
    }

    public ConfigFile(String folder, String name) {
        this(Contracts.plugin, folder, name);
    }

    public ConfigFile(Plugin plugin, String folder, String name) {
        config = YamlConfiguration.loadConfiguration(file = new File(plugin.getDataFolder() + folder, (this.name = name) + ".yml"));
    }

    public ConfigFile(File file) {
        this.file = file;
        this.name = file.getName();
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean exists() {
        return file.exists();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            Contracts.log(Level.SEVERE, "Could not save " + name + ".yml: " + exception.getMessage());
        }
    }

    public void delete() {
        if (file.exists())
            if (!file.delete())
                Contracts.plugin.getLogger().log(Level.SEVERE, "Could not delete " + name + ".yml.");
    }
}