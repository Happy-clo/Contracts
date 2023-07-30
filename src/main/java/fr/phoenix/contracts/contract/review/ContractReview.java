package fr.phoenix.contracts.contract.review;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.manager.data.sql.api.MySQLTableEditor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class ContractReview {
    private final long reviewDate;
    private final UUID reviewUUID, contractId, reviewer, reviewed;
    private final List<String> comment;

    private int notation;

    public ContractReview(ConfigurationSection section) {
        reviewDate = section.getLong("date");
        reviewUUID = UUID.fromString(section.getName());
        contractId = UUID.fromString(section.getString("contract-id"));
        reviewer = UUID.fromString(section.getString("reviewer"));
        reviewed = UUID.fromString(section.getString("reviewed"));
        notation = section.getInt("notation");
        comment = section.getStringList("comment");
    }

    public ContractReview(JsonObject object) {
        reviewDate = object.get("date").getAsLong();
        reviewUUID = UUID.fromString(object.get("uuid").getAsString());
        contractId = UUID.fromString(object.get("contract-id").getAsString());
        reviewer = UUID.fromString(object.get("reviewer").getAsString());
        reviewed = UUID.fromString(object.get("reviewed").getAsString());
        notation = object.get("notation").getAsInt();
        comment = new ArrayList<>();
        object.get("comment").getAsJsonArray().forEach(jsonElement -> comment.add(jsonElement.getAsString()));
    }

    public ContractReview(UUID reviewed, UUID reviewer, Contract contract, int notation, List<String> comment) {
        reviewDate = System.currentTimeMillis();
        reviewUUID = UUID.randomUUID();
        this.reviewer = reviewer;
        this.reviewed = reviewed;
        this.contractId = contract.getUUID();
        this.notation = notation;
        this.comment = comment;
    }

    public void save(ConfigurationSection section) {
        section.set(reviewUUID + ".date", reviewDate);
        section.set(reviewUUID + ".contract-id", contractId.toString());
        section.set(reviewUUID + ".reviewer", reviewer.toString());
        section.set(reviewUUID + ".reviewed", reviewed.toString());
        section.set(reviewUUID + ".notation", notation);
        section.set(reviewUUID + ".comment", comment);
    }

    public JsonObject getAsJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("date", reviewDate);
        object.addProperty("uuid",reviewUUID.toString());
        object.addProperty("contract-id", contractId.toString());
        object.addProperty("reviewer", reviewer.toString());
        object.addProperty("reviewed", reviewed.toString());
        object.addProperty("notation", notation);
        JsonArray json = new JsonArray();
        for (String s : comment)
            json.add(s);
        object.add("comment", json);
        return object;
    }

    public void removeComment() {
        comment.clear();
    }

    public void setNotation(int notation) {
        this.notation = notation;
    }


    public void addComment(String line) {
        comment.add(line);
    }

    public long getReviewDate() {
        return reviewDate;
    }

    public UUID getUuid() {
        return reviewUUID;
    }

    public UUID getReviewer() {
        return reviewer;
    }

    public UUID getReviewed() {
        return reviewed;
    }

    public Contract getContract() {
        return Contracts.plugin.dataProvider.getContractManager().get(contractId);
    }

    public int getNotation() {
        return notation;
    }

    public List<String> getComment() {
        return comment;
    }
}
