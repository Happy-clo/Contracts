package fr.phoenix.contracts.contract;

import com.google.gson.JsonObject;
import fr.phoenix.contracts.contract.list.*;
import fr.phoenix.contracts.utils.ContractsUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public enum ContractType {
    GENERIC(GenericContract::new, GenericContract::new, GenericContract::new),
    HITMAN(HitmanContract::new, HitmanContract::new, HitmanContract::new),
    TRADE(TradeContract::new, TradeContract::new, TradeContract::new),
    LENDING(LendingContract::new, LendingContract::new, LendingContract::new),
    SALARY(SalaryContract::new, SalaryContract::new, SalaryContract::new);

    private final Function<UUID, Contract> initializer;
    private final Function<ConfigurationSection, Contract> yamlLoader;
    private final Function<JsonObject, Contract> SQLLoader;
    /**
     * This description will be given to add information specific to the type (in the GUI).
     * It is not parsed directly.
     **/
    private List<String> description;
    private String employeeName;
    private List<String> employeeLore;
    private String employerName;
    private List<String> employerLore;

    ContractType(Function<UUID, Contract> initializer, Function<ConfigurationSection, Contract> yamlLoader, Function<JsonObject, Contract> SQLLoader) {
        this.initializer = initializer;
        this.yamlLoader = yamlLoader;
        this.SQLLoader = SQLLoader;
    }

    public String getId() {
        return ContractsUtils.ymlName(toString());
    }

    public Contract instanciate(UUID uuid) {
        return initializer.apply(uuid);
    }

    public Contract loadFromSection(ConfigurationSection config) {
        return yamlLoader.apply(config);
    }

    public Contract loadFromJson(JsonObject object) {
        return SQLLoader.apply(object);
    }

    public void update(ConfigurationSection config) {
        List<String> description = config.getStringList("description");
        Validate.notNull(this.description = description, "Could not read contract type specific description.");
        if (config.contains("special-action.employee")) {
            ConfigurationSection section = config.getConfigurationSection("special-action.employee");
            employeeName = section.getString("name");
            employeeLore = section.getStringList("lore");
        }
        if (config.contains("special-action.employer")) {
            ConfigurationSection section = config.getConfigurationSection("special-action.employer");
            employerName = section.getString("name");
            employerLore = section.getStringList("lore");
        }

    }

    public List<String> getSpecificDescription() {
        return description;
    }

    public String getSpecialActionName(ContractParties party) {
        if (party == ContractParties.EMPLOYEE)
            return employeeName;
        return employerName;
    }

    public List<String> getSpecialActionLore(ContractParties party) {
        if (party == ContractParties.EMPLOYEE)
            return employeeLore;
        return employerLore;
    }
}
