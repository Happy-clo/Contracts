package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.contract.parameter.ParameterType;
import fr.phoenix.contracts.utils.ConfigFile;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    public int reviewPeriod, callDisputePeriod, callAdminPeriod, maxCommentCharPerLine, maxCommentLines,
            defaultNotation, maxEmployerContract, maxContractsPerMiddleman, checkIfResolvedPeriod, contractTaxes, middlemanCommission;
    public char colorCodeChar;
    public DecimalFormat decimalFormat;

    public List<String> discordWebhooks = new ArrayList<>();

    public FileConfiguration discordMessages;

    public boolean sendMessageWhenLogin, sendDiscordMessages;

    public void load() {
        discordMessages = new ConfigFile("/language", "discord-messages").getConfig();
        reviewPeriod = Contracts.plugin.getConfig().getInt("review-period");
        callDisputePeriod = Contracts.plugin.getConfig().getInt("call-dispute-period");
        checkIfResolvedPeriod = Contracts.plugin.getConfig().getInt("check-if-resolved-period");
        maxCommentLines = Contracts.plugin.getConfig().getInt("max-comment-lines");
        maxCommentCharPerLine = Contracts.plugin.getConfig().getInt("max-comment-char-per-line");
        callAdminPeriod = Contracts.plugin.getConfig().getInt("call-admin-period");
        defaultNotation = Contracts.plugin.getConfig().getInt("default-notation");
        colorCodeChar = Contracts.plugin.getConfig().getString("color-code-char").charAt(0);
        maxContractsPerMiddleman = Contracts.plugin.getConfig().getInt("max-contract-per-middleman");
        middlemanCommission = Contracts.plugin.getConfig().getInt("middleman-commission");
        contractTaxes = Contracts.plugin.getConfig().getInt("contract-taxes");
        maxEmployerContract = Contracts.plugin.getConfig().getInt("max-employer-contract", 5);
        decimalFormat = new DecimalFormat(Contracts.plugin.getConfig().getString("decimal-format"));
        sendMessageWhenLogin = Contracts.plugin.getConfig().getBoolean("send-message-when-login");
        sendDiscordMessages = Contracts.plugin.getConfig().getBoolean("send-discord-messages");
        discordWebhooks = Contracts.plugin.getConfig().getStringList("discord-webhooks");
        // Load default files
        loadDefaultFile("commands.yml");
        // Save default messages
        ConfigFile messages = new ConfigFile("/language", "messages");
        for (Message key : Message.values()) {
            String path = key.getPath();
            if (!messages.getConfig().contains(path)) {
                messages.getConfig().set(path + ".format", key.getCached());
                if (key.hasSound()) {
                    messages.getConfig().set(path + ".sound.name", key.getSound().getSound().name());
                    messages.getConfig().set(path + ".sound.vol", key.getSound().getVolume());
                    messages.getConfig().set(path + ".sound.pitch", key.getSound().getPitch());
                }
            }
        }
        messages.save();

        // Reload messages
        FileConfiguration messagesConfig = new ConfigFile("/language", "messages").getConfig();
        for (Message message : Message.values())
            try {
                message.update(messagesConfig.getConfigurationSection(message.getPath()));
            } catch (IllegalArgumentException exception) {
                Contracts.plugin.getLogger().log(Level.WARNING, "Could not reload message " + message.name() + ": " + exception.getMessage());
            }

        // Save default parameter description
        ConfigFile parametersDescription = new ConfigFile("/language", "parameters");
        for (ParameterType key : ParameterType.values()) {
            String path = key.getId();
            if (!parametersDescription.getConfig().contains(path)) {
                parametersDescription.getConfig().set(path + ".name", key.getName());
                parametersDescription.getConfig().set(path + ".description", key.getDescription());
            }
        }
        parametersDescription.save();

        // Reload parameters description
        FileConfiguration parametersConfig = parametersDescription.getConfig();
        for (ParameterType parameterType : ParameterType.values())
            try {
                parameterType.update(parametersConfig.getConfigurationSection(parameterType.getId()));
            } catch (IllegalArgumentException exception) {
                Contracts.plugin.getLogger().log(Level.WARNING, "Could not reload parameter " + parameterType.getId() + ": " + exception.getMessage());
            }


        loadDefaultFile("language/type-specific-description.yml");
        loadDefaultFile("language/discord-messages.yml");
        // Reload type specific description
        FileConfiguration typeSpecificDescriptionConfig = new ConfigFile("/language", "type-specific-description").getConfig();
        for (ContractType contractType : ContractType.values())
            try {
                contractType.update(typeSpecificDescriptionConfig.getConfigurationSection(contractType.getId()));
            } catch (Exception exception) {
                Contracts.plugin.getLogger().log(Level.WARNING, "Could not reload specific type description for " + contractType.getId() + ": " + exception.getMessage());
            }

        //Load GUI
        InventoryManager.load();

    }

    public String getDiscordMessage(String id) {
        return discordMessages.getString(id);
    }

    public void loadDefaultFile(String name) {
        loadDefaultFile("", name);
    }

    public void loadDefaultFile(String path, String name) {
        String newPath = path.isEmpty() ? "" : "/" + path;
        File folder = new File(Contracts.plugin.getDataFolder() + (newPath));
        if (!folder.exists()) folder.mkdir();

        File file = new File(Contracts.plugin.getDataFolder() + (newPath), name);
        if (!file.exists()) try {
            Files.copy(Contracts.plugin.getResource("default/" + (path.isEmpty() ? "" : path + "/") + name), file.getAbsoluteFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
