package fr.phoenix.contracts.manager.data.yaml;

import fr.phoenix.contracts.manager.data.ContractManager;
import fr.phoenix.contracts.manager.data.DataProvider;
import fr.phoenix.contracts.manager.data.PlayerDataManager;

public class YamlDataProvider implements DataProvider {
    private final YamlContractManager yamlContractManager = new YamlContractManager(this);
    private final YamlPlayerDataManager yamlPlayerDataManager = new YamlPlayerDataManager(this);


    @Override
    public ContractManager getContractManager() {
        return yamlContractManager;
    }

    @Override
    public PlayerDataManager getPlayerDataManager() {
        return yamlPlayerDataManager;
    }
}
