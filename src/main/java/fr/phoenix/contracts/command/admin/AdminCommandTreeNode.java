package fr.phoenix.contracts.command.admin;

import fr.phoenix.contracts.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class AdminCommandTreeNode extends CommandTreeNode {

    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     */
    public AdminCommandTreeNode(CommandTreeNode parent) {
        super(parent, "admin");
        addChild(new PortfolioCommandTreeNode(this));
        addChild(new RemoveCommandTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
