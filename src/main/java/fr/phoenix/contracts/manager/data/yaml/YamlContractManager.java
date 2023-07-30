package fr.phoenix.contracts.manager.data.yaml;

import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.manager.data.ContractManager;
import fr.phoenix.contracts.manager.data.DataProvider;
import fr.phoenix.contracts.utils.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class YamlContractManager extends ContractManager {
    private final DataProvider provider;

    public YamlContractManager(DataProvider provider) {
        this.provider = provider;
    }

    @Override
    public void load() {
        FileConfiguration config = new ConfigFile("contracts").getConfig();
        for (String key : config.getKeys(false)) {
            final ContractType type = ContractType.valueOf(config.getString(key + ".type"));
            Contract contract = type.loadFromSection(config.getConfigurationSection(key));
            contracts.put(UUID.fromString(key), contract);
        }
    }

    @Override
    public void loadContract(UUID contractUUID) {
        FileConfiguration config = new ConfigFile("contracts").getConfig();
        final ContractType type = ContractType.valueOf(config.getString(contractUUID + ".type"));
        Contract contract = type.loadFromSection(config.getConfigurationSection(contractUUID.toString()));
        contracts.put(contractUUID, contract);
    }

    @Override
    public void saveContract(Contract contract) {
        ConfigFile config = new ConfigFile("contracts");
        contract.save(config.getConfig());
        config.save();
    }

    @Override
    public void unregisterContract(Contract contract) {
        contracts.remove(contract.getUUID());
    }
}
