package fr.phoenix.contracts.contract;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.api.event.ContractStateChangeEvent;
import fr.phoenix.contracts.contract.parameter.Parameter;
import fr.phoenix.contracts.contract.parameter.ParameterType;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.manager.data.sql.Jsonable;
import fr.phoenix.contracts.manager.data.sql.MySQLDataProvider;
import fr.phoenix.contracts.manager.data.sql.Synchronizable;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ChatInput;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.DiscordMessage;
import fr.phoenix.contracts.utils.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Contract implements Jsonable, Synchronizable {
    protected final UUID contractId;
    protected final ContractType type;
    protected String name;
    // The employer creates the contract and the employee tries employer fulfill it
    protected final UUID employer;
    // Not final
    protected UUID employee;
    protected UUID middleman;
    protected ContractParties middlemanDisputeCaller;
    protected ContractParties adminDisputeCaller;
    /**
     * The amount of money the employer will have to give according to the middleman decision.
     */
    protected double middlemanAmount;
    protected double amount;
    /**
     * The amount of money you can lose/gain when a dispute is resolved.
     */
    protected double guarantee;

    protected ContractState state;
    protected List<String> description = new ArrayList<>();

    protected List<Proposal> proposals = new ArrayList<>();

    protected double lastOffer;
    protected ContractParties lastOfferProvider;
    /**
     * The deadline for the project in days.
     */
    protected int deadLine;
    private Map<ContractState, Long> stateEnteringTime = new HashMap<>();
    private long lastStateChange;
    /**
     * Maps the parameter to their ids
     */
    private final Map<String, Parameter> parameters = new LinkedHashMap<>();
    private boolean employeeSentReview;
    private boolean employerSentReview;


    public Contract(ContractType type, UUID employer) {
        loadDefaultParameters();
        contractId = UUID.randomUUID();
        this.type = type;
        this.employer = employer;
        state = ContractState.OPEN;
        stateEnteringTime.put(ContractState.AWAITING_EMPLOYEE, System.currentTimeMillis());
        lastStateChange = System.currentTimeMillis();
    }

    public Contract(ContractType type, ConfigurationSection section) {
        loadDefaultParameters();
        this.type = type;
        name = section.getString("name");
        contractId = UUID.fromString(section.getName());
        employee = section.getString("employee") == null ? null : UUID.fromString(section.getString("employee"));
        employer = UUID.fromString(section.getString("employer"));
        amount = section.getDouble("amount");
        guarantee = section.getDouble("guarantee");
        state = ContractState.valueOf(ContractsUtils.enumName(section.getString("contract-state")));
        deadLine = section.getInt("deadline");
        description = section.getStringList("description");
        if (section.contains("middleman"))
            middleman = UUID.fromString(section.getString("middleman"));
        if (section.contains("middleman-dispute-caller"))
            middlemanDisputeCaller = ContractParties.valueOf(section.getString("middleman-dispute-caller"));
        if (section.contains("admin-dispute-caller"))
            adminDisputeCaller = ContractParties.valueOf(section.getString("admin-dispute-caller"));
        if (section.contains("entering-time")) {
            for (String key : section.getConfigurationSection("entering-time").getKeys(false)) {
                stateEnteringTime.put(ContractState.valueOf(ContractsUtils.enumName(key)), section.getLong("entering-time." + key));
            }
        }
        if (section.contains("proposals"))
            for (String key : section.getConfigurationSection("proposals").getKeys(false))
                addProposal(new Proposal(section.getConfigurationSection("proposals." + key)));

        lastOffer = section.getDouble("last-offer");
        if (section.contains("last-offer-provider"))
            lastOfferProvider = ContractParties.valueOf(section.getString("last-offer-provider"));
        middlemanAmount = section.getDouble("middleman-amount");
        employerSentReview = section.getBoolean("employer-sent-review");
        employeeSentReview = section.getBoolean("employee-sent-review");
    }

    public Contract(ContractType type, JsonObject object) {
        loadDefaultParameters();
        this.type = type;
        name = object.get("name").getAsString();
        contractId = UUID.fromString(object.get("uuid").getAsString());
        employee = object.has("employee") ? UUID.fromString(object.get("employee").getAsString()) : null;
        employer = UUID.fromString(object.get("employer").getAsString());
        amount = object.get("amount").getAsDouble();
        guarantee = object.get("guarantee").getAsDouble();
        state = ContractState.valueOf(ContractsUtils.enumName(object.get("contract-state").getAsString()));
        deadLine = object.get("deadline").getAsInt();
        description = new ArrayList<>();
        JsonArray array = object.get("description").getAsJsonArray();
        array.forEach(jsonElement -> description.add(jsonElement.toString()));

        if (object.has("middleman"))
            middleman = UUID.fromString(object.get("middleman").getAsString());
        if (object.has("middleman-dispute-caller"))
            middlemanDisputeCaller = ContractParties.valueOf(object.get("middleman-dispute-caller").getAsString());
        if (object.has("admin-dispute-caller"))
            adminDisputeCaller = ContractParties.valueOf(object.get("admin-dispute-caller").getAsString());

        if (object.has("state-entering-time")) {
            JsonObject json = object.get("state-entering-time").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet())
                stateEnteringTime.put(ContractState.valueOf(ContractsUtils.enumName(entry.getKey())), json.get(entry.getKey()).getAsLong());
        }
        if (object.has("proposals")) {
            array = object.get("proposals").getAsJsonArray();
            array.forEach(jsonElement -> proposals.add(new Proposal(jsonElement.getAsJsonObject())));
        }

        lastOffer = object.get("last-offer").getAsDouble();
        if (object.has("last-offer-provider"))
            lastOfferProvider = ContractParties.valueOf(object.get("last-offer-provider").getAsString());
        middlemanAmount = object.get("middleman-amount").getAsDouble();
        employeeSentReview = object.get("employee-sent-review").getAsBoolean();
        employerSentReview = object.get("employer-sent-review").getAsBoolean();
    }

    /**
     * Load all the parameters specific to the contract type.
     */
    public abstract void loadParameters();


    /**
     * The default parameters that are the same for all the contracts.
     */
    public void loadDefaultParameters() {
        addParameter(new Parameter(ParameterType.NAME,
                () -> Arrays.asList(name), (p, str) -> {
            name = str;
        }, () -> name == null));

        addParameter(new Parameter(ParameterType.DESCRIPTION,
                () -> description, (p, str) -> {
            description.add(str);
        }, () -> false));

        addParameter(new Parameter(ParameterType.DEADLINE, () -> Arrays.asList("" + deadLine), (p, str) -> {
            try {
                deadLine = Integer.parseInt(str);
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", str).send(p);
            }
        }, () -> deadLine <= 0));

        addParameter(new Parameter(ParameterType.GUARANTEE, () -> Arrays.asList(Contracts.plugin.configManager.decimalFormat.format(guarantee)), (p, str) -> {
            try {
                guarantee = Double.parseDouble(str);
            } catch (Exception e) {
                Message.NOT_VALID_DOUBLE.format("input", str).send(p);
            }
        }, () -> guarantee < 0));

        addParameter(new Parameter(ParameterType.PAYMENT_AMOUNT, () -> Arrays.asList(Contracts.plugin.configManager.decimalFormat.format(amount)), (p, str) -> {
            try {
                amount = Double.parseDouble(str);
            } catch (Exception e) {
                Message.NOT_VALID_DOUBLE.format("input", str).send(p);
            }
        }, () -> amount <= 0));
    }


    public void addProposal(Proposal proposal) {
        proposals.add(proposal);
        synchronize();
    }


    public List<Proposal> getProposals() {
        //Deep Copy
        return new ArrayList<>(proposals);
    }

    public boolean canLeaveReview(PlayerData playerData) {
        boolean sentReview = playerData.getUuid() == employee ? employeeSentReview : employerSentReview;
        return (hasBeenIn(ContractState.RESOLVED) && !sentReview && (System.currentTimeMillis() - getEnteringTime(ContractState.RESOLVED)) < Contracts.plugin.configManager.reviewPeriod * 1000 * 3600 * 24);
    }

    /**
     * This method is very important, it is used employer have an
     * ordered list representing the parameters for the gui.
     */
    protected void addParameter(Parameter parameter) {
        parameters.put(parameter.getId(), parameter);
    }

    public double getLastOffer() {
        return lastOffer;
    }

    public void hasSentReview(PlayerData playerData) {
        if (playerData.getUuid() == employee)
            employeeSentReview = true;
        else employerSentReview = true;
    }

    @Nullable
    /**
     * @return returns null if there hasn't been any offers yet.
     */
    public ContractParties getLastOfferProvider() {
        return lastOfferProvider;
    }

    public ContractParties getContractParty(UUID uuid) {
        if (isEmployee(uuid))
            return ContractParties.EMPLOYEE;
        if (isEmployer(uuid))
            return ContractParties.EMPLOYER;
        if (isMiddleman(uuid))
            return ContractParties.MIDDLEMAN;
        return null;
    }


    public void openChatInput(String str, PlayerData playerData, GeneratedInventory inv) {
        //If the player is already on chat input we block the access employer a new chat input.
        if (playerData.isOnChatInput()) {
            Message.ALREADY_ON_CHAT_INPUT.format().send(playerData.getPlayer());
            return;
        }
        Message.SET_PARAMETER_ASK.format("parameter-name", ContractsUtils.chatName(str)).send(playerData.getPlayer());
        new ChatInput(playerData, inv, (p, val) -> {
            parameters.get(str).set(p.getPlayer(), val);
            return true;
        });
    }


    /**
     * Used employer verify the contract has all is parameters setup.
     */
    public boolean allParameterFilled() {
        for (Parameter param : parameters.values())
            if (param.needsToBeFilled())
                return false;
        return true;
    }

    /**
     * Used employer fully create the initialized contract and put it in the contract market.
     */
    public void createContract() {
        state = ContractState.AWAITING_EMPLOYEE;
        Message.CREATED_CONTRACT.format("contract-name", name, "amount", "" + amount).send(Bukkit.getPlayer(employer));
        if (Contracts.plugin.configManager.sendDiscordMessages)
            new DiscordMessage("contract-created", "contract-name", name, "player-name", Bukkit.getPlayer(employer).getName(), "contract-amount", "" + amount, "contract-type", ContractsUtils.chatName(type.name())).send();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("contracts.notify"))
                Message.CONTRACT_CREATION_NOTIFICATION.format("contract-name", name,
                        "contract-type", ContractsUtils.chatName(type.toString()), "player-name", Bukkit.getPlayer(employer).getName()).send(player);
        }
        Contracts.plugin.dataProvider.getContractManager().registerContract(this);
        PlayerData.get(Bukkit.getPlayer(employer)).addContract(this);
        synchronize();
    }

    public ContractType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getMiddlemanAmount() {
        return middlemanAmount;
    }

    /**
     * @return The list of all the parameters in the order of insertion.
     */
    public List<Parameter> getParametersList() {
        List<Parameter> result = new ArrayList<>();
        parameters.keySet().forEach(str -> result.add(parameters.get(str)));
        return result;
    }

    public void setEmployee(UUID employee) {
        this.employee = employee;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


    public double getGuarantee() {
        return guarantee;
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    public void setGuarantee(double guarantee) {
        this.guarantee = guarantee;
    }

    public UUID getUUID() {
        return contractId;
    }

    public ContractState getState() {
        return state;
    }

    /**
     * If the entering time is set then the contract has been in the state.
     */
    public boolean hasBeenIn(ContractState state) {
        return stateEnteringTime.containsKey(state);
    }

    public long getEnteringTime(ContractState state) {
        return stateEnteringTime.get(state);
    }

    public long getLastStateChange() {
        return lastStateChange;
    }

    public String getName() {
        return name;
    }

    public UUID getEmployer() {
        return employer;
    }


    /**
     * Gets and load the data of the other person with you in the contract.
     *
     * @return
     */
    @Nullable
    public UUID getOther(PlayerData playerData) {
        return playerData.getUuid().equals(getEmployee()) ? getEmployer() : getEmployee();
    }

    public UUID getEmployee() {
        return employee;
    }

    public double getAmount() {
        return amount;
    }

    public List<String> getDescription() {
        return description;
    }

    public int getDeadLine() {
        return deadLine;
    }

    @Nullable
    public ContractParties getMiddlemanDisputeCaller() {
        return middlemanDisputeCaller;
    }

    @Nullable
    public ContractParties getAdminDisputeCaller() {
        return adminDisputeCaller;
    }

    public boolean isEmployee(UUID uuid) {
        return uuid.equals(employee);
    }

    public boolean isMiddleman(UUID uuid) {
        return uuid.equals(middleman);
    }

    public boolean isEmployer(UUID uuid) {
        return uuid.equals(employer);
    }

    /**
     * Called when the employer accepts the proposal of the employee.
     */
    public void whenProposalAccepted(Proposal proposal) {
        //Give back the guarantee to all the other players.
        getProposals()
                .stream()
                .filter(proposal1 -> proposal1 != proposal)
                .forEach(proposal1 -> {
                    Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(proposal1.getEmployee()), proposal1.getGuarantee());
                    Message.GUARANTEE_REFUND.format("guarantee", proposal1.getGuarantee(), "contract-name", name).send(proposal1.getEmployee());
                });

        getProposals().clear();
        Validate.isTrue(state == ContractState.AWAITING_EMPLOYEE, "Contract is not awaiting employee.");

        //We remove the amount of the contract to the employer
        Contracts.plugin.economy.withdrawPlayer(Bukkit.getOfflinePlayer(employer), proposal.getAmount() * (1 + Contracts.plugin.configManager.contractTaxes / 100.));

        employee = proposal.getEmployee();
        amount = proposal.getAmount();
        guarantee = proposal.getGuarantee();
        changeContractState(ContractState.OPEN);
        PlayerData.loadAndRun(employee,
                (playerData, isAsync) -> playerData.addContract(this));
        synchronize();
        Message.EMPLOYEE_PROPOSAL_ACCEPTED.format("contract-name", name).send(employee);
        PlayerData.loadAndRun(employee, (employeeData, isAsync) -> Message.EMPLOYER_PROPOSAL_ACCEPTED.format("contract-name", name, "employee-name", employeeData.getPlayerName(),
                "amount", Contracts.plugin.configManager.decimalFormat.format(proposal.getAmount() * (1 + Contracts.plugin.configManager.contractTaxes / 100.))).send(employer));

    }


    /**
     * Calls a middle man because there is a dispute with the contract.
     */
    public void callDispute(PlayerData playerData) {
        middlemanDisputeCaller = getContractParty(playerData.getUuid());
        changeContractState(ContractState.MIDDLEMAN_DISPUTED);
        UUID disputeCaller = middlemanDisputeCaller == ContractParties.EMPLOYEE ? employee : employer;
        synchronize();
        PlayerData.loadAndRun(employer, (employerData, isAsync) -> Message.CONTRACT_DISPUTED.format("contract-name", name, "who", employee.equals(middlemanDisputeCaller.getUUID(this)) ? "You" : employerData.getPlayerName())
                .send(employee));

        PlayerData.loadAndRun(employee, (employeeData, isAsync) ->
                Message.CONTRACT_DISPUTED.format("contract-name", name, "who", employer.equals(middlemanDisputeCaller.getUUID(this)) ? "You" : employeeData.getPlayerName())
                        .send(employer));
        Message.PAID_COMMISSION.format("commission", amount * Contracts.plugin.configManager.middlemanCommission / 100).send(disputeCaller);
        Contracts.plugin.middlemenManager.assignToRandomMiddleman(this);
    }

    /**
     * Calls a middle man because there is a dispute with the contract.
     */
    public void callAdminDispute(PlayerData playerData) {
        adminDisputeCaller = getContractParty(playerData.getUuid());
        changeContractState(ContractState.ADMIN_DISPUTED);
        synchronize();
        PlayerData.loadAndRun(adminDisputeCaller.getUUID(this), (adminDisputeData, isAsync) -> {
            Message.ADMIN_DISPUTED.format("contract-name", name, "who", adminDisputeCaller == ContractParties.EMPLOYEE ? "You" : adminDisputeData.getPlayerName())
                    .send(employee);
            Message.ADMIN_DISPUTED.format("contract-name", name, "who", adminDisputeCaller == ContractParties.EMPLOYER ? "You" : adminDisputeData.getPlayerName())
                    .send(employer);
            Message.ADMIN_DISPUTED.format("contract-name", name, "who", adminDisputeData.getPlayerName())
                    .send(middleman);
        });
    }


    /**
     * When the contract has been fulfilled.
     */
    public void whenContractFulfilled() {
        changeContractState(ContractState.RESOLVED);
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employee), guarantee + amount);
        synchronize();
        Message.EMPLOYEE_CONTRACT_FULFILLED.format("contract-name", getName(), "amount", amount, "guarantee", guarantee).send(employee);
        PlayerData.loadAndRun(employee, (employeeData, isAsync) -> Message.EMPLOYER_CONTRACT_FULFILLED.format("contract-name", getName(), "amount", amount, "employee", employeeData.getPlayerName()).send(employer)
        );
    }

    /**
     * When the employer judges that the job is done and pays the employee for it.
     */
    public void whenContractEnded() {
        changeContractState(ContractState.RESOLVED);
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employee), guarantee + amount);
        synchronize();
        PlayerData.loadAndRun(employee, (employeeData, isAsync) -> {
            Message.EMPLOYEE_CONTRACT_ENDED.format("employer",
                    employeeData.getPlayerName(), "contract-name", getName(), "amount", amount, "guarantee", guarantee).send(employee);
            Message.EMPLOYER_CONTRACT_ENDED.format("employee", employeeData.getPlayerName(), "contract-name", getName(), "amount", amount).send(employer);
        });
    }


    public void whenClosedByAdmin(double adminAmount) {
        OfflinePlayer employerPlayer = Bukkit.getOfflinePlayer(employer);
        OfflinePlayer employeePlayer = Bukkit.getOfflinePlayer(employee);
        OfflinePlayer middlemanPlayer = Bukkit.getOfflinePlayer(middleman);
        changeContractState(ContractState.RESOLVED);
        double commissionRate = Contracts.plugin.configManager.middlemanCommission / 100;
        double commission = (amount) * commissionRate;
        Contracts.plugin.economy.depositPlayer(middlemanPlayer, commission);
        Contracts.plugin.economy.depositPlayer(employeePlayer, adminAmount + guarantee);
        Contracts.plugin.economy.depositPlayer(employerPlayer, -adminAmount + amount);
        changeContractState(ContractState.RESOLVED);
        synchronize();
        Message.CLOSED_BY_ADMIN.format("contract-name", getName()).send(employee);
        Message.CLOSED_BY_ADMIN.format("contract-name", getName()).send(employer);
        Message.CLOSED_BY_ADMIN.format("contract-name", getName()).send(middlemanPlayer.getPlayer());

    }

    public void whenResolvedFromDispute() {
        OfflinePlayer employerPlayer = Bukkit.getOfflinePlayer(employer);
        OfflinePlayer employeePlayer = Bukkit.getOfflinePlayer(employee);
        OfflinePlayer middlemanPlayer = Bukkit.getOfflinePlayer(middleman);
        changeContractState(ContractState.RESOLVED);
        double commissionRate = Contracts.plugin.configManager.middlemanCommission / 100;
        double commission = (amount) * commissionRate;
        Contracts.plugin.economy.depositPlayer(middlemanPlayer, commission);
        Contracts.plugin.economy.depositPlayer(employeePlayer, middlemanAmount + guarantee);
        Contracts.plugin.economy.depositPlayer(employerPlayer, -middlemanAmount + amount);
        synchronize();
        PlayerData.loadAndRun(employer, (employerData, isAsync) -> Message.CONTRACT_MIDDLEMAN_RESOLVED.format("employer", employerData.getPlayerName(), "contract-name", getName(), "amount", middlemanAmount, "guarantee", guarantee).send(employee));
        PlayerData.loadAndRun(employee, (employeeData, isAsync) -> {
            Message.CONTRACT_MIDDLEMAN_RESOLVED.format("employee", employeeData.getPlayerName(), "contract-name", getName(), "amount", middlemanAmount).send(employer);
            Message.CONTRACT_MIDDLEMAN_RESOLVED.format("employee", employeeData.getPlayerName(), "contract-name", getName(), "amount", middlemanAmount).send(employer);

        });

        Message.MIDDLEMAN_RESOLVED.format("contract-name", getName(), "commission", commission).send(middleman);
    }


    public void whenDecidedByMiddleman(double amount) {
        changeContractState(ContractState.MIDDLEMAN_RESOLVED);
        middlemanAmount = amount;
        synchronize();
        Message.CONTRACT_MIDDLEMAN_RESOLVED.format("contract-name", getName()).send(employee);
        Message.CONTRACT_MIDDLEMAN_RESOLVED.format("contract-name", getName()).send(employer);
        Message.MIDDLEMAN_DECIDED.format("contract-name", getName()).send(middleman);


    }


    public void whenOfferCreated(PlayerData playerData, double value) {
        lastOfferProvider = getContractParty(playerData.getUuid());
        lastOffer = value;
        UUID other = getOther(playerData);
        synchronize();
        PlayerData.loadAndRun(other, ((otherData, isAsync) -> Message.OFFER_CREATED.format("other", otherData.getPlayerName(), "contract-name", getName()).send(playerData.getPlayer())));
        Message.OFFER_RECEIVED.format("other", playerData.getPlayerName(), "contract-name", getName()).send(other);
    }

    public void whenOfferAccepted() {
        changeContractState(ContractState.RESOLVED);
        OfflinePlayer employeePlayer = Bukkit.getOfflinePlayer(employee);
        OfflinePlayer employerPlayer = Bukkit.getOfflinePlayer(employer);
        //Deposit the right amount of money to the 2 parts.
        Contracts.plugin.economy.depositPlayer(employeePlayer, lastOffer + guarantee);
        Contracts.plugin.economy.depositPlayer(employerPlayer, amount - lastOffer);
        synchronize();
        PlayerData.loadAndRun(employee, (employeeData, isAsync) -> Message.EMPLOYER_OFFERED_ACCEPTED.format("employee", employeeData.getPlayerName(), "contract-name", getName(), "amount", lastOffer).send(employer));
        PlayerData.loadAndRun(employer, (employerData, isAsync) -> Message.EMPLOYEE_OFFERED_ACCEPTED.format("employer", employerData.getPlayerName(), "contract-name", getName(), "amount", lastOffer, "guarantee", guarantee).send(employee));
        ;


    }


    /**
     * When the employee wants to cancel the contract he has to pay the entire guarantee.
     */
    public void whenCancelledByEmployee() {
        //If cancelled by the employee
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employer), guarantee);
        changeContractState(ContractState.RESOLVED);

    }


    /**
     * Requires employee employer be online
     */
    public void completeContract(Player employee) {
        Validate.isTrue(state == ContractState.OPEN, "Contract is not open");
        changeContractState(ContractState.RESOLVED);

        Player employer = Bukkit.getPlayer(this.employer);
        if (employer != null)
            Message.CONTRACT_FULFILLED.format("contract-name", name, "other", employee.getName()).send(employer);
        Message.CONTRACT_FULFILLED.format("contract-name", name, "other", employer.getName()).send(employee);

        // Transactions
        Contracts.plugin.economy.withdrawPlayer(Bukkit.getOfflinePlayer(this.employer), amount);
        Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(this.employee), amount);
    }

    public void changeContractState(ContractState newState) {
        Bukkit.getPluginManager().callEvent(new ContractStateChangeEvent(this, newState));
        state = newState;
        lastStateChange = System.currentTimeMillis();
        stateEnteringTime.put(newState, System.currentTimeMillis());
    }

    public boolean hasAlreadyProposed(PlayerData proposingPlayer) {
        return proposals.stream().map(proposal -> proposal.getEmployee()).collect(Collectors.toList()).contains(proposingPlayer.getUuid());
    }

    public void setMiddleman(UUID middleman) {
        this.middleman = middleman;
        synchronize();
    }

    public boolean hasMiddleman() {
        return middleman != null;
    }

    public UUID getMiddleman() {
        return middleman;
    }

    public void makeProposal(PlayerData proposingPlayer, double amount, double guarantee) {
        //If the player already made a proposal
        if (hasAlreadyProposed(proposingPlayer)) {
            Message.HAS_ALREADY_MADE_PROPOSAL.format("contract-name", getName()).send(proposingPlayer.getPlayer());
            return;
        }
        proposals.add(new Proposal(this, proposingPlayer.getUuid(), employer, System.currentTimeMillis(), guarantee, amount));
        Contracts.plugin.economy.withdrawPlayer(proposingPlayer.getPlayer(), guarantee);
        synchronize();
        Message.PROPOSAL_RECEIVED.format("other", proposingPlayer.getPlayerName(), "contract-name", getName()).send(employer);
        Message.PROPOSAL_CREATED.format("contract-name", getName(), "guarantee", guarantee).send(proposingPlayer.getPlayer());
    }


    public void synchronize() {
        if (Contracts.plugin.dataProvider instanceof MySQLDataProvider) {
            Contracts.plugin.dataProvider.getContractManager().saveContract(this);
            //Notify the others servers that contract data has been modified.
            //We wait 2s to make sure that the data has been correctly loaded into the SQL database.
            new BukkitRunnable() {
                @Override
                public void run() {
                    Contracts.plugin.pluginMessageManager.notifyContractUpdate(contractId);
                }
            }.runTaskLater(Contracts.plugin, 30L);
        }
    }

    public void save(FileConfiguration config) {
        String uuid = contractId.toString();
        config.set(uuid + ".name", name);
        config.set(uuid + ".type", type.toString());
        //The employee Ve be null at first
        if (employee != null)
            config.set(uuid + ".employee", employee.toString());
        config.set(uuid + ".employer", employer.toString());
        if (middleman != null)
            config.set(uuid + ".middleman", middleman.toString());
        if (middlemanDisputeCaller != null)
            config.set(uuid + ".middleman-dispute-caller", middlemanDisputeCaller.toString());
        if (adminDisputeCaller != null) {
            config.set(uuid + ".admin-dispute-caller", adminDisputeCaller.toString());
        }
        config.set(uuid + ".amount", amount);
        config.set(uuid + ".guarantee", guarantee);
        config.set(uuid + ".contract-state", ContractsUtils.ymlName(state.toString()));
        config.set(uuid + ".deadline", deadLine);
        config.set(uuid + ".description", description);
        for (ContractState state : stateEnteringTime.keySet()) {
            config.set(uuid + ".entering-time." + ContractsUtils.ymlName(state.toString()), stateEnteringTime.get(state));
        }
        ConfigurationSection section = config.createSection(uuid + ".proposals");
        for (Proposal proposal : getProposals()) {
            proposal.save(section);
        }
        config.set(uuid + ".last-offer", lastOffer);
        config.set(uuid + ".employee-sent-review", employeeSentReview);
        config.set(uuid + ".employer-sent-review", employerSentReview);
        if (lastOfferProvider != null)
            config.set(uuid + ".last-offer-provider", lastOfferProvider.toString());
        config.set(uuid + ".middleman-amount", middlemanAmount);
    }


    @Override
    public JsonObject getAsJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("uuid", contractId.toString());
        object.addProperty("name", name);
        object.addProperty("employer", employer.toString());
        if (employee != null)
            object.addProperty("employee", employee.toString());
        object.addProperty("amount", amount);
        object.addProperty("guarantee", guarantee);
        object.addProperty("contract-type", type.toString());
        object.addProperty("contract-state", state.toString());
        object.addProperty("deadline", deadLine);
        JsonArray jsonArray = new JsonArray();
        for (String str : description)
            jsonArray.add(str);
        object.add("description", jsonArray);
        if (middleman != null)
            object.addProperty("middleman", middleman.toString());
        if (middlemanDisputeCaller != null)
            object.addProperty("middleman-dispute-caller", middlemanDisputeCaller.toString());
        if (adminDisputeCaller != null)
            object.addProperty("admin-dispute-caller", adminDisputeCaller.toString());

        JsonObject json = new JsonObject();
        for (ContractState state : stateEnteringTime.keySet())
            json.addProperty(state.toString(), stateEnteringTime.get(state));
        object.add("state-entering-time", json);

        jsonArray = new JsonArray();
        for (Proposal proposal : proposals)
            jsonArray.add(proposal.getAsJsonObject());
        object.add("proposals", jsonArray);

        object.addProperty("last-offer", lastOffer);
        if (lastOfferProvider != null)
            object.addProperty("last-offer-provider", lastOfferProvider.toString());
        object.addProperty("middleman-amount", middlemanAmount);
        object.addProperty("employee-sent-review", employeeSentReview);
        object.addProperty("employer-sent-review", employerSentReview);
        return object;
    }

    public Placeholders getContractPlaceholder(GeneratedInventory inv) {
        PlayerData playerData = inv.getPlayerData();
        Placeholders holders = new Placeholders();

        //Add placeholders for all the parameters.
        for (String id : parameters.keySet())
            holders.register(id, ContractsUtils.formatList(parameters.get(id).get()));

        holders.register("state", getState().toString());
        holders.register("type", ContractsUtils.chatName(getType().toString()));
        if (hasBeenIn(ContractState.MIDDLEMAN_DISPUTED)) {
            if (middleman == null)
                holders.register("middleman", "");
            else
                PlayerData.loadAndRun(middleman, (middlemanData, isAsync) -> {
                    holders.register("middleman", middlemanData.getPlayerName());
                    if (isAsync)
                        inv.open();
                });
            PlayerData.loadAndRun(middlemanDisputeCaller.getUUID(this), (middlemanDisputeCallerData, isAsync) -> {
                holders.register("middleman-dispute-caller", middlemanDisputeCallerData.getPlayerName());
                if (isAsync)
                    inv.open();
            });
        }
        if (hasBeenIn(ContractState.ADMIN_DISPUTED))
            PlayerData.loadAndRun(adminDisputeCaller.getUUID(this), (adminDisputeCallerData, isAsync) -> {
                holders.register("admin-dispute-caller", adminDisputeCallerData.getPlayerName());
                if (isAsync)
                    inv.open();
            });

        if (state == ContractState.OPEN) {
            boolean receivedOffer = lastOfferProvider != null && getContractParty(playerData.getUuid()) != lastOfferProvider;
            holders.register("received-offer", ContractsUtils.formatBoolean(receivedOffer));
        }
        if (state == ContractState.AWAITING_EMPLOYEE) {
            holders.register("proposals", getProposals().size());
        }


        //Load asynchronously if the employerData if not in RAM and the storage is SQL.
        PlayerData.loadAndRun(employer, (employerData, isAsync) -> {
            holders.register("employer", employerData.getPlayerName());
            holders.register("employer-reputation", ContractsUtils.formatNotation(employerData.getMeanNotation()));
            holders.register("employer-total-reviews", employerData.getNumberReviews());
            if (isAsync)
                inv.open();
        });

        //Load asynchronously if the employerData if not in RAM and the storage is SQL.
        if (getEmployee() != null)
            PlayerData.loadAndRun(employee, (employeeData, isAsync) -> {
                holders.register("employee", employeeData.getPlayerName());
                holders.register("employee-reputation", ContractsUtils.formatNotation(employeeData.getMeanNotation()));
                holders.register("employee-total-reviews", employeeData.getNumberReviews());
                if (isAsync)
                    inv.open();
            });

        //Asynchronous loading
        UUID other = getOther(playerData);
        if (other != null)
            PlayerData.loadAndRun(other, (otherData, isAsync) -> {
                holders.register("has-been-reviewed", ContractsUtils.formatBoolean(otherData.hasReceivedReviewFor(this)));
                holders.register("other", otherData.getPlayerName());
                holders.register("other-reputation", ContractsUtils.formatNotation(otherData.getMeanNotation()));
                holders.register("other-total-reviews", otherData.getNumberReviews());

                if (getLastOfferProvider() == null)
                    holders.register("last-offer-player", otherData.getPlayerName());
                else
                    PlayerData.loadAndRun(getLastOfferProvider().getUUID(this), (lastOfferProviderData, isAsync2) -> {
                        holders.register("last-offer-player", getLastOfferProvider().getUUID(this).equals(playerData.getUuid()) ? "You" : lastOfferProviderData.getPlayerName());
                        if (isAsync2)
                            inv.open();
                    });


                if (isAsync)
                    inv.open();
            });

        holders.register("can-be-reviewed", ContractsUtils.formatBoolean(canLeaveReview(playerData)));
        holders.register("last-offer", getLastOfferProvider() != null ? getLastOffer() : "None");

        //Loads the entering time for each state.
        for (
                ContractState state : ContractState.values()) {
            holders.register(ContractsUtils.ymlName(state.toString()) + "-since",
                    hasBeenIn(state) ? ContractsUtils.formatTime(getEnteringTime(state)) : "Not been employer this state");
        }
        //Loads the type specific description of the contract.
        //This must absolutely be at the end as it will use all the other placeholders!
        holders.register("type-specific-description", holders.apply(playerData.getPlayer(), ContractsUtils.formatList(type.getSpecificDescription())));

        return holders;
    }

    public abstract boolean canDoSpecialAction(PlayerData playerData);

    public abstract void onSpecialAction(InventoryClickEvent e);

}
