package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class MiddlemenManager {

    /**
     * Makes all the possible matching between the middleman and the contracts.
     */
    public void assignContracts() {
        for (Contract contract : Contracts.plugin.dataProvider.getContractManager().getContracts())
            //If the contract requires a middleman but doesn't have one.
            if (contract.getState() == ContractState.MIDDLEMAN_DISPUTED && !contract.hasMiddleman())
                assignToRandomMiddleman(contract);
    }

    public void assignToRandomMiddleman(Contract contract) {
        List<PlayerData> assignableMiddlemen = getAssignableMiddleman(contract);
        //If there is a middleman to assign.
        if (assignableMiddlemen.size() != 0) {
            //Assigns the contract to a random middleman and updates the assignableMiddleman list if the middleman
            // is no longer assignable after having this contract
            int random = (int) (Math.random() * assignableMiddlemen.size());
            PlayerData middleman = assignableMiddlemen.get(random);
            middleman.assignMiddlemanContract(contract);
            if (middleman.getNumberOpendMiddlemanContracts() == Contracts.plugin.configManager.maxContractsPerMiddleman)
                assignableMiddlemen.remove(middleman);
            contract.setMiddleman(middleman.getUuid());
        }

    }

    public List<PlayerData> getAssignableMiddleman(Contract contract) {
        List<PlayerData> assignableMiddlemen = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = Contracts.plugin.dataProvider.getPlayerDataManager().get(player.getUniqueId());
            if (isAssignableToMiddleman(contract, playerData))
                assignableMiddlemen.add(playerData);
        }
        return assignableMiddlemen;
    }


    public boolean isAssignableToMiddleman(Contract contract, PlayerData middleman) {
        return middleman.getPlayer().hasPermission("contracts.middleman")
                && middleman.getNumberOpendMiddlemanContracts() < Contracts.plugin.configManager.maxContractsPerMiddleman
                && !contract.getEmployer().equals(middleman.getUuid())
                && !contract.getEmployee().equals(middleman.getUuid());
    }

}

