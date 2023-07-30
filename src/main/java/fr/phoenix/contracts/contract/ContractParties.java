package fr.phoenix.contracts.contract;

import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Function;

public enum ContractParties {
    EMPLOYEE((contract)->contract.getEmployee()),
    EMPLOYER((contract)->contract.getEmployer()),
    MIDDLEMAN((contract)->contract.getMiddleman());

    private final Function<Contract,UUID> UUIDProvider;

    ContractParties(Function<Contract, UUID> UUIDProvider) {
        this.UUIDProvider = UUIDProvider;
    }

    public UUID getUUID(Contract contract) {
        return UUIDProvider.apply(contract);
    }
    public OfflinePlayer getOfflinePlayer(Contract contract) {
        return Bukkit.getOfflinePlayer(UUIDProvider.apply(contract));
    }

}
