package fr.phoenix.contracts.command.admin;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.command.objects.parameter.Parameter;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ContractsUtils;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveCommandTreeNode extends CommandTreeNode {

    public RemoveCommandTreeNode(CommandTreeNode parent) {
        super(parent, "remove");
        addParameter(new Parameter("contract-name", ((commandTreeExplorer, list) -> Contracts.plugin.dataProvider
                .getContractManager().getContracts().forEach(contract -> list.add(ContractsUtils.enumName(contract.getName()))))));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("contracts.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have the right to execute this command");
                return CommandResult.FAILURE;
            }
            if (args.length != 3)
                return CommandResult.FAILURE;
            Contract contract = Contracts.plugin.dataProvider.getContractManager().getContracts()
                    .stream()
                    .filter(contract1 -> ContractsUtils.enumName(contract1.getName()).equalsIgnoreCase(args[2]))
                    .findFirst()
                    .orElse(null);
            if (contract == null) {
                sender.sendMessage(ChatColor.RED + "There isn't any contract with name " + ChatColor.YELLOW + args[2] + ChatColor.RED + ".");
                return CommandResult.FAILURE;
            }
            PlayerData.loadAndRun(contract.getEmployer(),
                    (employerData, isAsync) -> {
                        employerData.removeContract(contract);
                        if (contract.getEmployee() != null)
                            PlayerData.loadAndRun(contract.getEmployee(),
                                    (employeeData, async) -> {
                                        employeeData.removeContract(contract);
                                        Contracts.plugin.dataProvider.getContractManager().unregisterContract(contract);
                                    });
                        else
                            Contracts.plugin.dataProvider.getContractManager().unregisterContract(contract);
                    });


            if (contract.getState() == ContractState.AWAITING_EMPLOYEE) {
                Message.AWAITING_EMPLOYEE_CONTRACT_REMOVED_EMPLOYER.format("contract-name", contract.getName()).send(contract.getEmployer());
                //Give back the guarantee to all the other players.
                contract.getProposals()
                        .stream()
                        .forEach(proposal1 -> {
                            Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(proposal1.getEmployee()), proposal1.getGuarantee());
                            Message.CONTRACT_REMOVED_EMPLOYEE.format("guarantee", proposal1.getGuarantee(), "contract-name", contract.getName()).send(proposal1.getEmployee());
                        });
            }

            if (contract.getState() == ContractState.OPEN) {
                Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(contract.getEmployer()), contract.getAmount());
                Message.OPEN_CONTRACT_REMOVED_EMPLOYER.format("amount", contract.getAmount(), "contract-name", contract.getName()).send(contract.getEmployer());
                Contracts.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(contract.getEmployee()), contract.getGuarantee());
                Message.CONTRACT_REMOVED_EMPLOYEE.format("guarantee", contract.getGuarantee(), "contract-name", contract.getName()).send(contract.getEmployee());
            }


        }
        sender.sendMessage(ChatColor.RED + "You successfully removed the contract " + ChatColor.YELLOW + args[2] + ChatColor.RED + ".");
        return CommandResult.SUCCESS;
    }
}