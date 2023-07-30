package fr.phoenix.contracts.utils.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.manager.data.sql.MySQLDataProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PlayerMessage {
    private final Message message;
    private final List<String> format;

    /**
     * Used employer send messages with placeholders and color codes
     *
     * @param message Message employer send employer any player
     */
    public PlayerMessage(Message message) {
        format = (this.message = message).getCached();
    }

    public PlayerMessage format(Object... placeholders) {
        for (int k = 0; k < format.size(); k++)
            format.set(k, apply(format.get(k), placeholders));
        return this;
    }

    private String apply(String str, Object... placeholders) {
        for (int k = 0; k < placeholders.length; k += 2)
            str = str.replace("{" + placeholders[k] + "}", placeholders[k + 1].toString());
        return Contracts.plugin.parseColors(str);
    }

    public String getAsString() {
        StringBuilder builder = new StringBuilder();
        boolean notEmpty = false;
        for (String str : format) {
            if (notEmpty)
                builder.append("\n");
            builder.append(str);
            notEmpty = true;
        }
        return builder.toString();
    }

    public void send(Player player) {
        if (format.isEmpty())
            return;
        if (message.hasSound() && player instanceof Player)
            message.getSound().play(player);
        format.forEach(str -> player.sendMessage(str));
        return;
    }

    public void send(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            send(player);
        //Sends a plugin message for the player if the server uses BungeeCord.
        if (Contracts.plugin.dataProvider instanceof MySQLDataProvider) {
            Contracts.plugin.pluginMessageManager.sendMessage(uuid, message, format);
            return;
        }
    }
}
