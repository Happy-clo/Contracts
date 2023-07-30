package fr.phoenix.contracts.command.objects.parameter;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.command.objects.CommandTreeExplorer;
import fr.phoenix.contracts.player.PlayerData;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.function.BiConsumer;

public class Parameter {
    private final String key;
    private final BiConsumer<CommandTreeExplorer, List<String>> autoComplete;

    public static final Parameter PLAYER = new Parameter("<player>",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));
    public static final Parameter PLAYER_OPTIONAL = new Parameter("(player)",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));

    public Parameter(String key, BiConsumer<CommandTreeExplorer, List<String>> autoComplete) {
        this.key = key;
        this.autoComplete = autoComplete;
    }

    public String getKey() {
        return key;
    }

    public void autoComplete(CommandTreeExplorer explorer, List<String> list) {
        autoComplete.accept(explorer, list);
    }
}
