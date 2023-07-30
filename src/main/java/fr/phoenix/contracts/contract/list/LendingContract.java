package fr.phoenix.contracts.contract.list;

import com.google.gson.JsonObject;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.*;
import fr.phoenix.contracts.contract.parameter.Parameter;
import fr.phoenix.contracts.contract.parameter.ParameterType;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.UUID;

public class LendingContract extends Contract {
    private int interestRate;
    private boolean moneyGiven;
    private boolean moneyTaken;

    public LendingContract(UUID employer) {
        super(ContractType.LENDING, employer);
        loadParameters();
    }

    public LendingContract(ConfigurationSection section) {
        super(ContractType.LENDING, section);
        interestRate = section.getInt("interest-rate");
        moneyTaken = section.getBoolean("money-taken");
        moneyGiven = section.getBoolean("money-given");
        loadParameters();
    }

    public LendingContract(JsonObject object) {
        super(ContractType.LENDING, object);
        interestRate = object.get("interest-rate").getAsInt();
        moneyTaken = object.get("money-taken").getAsBoolean();
        moneyGiven = object.get("money-given").getAsBoolean();
        loadParameters();
    }


    @Override
    public void loadParameters() {
        addParameter(new Parameter(ParameterType.INTEREST_RATE, () -> Arrays.asList(interestRate + "%"), (player, s) -> {
            try {
                interestRate = Integer.parseInt(s);
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", s).send(player);
            }
        }, () -> interestRate <= 0));
    }

    @Override
    public boolean canDoSpecialAction(PlayerData playerData) {
        if (getContractParty(playerData.getUuid()) == ContractParties.EMPLOYEE)
            return getState() == ContractState.OPEN;
        if (getContractParty(playerData.getUuid()) == ContractParties.EMPLOYER)
            return moneyGiven && !moneyTaken;
        return false;
    }

    @Override
    public void onSpecialAction(InventoryClickEvent e) {
        PlayerData playerData = PlayerData.get((Player) e.getWhoClicked());
        if (e.getClick() == ClickType.LEFT) {
            if (getContractParty(playerData.getUuid()) == ContractParties.EMPLOYEE)
                employeeSpecialAction(playerData);
            if (getContractParty(playerData.getUuid()) == ContractParties.EMPLOYER)
                employerSpecialAction(playerData);

        }
    }

    public void employeeSpecialAction(PlayerData playerData) {
        double refundAmount = getAmount() * Math.pow((1 + interestRate / 100.), deadLine);

        if (Contracts.plugin.economy.getBalance(playerData.getPlayer()) < refundAmount) {
            Message.NOT_ENOUGH_MONEY.format("amount", Contracts.plugin.configManager.decimalFormat.format(refundAmount));
        } else {
            Contracts.plugin.economy.withdrawPlayer(playerData.getPlayer(), refundAmount);
            moneyGiven = true;
            changeContractState(ContractState.RESOLVED);
            Player employeePlayer = Bukkit.getPlayer(employee);
            Player employerPlayer = Bukkit.getPlayer(employer);
            if (employeePlayer != null)
                Message.EMPLOYEE_LENDING_CONTRACT_FULFILLED.format("contract-name", getName(), "guarantee", guarantee).send(employeePlayer);
            if (employerPlayer != null)
                Message.EMPLOYER_LENDING_CONTRACT_FULFILLED.format("contract-name", getName(), "amount", Contracts.plugin.configManager.decimalFormat.format(refundAmount), "employee", employeePlayer.getName()).send(employerPlayer);

        }
    }

    public void employerSpecialAction(PlayerData playerData) {
        double refundAmount = amount * Math.pow((1 + interestRate / 100.), deadLine);
        Contracts.plugin.economy.depositPlayer(playerData.getPlayer(), refundAmount);
        moneyTaken = true;
        Message.LENDING_RECEIPT.format("amount", refundAmount, "contract-name", name).send(playerData.getPlayer());
    }


    @Override
    public void whenProposalAccepted(Proposal proposal) {
        super.whenProposalAccepted(proposal);
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employee), amount);
        Player employeePlayer = Bukkit.getPlayer(employee);
        if (employeePlayer != null)
            Message.RECEIVED_LENDING_MONEY.format("amount", amount, "contract-name", name).send(employeePlayer);
    }

    @Override
    public void save(FileConfiguration config) {
        super.save(config);
        String str = contractId.toString();
        config.set(str + ".interest-rate", interestRate);
        config.set(str + ".money-given", moneyGiven);
        config.set(str + ".money-taken", moneyTaken);
    }

    public JsonObject getAsJsonObject() {
        JsonObject object = super.getAsJsonObject();
        object.addProperty("interest-rate", interestRate);
        object.addProperty("money-given", moneyGiven);
        object.addProperty("money-taken", moneyTaken);
        return object;
    }


    @Override
    public Placeholders getContractPlaceholder(GeneratedInventory inv) {
        PlayerData playerData = inv.getPlayerData();
        Placeholders holders = super.getContractPlaceholder(inv);
        holders.register("refund-amount", Contracts.plugin.configManager.decimalFormat.format(amount * Math.pow((1 + interestRate / 100.), deadLine)));
        holders.register("money-taken", moneyTaken);

        //Loads the type specific description of the contract.
        //This must absolutely be at the end as it will use all the other placeholders!
        holders.register("type-specific-description", holders.apply(playerData.getPlayer(), ContractsUtils.formatList(type.getSpecificDescription())));

        return holders;
    }
}
