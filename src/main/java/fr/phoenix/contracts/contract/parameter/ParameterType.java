package fr.phoenix.contracts.contract.parameter;

import fr.phoenix.contracts.utils.ContractsUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;

public enum ParameterType {
    NAME("name", "The name of your contract"),
    DESCRIPTION("description", "The description of your contract"),
    DEADLINE("deadline", "The number of days by which", "the contract needs to be fulfilled."),
    GUARANTEE("guarantee", "The amount of money the employee will have", "to pay you if doesn't fulfill the contract.."),
    PAYMENT_AMOUNT("payment amount", "The amount of money the contract."),
    PLAYER_TO_KILL("player to kill", "The player that needs to be killed."),
    TRADED_ITEM("item", "The traded item."),
    TRADED_ITEM_AMOUNT("item amount", "The amount of the desired item."),
    INTEREST_RATE("interest rate", "The daily interest rate for the lending contract."),
    SALARY_PERIOD("salary period", "The period in days by which the salary will be paid each time.");

    private String name;
    private List<String> description;


    ParameterType(String name, String... description) {
        this.name = name;
        this.description = Arrays.asList(description);
    }

    ParameterType(String name, List<String> description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getId() {
        return ContractsUtils.ymlName(toString());
    }

    public void update(ConfigurationSection config) {
        String name = config.getString("name");
        List<String> description = config.getStringList("description");
        Validate.notNull(this.description = description, "Could not read message format");
    }

}
