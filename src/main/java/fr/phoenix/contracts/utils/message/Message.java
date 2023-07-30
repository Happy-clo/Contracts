package fr.phoenix.contracts.utils.message;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Message {
    RECEIVED_DEPT("&6{employee}&7 has reimbursed you &a{amount}&7!"),

    PAYED_DEBT("&7You reimbursed &a{amount} &7employer &6{employer}&7!"),

    MIDDLEMAN_ADDED("&7You successfully made &6{player}&7 a middleman."),

    MIDDLEMAN_REMOVED("&7You successfully removed the middleman role from &6{player}&7."),

    CANT_ACCEPT_OWN_CONTRACT("&cYou''re not allowed to accept a contract you have created!"),

    ALREADY_HAS_TOO_MUCH_CONTRACT("&7You can't create a new contract as you have reached the limit of open contracts as employer of &6{amount}&7."),

    CONTRACT_REFUSED("&7You refused the contract &6{contract-name}&7."),

    ASSIGNED_MIDDLEMAN_CONTRACT("&7A brand new &6middleman &7contract as been assigned to you!"),

    HAS_ALREADY_MADE_PROPOSAL("&7You already have a proposal for &6{contract-name}&7."),

    EMPLOYER_PROPOSAL_ACCEPTED("&7Congratulations, you have accepted the proposal of &6{employee-name}&7 and paid &f{amount}&7 for it."),

    EMPLOYEE_PROPOSAL_ACCEPTED("&7Congratulations! Your proposal was accepted for the contract &c{contract-name}&7."),

    PROPOSAL_RECEIVED("&7You just received a proposal from &6{other}&7 for the contract &c{contract-name}&7."),

    PROPOSAL_CREATED("&7You just created a proposal for &6{contract-name}&7 and paid &6{guarantee}&7 in guarantee."),

    CONTRACT_DISPUTED("&6{who}&7 called a &cdispute&7 for the contract &6{contract-name}&7."),

    ADMIN_DISPUTED("&6{who}&7 made an appeal and called an &cadmin &7dispute for the contract &6{contract-name}&7."),

    SET_PHYSICAL_ECONOMY("You successfully changed the economy system to a physical economy."),

    CLOSED_BY_ADMIN("&7The contract &6{contract-name}&7 has been closed by an &cadmin&7."),

    MIDDLEMAN_ADMIN_DISPUTED("&7The contract &6{contract-name}&7 with &6{employee}&7 is now under &cadmin dispute&7."),

    MIDDLEMAN_DECIDED("&7Your decision for &6{contract-name} &7has been taken in account."),

    EMPLOYEE_CONTRACT_ENDED("&6{employer}&7 ended the contract &e{contract-name}&7, you received &a{amount}&7 for it and your guarantee of &6{guarantee}&7 has been refunded."),

    EMPLOYER_CONTRACT_ENDED("&7You just ended the contract &e{contract-name}&7 and paid &6{employee} &a{amount}&7 for it."),

    EMPLOYEE_LENDING_CONTRACT_FULFILLED("&7You just fulfilled the contract &6{contract-name}&7 and your guarantee of &6{guarantee}&7 has been refunded."),

    EMPLOYER_LENDING_CONTRACT_FULFILLED("&7The contract &6{contract-name}&7 just got fulfilled by &6{employee}&7, who refunded you &6{amount}&7 for it."),

    RECEIVED_LENDING_MONEY("&7You received &6{amount}&7 from the lending contract &6{contract-name}&7."),

    CONTRACT_CREATION_NOTIFICATION("&7The {contact-type} contract &6{contract-name}&7 has just been created by &6{player-name}&7."),

    EMPLOYEE_CONTRACT_FULFILLED("&7You just fulfilled the contract &6{contract-name}&7, you received &a{amount}&7 for it and your guarantee of &6{guarantee}&7 has been refunded."),

    EMPLOYER_CONTRACT_FULFILLED("&7The contract &6{contract-name}&7 just got fulfilled by &6{employee}&7, you paid him &6{amount}&7 for it."),

    EMPLOYER_CONTRACT_RESOLVED("&7The contract &e{contract-name}&7 is now resolved, you paid &6{employee} &a{amount}&7 for it."),

    CONTRACT_MIDDLEMAN_RESOLVED("&7The middleman took his decision to solve the dispute for &c{contract-name}&7."),

    EMPLOYEE_CONTRACT_RESOLVED("&7The contract &c{contract-name}&7 is now resolved, you received &a{amount}&7 for it and got back &6{guarantee}&7 of guarantee."),

    MIDDLEMAN_RESOLVED("&7The contract &c{contract-name}&7 is now resolved, you received &6{commission}&7 from commissions for it."),

    EMPLOYER_OFFERED_ACCEPTED("&7The negotiation for &c{contract-name}&7 is ended and you paid &6{employee} &a{amount}&7 for it."),

    EMPLOYEE_OFFERED_ACCEPTED("&7The negotiation for the contract &c{contract-name}&7 is ended, you received &a{amount}&7 for it and got back &6{guarantee}&7 of guarantee."),

    OFFER_CREATED("&7Congratulations! You just sent an offer to &6{other}&7 for &c{contract-name}&7."),

    OFFER_RECEIVED("&7You just received an offer from &6{other}&7 for &c{contract-name}&7."),

    CONTRACT_FULFILLED("&7The contract &c{contract-name}&7 with &6{other}&7 is now fulfilled!"),

    ARE_YOU_SURE_TO_ACCEPT("&7Type '&6yes&7' to accept the contract &c{contract-name}&7."),

    NOT_ENOUGH_MONEY_ACCEPT_PROPOSAL("&7You can't accept this proposal because you don't have &a{amount}&7 on your balance to pay for it."),

    NOT_ENOUGH_MONEY("&7You don't have enough money to pay &6{amount}."),

    GUARANTEE_REFUND("&7Another proposal has been accepted for &6{contract-name}&7 your guarantee of &e{guarantee}&7 has been refunded."),

    CONTRACT_REMOVED_EMPLOYEE("&7The contract &c{contract-name}&7 has been removed by an admin, your guarantee of &e{guarantee}&7 has been refunded."),

    OPEN_CONTRACT_REMOVED_EMPLOYER("&7Your contract &c{contract-name}&7 has been removed by an admin, the payment amount of &e{amount}&7 has been refunded."),
    AWAITING_EMPLOYEE_CONTRACT_REMOVED_EMPLOYER("&7Your contract &c{contract-name}&7 has been removed by an admin."),

    NOT_ENOUGH_MONEY_PROPOSAL("&7You can't make a proposal because you can't pay &e{guarantee}&7 for the guarantee."),

    CREATED_CONTRACT("&7Congratulations! You created the contract &c{contract-name}&7 with a payment amount of &a{amount}&7."),

    SET_PARAMETER_ASK("&7Enter the value of &6{parameter-name}&7."),

    SEND_REVIEW("&7You successfully sent a review for &c{contract-name}&7."),

    RECEIVED_REVIEW("&7You received a &6{notation}&7 stars review for &c{contract-name}&7."),

    NOT_ANY_TRADE_ITEM("&cYou don't have any &6{item}&c in your inventory. "),

    NOT_ANY_SPACE_INVENTORY("&cYou don't have any space in your inventory."),

    TRADE_ITEM_DEPOSIT("&7You successfully deposited &a{amount} &7of &6{item}&7 for &c{contract-name}&7", "&7You still need to deposit &6{item-remaining}&7 items."),

    TRADE_ITEM_RECEIPT("&7You successfully claimed &a{amount} &7of &6{item}&7 for {contract-name}&7.", "&7You can still claim &6{item-remaining}&7 items."),

    PAID_SALARY("&7The salary for {contract-name} just got paid",
            "&7You now have &6{period-paid-in-advance}&7 salary periods in advance."),

    RECEIVED_SALARY("&7You just received your salary of &6{amount}&7 for {contract-name}."),

    PAID_FOR_ONE_PERIOD("&7You successfully paid &6{amount}&7 for one period salary for {contract-name}&7."),

    LENDING_RECEIPT("&7You successfully claimed &6{amount}&7 for {contract-name}&7."),

    ALREADY_ON_CHAT_INPUT("&cYou need to finish your previous chat prompt!"),

    NOT_VALID_INTEGER("&f{input} &cis not a valid integer."),

    NOT_VALID_DOUBLE("&f{input} &cis not a valid number."),

    NOT_POSITIVE_NUMBER("&cThe number must be positive."),

    NOT_STRICTLY_POSITIVE_NUMBER("&cThe number must be strictly positive."),

    NOT_VALID_PLAYER("&f{input} &cis not a valid player."),

    NOT_VALID_MATERIAL("&f{input} &cis not a valid material."),

    MISSING_CONTRACT_PARAMETER("&7You can't create the contract yet, some parameters are &cmissing&7."),

    SET_OFFER_ASK("&7Write the amount of money &e{employer}&7 will pay. It must be between &a{min}&7 and &a{max}"),

    RESOLVE_DISPUTE_ASK("&7Write the amount of money the employer will have to pay to the employee. It must be between &a{min}&7 and &a{max}"),

    NOT_IN_LIMIT("&a{amount}&7 is not between &a{min}&7 and &a{max}&7. Write another number."),

    SET_NOTATION_INFO("&e(Click Employer Change) Notation: {notation}"),

    SET_COMMENT_INFO("&e(Click Employer Change) Comment: {comment}"),

    SET_NOTATION_ASK("&7Write an integer between &e0 and 5&7 for the notation."),

    SET_COMMENT_ASK("&7Write the comment you want to leave."),

    CANCEL_CHAT_INPUT("&7Type '&ecancel&7' to cancel this input."),

    COMMENT_TOO_LONG("&7The comment you tried posting is too long, you must shorten it."),

    COMMENT_REQUIRED("&7You need to provide a comment to send the review!"),

    NOT_VALID_NOTATION("&c{input} &7is not a valid notation, it must be an integer."),

    NOT_ENOUGH_MONEY_FOR_COMMISSION("&cYou don't have enough money to pay for commissions."),

    PAID_COMMISSION("&7You paid &a{commission}&7 in commission for the middleman."),

    NOT_ENOUGH_PERMISSIONS("&cYou don't have sufficient permissions."),

    LOGIN_MESSAGE("&6{contracts}&7 new contracts have been created since your last login."),
    ;

    private List<String> message;
    private SoundReader sound;

    private Message(String... message) {
        this(null, message);
    }

    private Message(SoundReader sound, String... message) {
        this.message = Arrays.asList(message);
        this.sound = sound;
    }

    public String getPath() {
        return name().toLowerCase().replace("_", "-");
    }

    /**
     * Deep Copy !!
     *
     * @return Message updated based on what's in the config files
     */
    public List<String> getCached() {
        return new ArrayList<>(message);
    }

    public SoundReader getSound() {
        return sound;
    }

    public boolean hasSound() {
        return sound != null;
    }

    public PlayerMessage format(Object... placeholders) {
        return new PlayerMessage(this).format(placeholders);
    }

    public void update(ConfigurationSection config) {
        List<String> format = config.getStringList("format");
        Validate.notNull(this.message = format, "Could not read message format");
        sound = config.contains("sound") ? new SoundReader(config.getConfigurationSection("sound")) : null;
    }
}