package fr.phoenix.contracts.api.event;

import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ContractStateChangeEvent extends Event {
    private final Contract contract;
    private final ContractState newState;
    private static final HandlerList handlers = new HandlerList();

    public ContractStateChangeEvent(Contract contract, ContractState newState) {
        this.contract = contract;
        this.newState = newState;
    }

    public ContractState getOldState() {
        return contract.getState();
    }

    public ContractState getNewState() {
        return newState;
    }

    public Contract getContract() {
        return contract;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
