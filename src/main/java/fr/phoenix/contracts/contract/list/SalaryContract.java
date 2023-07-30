package fr.phoenix.contracts.contract.list;

import com.google.gson.JsonObject;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractParties;
import fr.phoenix.contracts.contract.ContractType;
import fr.phoenix.contracts.contract.Proposal;
import fr.phoenix.contracts.contract.parameter.Parameter;
import fr.phoenix.contracts.contract.parameter.ParameterType;
import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.gui.objects.item.Placeholders;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.UUID;

public class SalaryContract extends Contract {
    private int salaryPeriod = 10;
    private int periodPaidInAdvance;
    private long lastPeriodTime;

    public SalaryContract(UUID employer) {
        super(ContractType.SALARY, employer);
        loadParameters();
    }

    public SalaryContract(ConfigurationSection section) {
        super(ContractType.SALARY, section);
        salaryPeriod = section.getInt("salary-period");
        periodPaidInAdvance = section.getInt("period-paid-in-advance");
        lastPeriodTime = section.getLong("last-period-time");
        loadParameters();
    }

    public SalaryContract(JsonObject object) {
        super(ContractType.SALARY, object);
        salaryPeriod = object.get("salary-period").getAsInt();
        periodPaidInAdvance = object.get("period-paid-in-advance").getAsInt();
        lastPeriodTime = object.get("last-period-time").getAsLong();
        loadParameters();
    }

    @Override
    public void whenProposalAccepted(Proposal proposal) {
        super.whenProposalAccepted(proposal);
        lastPeriodTime = System.currentTimeMillis();
    }

    @Override
    public void loadParameters() {
        addParameter(new Parameter(ParameterType.SALARY_PERIOD, () -> Arrays.asList(salaryPeriod + ""), (player, s) -> {
            try {
                salaryPeriod = Integer.parseInt(s);
            } catch (Exception e) {
                Message.NOT_VALID_INTEGER.format("input", s).send(player);
            }
        }, () -> salaryPeriod <= 0));

    }

    @Override
    public boolean canDoSpecialAction(PlayerData playerData) {
        ContractParties contractParty = getContractParty(playerData.getUuid());
        if (contractParty == ContractParties.EMPLOYER)
            return true;
        //The employee can claim its money if there is some waiting for him.
        return false;

    }

    @Override
    public void onSpecialAction(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ContractParties contractParty = getContractParty(e.getWhoClicked().getUniqueId());
        if (contractParty == ContractParties.EMPLOYER) {
            if (Contracts.plugin.economy.getBalance(player) < amount) {
                Message.NOT_ENOUGH_MONEY.format("amount", amount).send(player);
                return;
            } else {
                Contracts.plugin.economy.withdrawPlayer(player, amount);
                periodPaidInAdvance += 1;
                Message.PAID_FOR_ONE_PERIOD.format("amount", amount, "contract-name", name).send(player);
            }
        }
    }

    public int getSalaryPeriod() {
        return salaryPeriod;
    }

    public long getLastPeriodTime() {
        return lastPeriodTime;
    }

    public void whenPeriodEnd() {
        lastPeriodTime = System.currentTimeMillis();
        if (periodPaidInAdvance == 0) {
            whenContractEnded();
        } else {
            Message.PAID_SALARY.format("period-paid-in-advance", periodPaidInAdvance, "contract-name", name).send(employer);
                    Message.RECEIVED_SALARY.format("amount", amount, "contract-name", name).send(employee);
            periodPaidInAdvance -= 1;
            Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(employee), amount);
        }
    }


    @Override
    public void save(FileConfiguration config) {
        super.save(config);
        String str = contractId.toString();
        config.set(str + ".salary-period", salaryPeriod);
        config.set(str + ".period-paid-in-advance", periodPaidInAdvance);
        config.set(str + ".last-period-time", lastPeriodTime);
    }

    public JsonObject getAsJsonObject() {
        JsonObject object = super.getAsJsonObject();
        object.addProperty("salary-period", salaryPeriod);
        object.addProperty("period-paid-in-advance", periodPaidInAdvance);
        object.addProperty("last-period-time", lastPeriodTime);
        return object;
    }


    @Override
    public Placeholders getContractPlaceholder(GeneratedInventory inv) {
        PlayerData playerData = inv.getPlayerData();
        Placeholders holders = super.getContractPlaceholder(inv);
        holders.register("salary-period", salaryPeriod);
        holders.register("period-paid-in-advance", periodPaidInAdvance);
        holders.register("time-before-next-period", ContractsUtils.formatTime(lastPeriodTime));
        //Loads the type specific description of the contract.
        //This must absolutely be at the end as it will use all the other placeholders!
        holders.register("type-specific-description", holders.apply(playerData.getPlayer(), ContractsUtils.formatList(type.getSpecificDescription())));

        return holders;
    }

}
