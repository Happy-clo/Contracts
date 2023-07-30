package fr.phoenix.contracts.player;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.ContractParties;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.contract.review.ContractReview;
import fr.phoenix.contracts.manager.data.sql.Jsonable;
import fr.phoenix.contracts.manager.data.sql.MySQLDataProvider;
import fr.phoenix.contracts.manager.data.sql.Synchronizable;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlayerData implements Synchronizable {
    private final UUID uuid;
    private final String playerName;
    @Nullable
    private Player player;
    private boolean onChatInput;

    /**
     * This should just remain UUID (=pointers) as the Contracts might change because
     * of synchronization between bungee cord server.
     */
    private final List<UUID> contracts = new ArrayList<>();


    /**
     * Used only for the middleman, not for the others
     */
    private final List<UUID> middlemanContracts = new ArrayList<>();


    /**
     * Maps the reviews to the player the contract the review was for.
     */
    private final Map<UUID, ContractReview> contractReviews = new LinkedHashMap<>();

    private int numberReviews;
    private double meanNotation;


    /**
     * Called when the data is not in SQL database.
     * This corresponds to a player that just joined for the first time the server.
     */
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
        playerName = Bukkit.getOfflinePlayer(uuid).getName();
    }

    public PlayerData(UUID uuid, ConfigurationSection section) {
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
        playerName = player != null ? player.getName() : section.getString("name");
        //We load the contracts
        for (String key : section.getStringList("contracts")) {
            Contract contract = Contracts.plugin.dataProvider.getContractManager().get(UUID.fromString(key));
            contracts.add(contract.getUUID());
        }


        //We load the middleman contracts
        for (String key : section.getStringList("middleman-contracts")) {
            Contract contract = Contracts.plugin.dataProvider.getContractManager().get(UUID.fromString(key));
            middlemanContracts.add(contract.getUUID());
        }

        if (section.contains("reviews"))
            //We load the contracts reviews
            for (String key : section.getConfigurationSection("reviews").getKeys(false)) {
                ContractReview review = new ContractReview(section.getConfigurationSection("reviews." + key));
                contractReviews.put(review.getContract().getUUID(), review);
                meanNotation += review.getNotation();
            }
        numberReviews = contractReviews.size();
        meanNotation /= numberReviews;

    }


    public PlayerData(UUID uuid, String playerName, List<Contract> contractList, List<Contract> middlemanContractsList, List<ContractReview> reviewsList) {
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
        this.playerName = Bukkit.getOfflinePlayer(uuid).getName() != null ?
                Bukkit.getOfflinePlayer(uuid).getName() : playerName;
        //We load the contracts
        contractList.forEach(contract -> contracts.add(contract.getUUID()));
        middlemanContractsList.forEach(contract -> middlemanContracts.add(contract.getUUID()));
        reviewsList.forEach(review -> {
            contractReviews.put(review.getContract().getUUID(), review);
            meanNotation += review.getNotation();
        });
        numberReviews = contractReviews.size();
        meanNotation /= numberReviews;
    }


    public String getPlayerName() {
        return playerName;
    }

    public boolean isOnChatInput() {
        return onChatInput;
    }

    public void setOnChatInput(boolean onChatInput) {
        this.onChatInput = onChatInput;
    }


    public void addContract(Contract contract) {
        contracts.add(contract.getUUID());
        synchronize();
    }

    public void removeContract(Contract contract) {
        contracts.remove(contract.getUUID());
        contractReviews.remove(contract.getUUID());
        synchronize();
    }

    public void addReview(ContractReview review) {
        int totalNotation = (int) meanNotation * numberReviews;
        contractReviews.put(review.getContract().getUUID(), review);
        numberReviews++;
        meanNotation = ((double) (totalNotation + review.getNotation())) / ((double) numberReviews);
        synchronize();
    }

    public int getNumberReviews() {
        return numberReviews;
    }

    public double getMeanNotation() {
        return meanNotation;
    }

    /**
     * When a middleman is assigned a contract.
     */
    public void assignMiddlemanContract(Contract contract) {
        Message.ASSIGNED_MIDDLEMAN_CONTRACT.format().send(player);
        middlemanContracts.add(contract.getUUID());
        synchronize();
    }

    public boolean hasReceivedReviewFor(Contract contract) {
        return contractReviews.containsKey(contract.getUUID());
    }

    /**
     * @return The number of middle man contracts that are not resolved.
     */
    public int getNumberOpendMiddlemanContracts() {
        return middlemanContracts.stream()
                .map(contractId -> Contracts.plugin.dataProvider.getContractManager().get(contractId))
                .filter(contract -> contract.getState() != ContractState.RESOLVED)
                .collect(Collectors.toList()).size();
    }

    public void clearMiddlemanContracts() {
        middlemanContracts.clear();
    }


    /**
     * Gets all the middleman contracts with the state matching the contractStates given in argument.
     */
    public List<Contract> getMiddlemanContracts(ContractState... states) {
        return middlemanContracts
                .stream()
                .map(contractId -> Contracts.plugin.dataProvider.getContractManager().get(contractId))
                .filter(contract -> Arrays.asList(states).contains(contract.getState()))
                .sorted((contract1, contract2) -> (int) (contract1.getLastStateChange() - contract2.getLastStateChange()))
                .collect(Collectors.toList());
    }


    public List<ContractReview> getReviews() {
        return contractReviews.values().stream().sorted((review1, review2) -> (int) (review1.getReviewDate() - review2.getReviewDate())).collect(Collectors.toList());
    }

    /**
     * Gets all the contracts with the state matching the contractStates given in argument.
     */
    public List<Contract> getContracts(ContractState... states) {
        return contracts
                .stream()
                .map(contractId -> Contracts.plugin.dataProvider.getContractManager().get(contractId))
                .filter(contract -> Arrays.asList(states).contains(contract.getState()))
                .sorted((contract1, contract2) -> (int) (contract1.getLastStateChange() - contract2.getLastStateChange()))
                .collect(Collectors.toList());
    }

    public List<Contract> getAllContracts() {
        return contracts.stream()
                .map(contractId -> Contracts.plugin.dataProvider.getContractManager().get(contractId))
                .sorted((contract1, contract2) -> (int) (contract1.getLastStateChange() - contract2.getLastStateChange()))
                .collect(Collectors.toList());
    }

    public List<Contract> getUncompletedContracts(PlayerData playerData, ContractParties... parties) {
        return contracts
                .stream()
                .map(contractId -> Contracts.plugin.dataProvider.getContractManager().get(contractId))
                .filter(contract -> contract.getState() != ContractState.RESOLVED
                        && Arrays.asList(parties).contains(contract.getContractParty(playerData.getUuid())))
                .sorted((contract1, contract2) -> (int) (contract1.getLastStateChange() - contract2.getLastStateChange()))
                .collect(Collectors.toList());
    }

    public static boolean has(UUID uuid) {
        return Contracts.plugin.dataProvider.getPlayerDataManager().has(uuid);
    }


    public static PlayerData get(Player player) {
        return Contracts.plugin.dataProvider.getPlayerDataManager().get(player.getUniqueId());
    }

    public static List<PlayerData> getAll() {
        return Contracts.plugin.dataProvider.getPlayerDataManager().getAll();
    }

    /**
     * Should be run when we don't know if the player is online or not.
     * This method enables to run a certain set of action on a playerData of an online or an offline player.
     * If the player is offline  it loads the data, runs the runnable without saving the data in the playerDataManager.
     * The boolean used in the consumer enables to know if getting the data involved some asynchronous process.
     */
    public static void loadAndRun(UUID uuid, BiConsumer<PlayerData, Boolean> playerDataConsumer) {
        if (!Contracts.plugin.dataProvider.getPlayerDataManager().has(uuid))
            Contracts.plugin.dataProvider.getPlayerDataManager().loadAndRun(uuid, (playerData) -> playerDataConsumer.accept(playerData, Contracts.plugin.dataProvider instanceof MySQLDataProvider));
        else {
            PlayerData playerData = Contracts.plugin.dataProvider.getPlayerDataManager().get(uuid);
            playerDataConsumer.accept(playerData, false);
        }
    }


    public void updatePlayer(Player player) {
        this.player = player;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void synchronize() {
        //Updates data to the SQL server.
        if (Contracts.plugin.dataProvider instanceof MySQLDataProvider) {
            //Notify the others servers that player data has been modified.
            //We wait 2s to make sure that the data has been correctly loaded into the SQL database.
            new BukkitRunnable() {
                @Override
                public void run() {
                    Contracts.plugin.pluginMessageManager.notifyPlayerDataUpdate(uuid);
                }
            }.runTaskLater(Contracts.plugin, 40L);
            Contracts.plugin.dataProvider.getPlayerDataManager().save(this);
        } else {
            Contracts.plugin.dataProvider.getPlayerDataManager().refreshLastTimeUsed(this);
        }
    }
}
