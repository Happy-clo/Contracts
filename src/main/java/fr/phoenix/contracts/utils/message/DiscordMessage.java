package fr.phoenix.contracts.utils.message;

import fr.phoenix.contracts.Contracts;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DiscordMessage {
    private final String message;

    private List<String> placeholders = new ArrayList<>();


    public DiscordMessage(String id, String... placeholders) {
        this.message=Contracts.plugin.configManager.discordMessages.getString(id);
        for (String placeholder : placeholders)
            this.placeholders.add(placeholder);
    }


    public void send() {
        String parsedMessage = message;
        for (int i = 0; i < placeholders.size(); i += 2)
            parsedMessage = parsedMessage.replace("{" + placeholders.get(i) + "}", placeholders.get(i + 1));
        try {
            for (String stringUrl : Contracts.plugin.configManager.discordWebhooks) {
                // Create a new URL object with the webhook URL
                URL url = new URL(stringUrl);

                // Create a new HttpURLConnection object and set the request method to POST
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");

                // Set the content type of the request to "application/json"
                con.setRequestProperty("Content-Type", "application/json");


                // Enable output on the HttpURLConnection object
                con.setDoOutput(true);

                // Write the JSON string to the output stream of the HttpURLConnection object
                OutputStream out = con.getOutputStream();
                out.write(parsedMessage.getBytes());
                out.flush();
                out.close();

                // Get the response code of the request
                int responseCode = con.getResponseCode();
                if (responseCode >= 400)
                    Contracts.log(Level.SEVERE, "Error while sending discord message. Response code: " + responseCode);
            }
        } catch (IOException e) {
            Contracts.log(Level.SEVERE, "Error while sending discord message: " + e.getMessage());
        }
    }
}

