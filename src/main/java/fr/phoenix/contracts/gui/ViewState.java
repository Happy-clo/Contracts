package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.contract.ContractState;

/**
 * The state viewed (awaiting employee/ open/dispute/ended)
 */
public enum ViewState {
    AWAITING_EMPLOYEE(ContractState.AWAITING_EMPLOYEE),
    OPEN(ContractState.OPEN),
    DISPUTED(ContractState.ADMIN_DISPUTED, ContractState.MIDDLEMAN_DISPUTED,ContractState.MIDDLEMAN_RESOLVED),
    ENDED(ContractState.RESOLVED);


    public final ContractState[] corresponding;

    ViewState(ContractState... corresponding) {
        this.corresponding = corresponding;
    }
}