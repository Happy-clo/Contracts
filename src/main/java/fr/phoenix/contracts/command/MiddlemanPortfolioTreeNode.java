package fr.phoenix.contracts.command;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiddlemanPortfolioTreeNode extends CommandTreeNode {

    public MiddlemanPortfolioTreeNode(CommandTreeNode parent) {
        super(parent, "middleman");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length != 2)
                return CommandResult.THROW_USAGE;
            PlayerData playerData = PlayerData.get(player);
            if (!player.hasPermission("contracts.middleman")) {
                player.sendMessage(ChatColor.RED + "This command is only for middlemen.");
                return CommandResult.FAILURE;
            }
            InventoryManager.CONTRACT_MIDDLEMAN.generate(playerData, null).open();
        }
        return CommandResult.FAILURE;
    }
}
