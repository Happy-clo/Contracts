package fr.phoenix.contracts.compat.adventure.resolver.implementation;

import fr.phoenix.contracts.compat.adventure.argument.AdventureArgumentQueue;
import fr.phoenix.contracts.compat.adventure.resolver.AdventureTagResolver;
import fr.phoenix.contracts.utils.AdventureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HexColorResolver implements AdventureTagResolver {

    @Override
    public @Nullable String resolve(@NotNull String tag, @NotNull AdventureArgumentQueue args) {
        return args.hasNext() ?
                AdventureUtils.getByHex(args.pop().value())
                        .map(chatColor -> "" + chatColor)
                        .orElse(null)
                : null;
    }
}
