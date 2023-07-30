package fr.phoenix.contracts;

import fr.phoenix.contracts.command.ContractTreeRoot;
import fr.phoenix.contracts.compat.Metrics;
import fr.phoenix.contracts.compat.adventure.AdventureParser;
import fr.phoenix.contracts.compat.placeholder.DefaultPlaceholderParser;
import fr.phoenix.contracts.compat.placeholder.PlaceholderAPIParser;
import fr.phoenix.contracts.compat.placeholder.PlaceholderParser;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.contract.list.SalaryContract;
import fr.phoenix.contracts.listener.PlayerListener;
import fr.phoenix.contracts.manager.ConfigManager;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.manager.MiddlemenManager;
import fr.phoenix.contracts.manager.PluginMessageManager;
import fr.phoenix.contracts.manager.data.DataProvider;
import fr.phoenix.contracts.manager.data.sql.MySQLDataProvider;
import fr.phoenix.contracts.manager.data.yaml.YamlDataProvider;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.version.ServerVersion;
import fr.phoenix.contracts.version.SpigotPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class Contracts extends JavaPlugin {
    public static Contracts plugin;

    public Economy economy;

    public final ConfigManager configManager = new ConfigManager();
    public DataProvider dataProvider = new YamlDataProvider();
    public final MiddlemenManager middlemenManager = new MiddlemenManager();
    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    public PluginMessageManager pluginMessageManager = new PluginMessageManager();

    public AdventureParser adventureParser;

    public ServerVersion version;
    public boolean shouldDebugSQL;

    @Override
    public void onEnable() {
        // Register eco
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null)
            economy = provider.getProvider();
        else {
            getLogger().log(Level.SEVERE, "Could not hook onto Vault, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            version = new ServerVersion(Bukkit.getServer().getClass());
            getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
        } catch (Exception exception) {
            getLogger().log(Level.INFO, net.md_5.bungee.api.ChatColor.RED + "Your server version is not compatible.");
            exception.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderParser = new PlaceholderAPIParser();
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        }


        //SQL
        if (getConfig().isConfigurationSection("mysql") && getConfig().getBoolean("mysql.enabled"))
            dataProvider = new MySQLDataProvider(getConfig());
        shouldDebugSQL = getConfig().getBoolean("mysql.debug");

        // Metrics data
        new Metrics(this, 15383);

        // Update checker
        new SpigotPlugin(106871, this).checkForUpdate();

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Register the root command
        getCommand("contract").setExecutor(new ContractTreeRoot("contract", "contracts.contracts"));

        // Load managers (the order is important: player must be loaded at the end)
        configManager.load();
        adventureParser = new AdventureParser();
        if (dataProvider instanceof MySQLDataProvider)
            pluginMessageManager.load();
        InventoryManager.load();

        //We load only if there is at least one player online or if we are using YamlDataProvider.
        if (dataProvider instanceof YamlDataProvider || Bukkit.getOnlinePlayers().size() != 0) {
            dataProvider.getContractManager().load();
            dataProvider.getPlayerDataManager().load();
        }

        //Removes reviews.yml to make reviews stored in playerData.
        transferReviewToPlayerData();

        //Checks every 10 min to remove from memory playerData that hasn't been used for the past day.
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : dataProvider.getPlayerDataManager().getAllUUID())
                    if ((System.currentTimeMillis() - dataProvider.getPlayerDataManager().getLastTimeUsed(uuid)) > 1000 * 3600 * 24)
                        dataProvider.getPlayerDataManager().remove(uuid);
            }
        }.runTaskTimer(Contracts.plugin, 0L, 600 * 20L);


        //Proceeds the payment for SALARY CONTRACT
        new BukkitRunnable() {
            @Override
            public void run() {
                dataProvider.getContractManager().getContractsOfType(ContractType.SALARY).stream()
                        .filter(contract -> contract.getState() == ContractState.OPEN)
                        .map(contract -> (SalaryContract) contract)
                        .filter(contract -> (System.currentTimeMillis() - contract.getLastPeriodTime()) > 1000 * 3600 * 24 * contract.getSalaryPeriod())
                        .forEach(contract -> contract.whenPeriodEnd());
            }
        }.runTaskTimer(Contracts.plugin, 0L, 600 * 20L);


        //Assign MIDDLEMAN_DISPUTED contracts to middleman.
        new BukkitRunnable() {
            @Override
            public void run() {
                middlemenManager.assignContracts();
            }
        }.runTaskTimer(Contracts.plugin, 0L, 60 * 20L);

        //Transfer disputed contracts to resolved if they passed the callAdminPeriod.
        new BukkitRunnable() {
            @Override
            public void run() {
                dataProvider.getContractManager().getContracts().stream()
                        .filter(contract -> contract.getState() == ContractState.MIDDLEMAN_RESOLVED)
                        .filter(contract -> (System.currentTimeMillis() - contract.getEnteringTime(contract.getState())) > 1000 * 3600 * 24 * Contracts.plugin.configManager.callAdminPeriod)
                        .forEach(contract -> contract.whenResolvedFromDispute());
            }
        }.runTaskTimer(Contracts.plugin, 0L, Contracts.plugin.configManager.checkIfResolvedPeriod * 1000 * 3600);
    }


    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onDisable() {
        // Executes all the pending asynchronous task (like saving the playerData)
        Bukkit.getScheduler().getPendingTasks().forEach(worker -> {
            if (worker.getOwner().equals(this)) {
                ((Runnable) worker).run();
            }
        });
        //We don't need to save contract & player data as there are automatically saved & synchronized.
        // Close MySQL data provider (memory leaks)
        if (dataProvider instanceof MySQLDataProvider)
            ((MySQLDataProvider) dataProvider).close();
        else {
            Bukkit.getOnlinePlayers().forEach(player -> dataProvider.getPlayerDataManager().save(PlayerData.get(player)));
            dataProvider.getContractManager().getContracts().forEach(contract -> dataProvider.getContractManager().saveContract(contract));
        }
    }

    public static void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    public static void log(String message) {
        plugin.getLogger().log(Level.WARNING, message);
    }

    public static void sqlDebug(String s) {
        if (!plugin.shouldDebugSQL) return;
        plugin.getLogger().warning("- [SQL Debug] " + s);
    }


    /**
     * Modifications to put the review data in the userdata.
     */
    private void transferReviewToPlayerData() {
        File reviewFile = new File(getDataFolder(), "review.yml");
        if (reviewFile.exists()) {
            File userDataFolder = new File(getDataFolder(), "userdata");
            FileConfiguration config = new ConfigFile("review").getConfig();
            for (File file : userDataFolder.listFiles()) {
                ConfigFile playerConfig = new ConfigFile(file);
                //Gets the reviews UUID and delete the review section fo the playerData.
                List<String> reviews = playerConfig.getConfig().getStringList("reviews");
                playerConfig.getConfig().set("reviews", null);
                for (String reviewId : reviews) {
                    playerConfig.getConfig().set("reviews." + reviewId + ".date", config.getLong(reviewId + ".date"));
                    playerConfig.getConfig().set("reviews." + reviewId + ".contract-id", config.getString(reviewId + ".contract-id"));
                    playerConfig.getConfig().set("reviews." + reviewId + ".reviewed", config.getString(reviewId + ".reviewed"));
                    playerConfig.getConfig().set("reviews." + reviewId + ".reviewer", config.getString(reviewId + ".reviewer"));
                    playerConfig.getConfig().set("reviews." + reviewId + ".notation", config.getInt(reviewId + ".notation"));
                    playerConfig.getConfig().set("reviews." + reviewId + ".comment", config.getStringList(reviewId + ".comment"));
                }
                playerConfig.save();
            }

            reviewFile.delete();

        }
    }

    public String parseColors(String format) {
        return adventureParser.parse(format);
    }

    public List<String> parseColors(List<String> format) {
        return new ArrayList<>(adventureParser.parse(format));
    }
}
