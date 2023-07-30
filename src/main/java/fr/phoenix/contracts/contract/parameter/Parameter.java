package fr.phoenix.contracts.contract.parameter;

import fr.phoenix.contracts.utils.ContractsUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Parameter {
    private final ParameterType parameterType;
    private final Supplier<List<String>> get;
    private final BiConsumer<Player, String> setter;
    private final Supplier<Boolean> needsToBeFilled;

    public Parameter(ParameterType parameterType, Supplier<List<String>> get,
                     BiConsumer<Player, String> setter, Supplier<Boolean> needsToBeFilled) {
        this.parameterType = parameterType;
        this.get = get;
        this.setter = setter;
        this.needsToBeFilled = needsToBeFilled;
    }

    public boolean needsToBeFilled() {
        return needsToBeFilled.get();
    }

    public List<String> get() {
        if (!needsToBeFilled())
            return get.get();
        return new ArrayList<>();
    }

    public void set(Player player, String s) {
        setter.accept(player, s);
    }

    public String getId() {
        return parameterType.getId();
    }

    public String getName() {
        return parameterType.getName();
    }

    public List<String> getParameterType() {
        return parameterType.getDescription();
    }
}
