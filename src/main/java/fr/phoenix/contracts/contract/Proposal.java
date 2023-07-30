package fr.phoenix.contracts.contract;

import com.google.gson.JsonObject;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.manager.data.sql.Jsonable;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class Proposal implements Jsonable {
    private final UUID proposalId;
    private final UUID contractId, employee, employer;
    private final long creationTime;
    private final double guarantee, paymentAmount;


    public Proposal(Contract contract, UUID employee, UUID employer, long creationTime, double guarantee, double paymentAmount) {
        proposalId = UUID.randomUUID();
        this.contractId = contract.getUUID();
        this.employee = employee;
        this.employer = employer;
        this.creationTime = creationTime;
        this.guarantee = guarantee;
        this.paymentAmount = paymentAmount;
    }

    public Proposal(ConfigurationSection section) {
        proposalId = UUID.fromString(section.getName());
        contractId = UUID.fromString(section.getString("contract-id"));
        employee = UUID.fromString(section.getString("employee"));
        employer = UUID.fromString(section.getString("employer"));
        creationTime = section.getLong("creation-time");
        guarantee = section.getDouble("guarantee");
        paymentAmount = section.getDouble("amount");
    }

    public Proposal(JsonObject object) {
        proposalId = UUID.fromString(object.get("uuid").getAsString());
        contractId = UUID.fromString(object.get("contract-id").getAsString());
        employee = UUID.fromString(object.get("employee").getAsString());
        employer = UUID.fromString(object.get("employer").getAsString());
        creationTime = object.get("creation-time").getAsLong();
        guarantee = object.get("guarantee").getAsDouble();
        paymentAmount = object.get("amount").getAsDouble();
    }

    @Override
    public JsonObject getAsJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", proposalId.toString());
        object.addProperty("contract-id", contractId.toString());
        object.addProperty("employee", employee.toString());
        object.addProperty("employer", employer.toString());
        object.addProperty("creation-time", creationTime);
        object.addProperty("guarantee", guarantee);
        object.addProperty("amount", paymentAmount);
        return object;
    }


    public Contract getContract() {
        return Contracts.plugin.dataProvider.getContractManager().get(contractId);
    }

    public UUID getEmployee() {
        return employee;
    }

    public UUID getEmployer() {
        return employer;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public double getGuarantee() {
        return guarantee;
    }

    public double getAmount() {
        return paymentAmount;
    }

    public void save(ConfigurationSection section) {
        section.set(proposalId + ".contract-id", contractId.toString());
        section.set(proposalId + ".employee", employee.toString());
        section.set(proposalId + ".employer", employer.toString());
        section.set(proposalId + ".creation-time", creationTime);
        section.set(proposalId + ".guarantee", guarantee);
        section.set(proposalId + ".amount", paymentAmount);
    }

}
