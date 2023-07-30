package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.listener.temp.TemporaryListener;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.contract.review.ContractReview;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens employer chat input without any inventory
 */
public class ReviewChatInput extends TemporaryListener {
    private final PlayerData playerData;
    private final ContractReview contract;
    GeneratedInventory inv;
    private final TriFunction<PlayerData, String, ContractReview, Boolean> inputHandler;

    public ReviewChatInput(PlayerData playerData, GeneratedInventory inv, ContractReview contract, TriFunction<PlayerData, String, ContractReview, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList(), PlayerMoveEvent.getHandlerList());

        this.playerData = playerData;
        this.contract = contract;
        this.inputHandler = inputHandler;
        this.inv = inv;

        playerData.setOnChatInput(true);
    }

    public ReviewChatInput(PlayerData playerData, ContractReview contract, TriFunction<PlayerData, String, ContractReview, Boolean> inputHandler) {
    this(playerData,null,contract,inputHandler);
    }

    /**
     * Close only if the input handler accepts the message.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(playerData.getPlayer())) {
            event.setCancelled(true);
            if (inputHandler.apply(playerData, event.getMessage(), contract))
                close();
        }
    }

    @EventHandler
    public void cancel(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ())
            close();
    }

    @Override
    public void whenClosed() {
        playerData.setOnChatInput(false);
        if(inv!=null) {
            inv.open();
        }
    }
}
