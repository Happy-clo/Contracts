package fr.phoenix.contracts.command.admin;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import fr.phoenix.contracts.manager.InventoryManager;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortfolioCommandTreeNode extends CommandTreeNode {
    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     */
    public PortfolioCommandTreeNode(CommandTreeNode parent) {
        super(parent, "portfolio");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player= (Player) sender;
            if (!player.hasPermission("contracts.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have the right to execute this command");
                return CommandResult.FAILURE;
            }
            InventoryManager.CONTRACT_ADMIN.generate(PlayerData.get(player),null).open();
            return CommandResult.SUCCESS;
        }
        return CommandResult.FAILURE;
    }
}
