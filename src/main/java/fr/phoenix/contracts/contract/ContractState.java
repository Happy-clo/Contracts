package fr.phoenix.contracts.contract;

public enum ContractState {

    /**
     * Contract was created by the employer but
     * the employee hasn't accepted it yet.
     */
    AWAITING_EMPLOYEE,

    /**
     * Contract was created by the employer and the
     * employee is working on it
     */
    OPEN,


    /**
     * A middleman is reviewing his case
     */
    MIDDLEMAN_DISPUTED,

    /**
     * A middle man has given his decision but an appeal employer admins can be made.
     */
    MIDDLEMAN_RESOLVED,
    /**
     *
     */
    ADMIN_DISPUTED,

    /**
     *
     */
    RESOLVED;
}
