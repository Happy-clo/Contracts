package fr.phoenix.contracts.manager.data;

import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.ContractType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The contract manager should be empty when there is no playerConnected and should be full otherwise.
 */
public abstract class ContractManager {
    protected final Map<UUID, Contract> contracts = new HashMap<>();


    public List<Contract> getContracts() {
        return contracts.values().stream().collect(Collectors.toList());
    }

    public List<Contract> getContractsOfType(ContractType type) {
        return contracts.values().stream().filter(contract -> type == contract.getType()).collect(Collectors.toList());
    }

    public List<Contract> getContractsOfState(ContractState state) {
        return contracts.values().stream().filter(contract -> contract.getState() == state).collect(Collectors.toList());
    }

    public Contract get(UUID contractId) {
        return contracts.get(contractId);
    }

    public abstract void load();

    public abstract void loadContract(UUID contractUUID);

    public abstract void saveContract(Contract contract);


    public void registerContract(Contract contract) {
        contracts.put(contract.getUUID(), contract);
    }

    public abstract void unregisterContract(Contract contract);

    public void clear() {
        contracts.clear();
    }
}
