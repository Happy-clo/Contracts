package fr.phoenix.contracts.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.List;
import java.util.UUID;

public class PluginMessageManager implements PluginMessageListener {
    public static String BUNGEECORD_CHANNEL = "BungeeCord";
    public static String NOTIFY_CONTRACTS_SUBCHANNEL = "contracts_notify_contract";
    public static String NOTIFY_PLAYERDATA_SUBCHANNEL = "contracts_notify_playerdata";
    public static String MESSAGE_SUBCHANNEL = "contracts_messages";


    public void load() {
        //Setup the channels.
        Contracts.plugin.getServer().getMessenger().registerOutgoingPluginChannel(Contracts.plugin, BUNGEECORD_CHANNEL);
        Contracts.plugin.getServer().getMessenger().registerIncomingPluginChannel(Contracts.plugin, BUNGEECORD_CHANNEL, this);
    }


    /**
     * Plugin message to notify that updates have been made for a contract.
     */
    public void notifyContractUpdate(UUID uuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(NOTIFY_CONTRACTS_SUBCHANNEL);
        out.writeUTF(uuid.toString());
        Contracts.plugin.getServer().sendPluginMessage(Contracts.plugin, BUNGEECORD_CHANNEL, out.toByteArray());
    }

    /**
     * Plugin message to notify that updates have been made for a contract.
     */
    public void notifyPlayerDataUpdate(UUID uuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(NOTIFY_PLAYERDATA_SUBCHANNEL);
        out.writeUTF(uuid.toString());
        Contracts.plugin.getServer().sendPluginMessage(Contracts.plugin, BUNGEECORD_CHANNEL, out.toByteArray());
    }



    public void sendMessage(UUID playerUUID, Message message, List<String> format) {

        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF(MESSAGE_SUBCHANNEL);

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            DataOutputStream dataOutStream = new DataOutputStream(outStream);
            dataOutStream.writeUTF(playerUUID.toString());
            dataOutStream.writeUTF(message.toString());
            dataOutStream.writeInt(format.size());
            for (String str : format)
                dataOutStream.writeUTF(str);

            out.writeShort(outStream.toByteArray().length);
            out.write(outStream.toByteArray());
            outStream.close();
            dataOutStream.close();
            Contracts.plugin.getServer().sendPluginMessage(Contracts.plugin, BUNGEECORD_CHANNEL, out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equals(BUNGEECORD_CHANNEL)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String sub = in.readUTF();
            if (sub.equals(NOTIFY_CONTRACTS_SUBCHANNEL)) {
                UUID uuid = UUID.fromString(in.readUTF());
                Contracts.plugin.dataProvider.getContractManager().loadContract(uuid);
                return;
            }
            if (sub.equals(NOTIFY_PLAYERDATA_SUBCHANNEL)) {
                UUID uuid = UUID.fromString(in.readUTF());
                Contracts.plugin.dataProvider.getPlayerDataManager().load(uuid);
                return;
            }
            if (sub.equals(MESSAGE_SUBCHANNEL)) {
                try {
                    short length = in.readShort();
                    byte[] byteArray = new byte[length];
                    in.readFully(byteArray);
                    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(byteArray));
                    String uuid = inputStream.readUTF();
                    inputStream.close();
                    Player targetPlayer = Bukkit.getPlayer(UUID.fromString(uuid));
                    if (targetPlayer == null)
                        return;
                    Message message = Message.valueOf(inputStream.readUTF());
                    int size = inputStream.readInt();
                    if (message.hasSound())
                        message.getSound().play(player);
                    for (int i = 0; i < size; i++) {
                        targetPlayer.sendMessage(inputStream.readUTF());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
