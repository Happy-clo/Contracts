package fr.phoenix.contracts.utils;

import fr.phoenix.contracts.gui.objects.GeneratedInventory;
import fr.phoenix.contracts.listener.temp.TemporaryListener;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.function.BiFunction;

/**
 * Listens employer chat input if an inventory is precised, it will be opened at the end of the chat input.
 */
public class ChatInput extends TemporaryListener {
    private final PlayerData playerData;
    private final BiFunction<PlayerData, String, Boolean> inputHandler;
    private GeneratedInventory inv;

    public ChatInput(PlayerData playerData, GeneratedInventory inv, BiFunction<PlayerData, String, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList(), PlayerMoveEvent.getHandlerList());

        this.playerData = playerData;
        this.inputHandler = inputHandler;
        this.inv = inv;

        Message.CANCEL_CHAT_INPUT.format().send(playerData.getPlayer());
        //We close the inventory
        if (inv != null)
            playerData.getPlayer().getOpenInventory().close();
        playerData.setOnChatInput(true);
    }


    public ChatInput(PlayerData playerData, BiFunction<PlayerData, String, Boolean> inputHandler) {
        this(playerData, null, inputHandler);
    }

    /**
     * Close only if the input handler accepts the message.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(playerData.getPlayer())) {
            event.setCancelled(true);
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                close();
                return;
            }

            if (inputHandler.apply(playerData, event.getMessage()))
                close();
        }
    }

    @EventHandler
    public void cancel(PlayerMoveEvent event) {
        if (event.getPlayer().equals(playerData.getPlayer())) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ())
                close();
        }
    }

    @Override
    public void whenClosed() {
        playerData.setOnChatInput(false);
        if (inv != null) {
            inv.open();
        }
    }
}
