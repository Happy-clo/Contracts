package fr.phoenix.contracts.compat.adventure.resolver.implementation;


import fr.phoenix.contracts.compat.adventure.argument.AdventureArgumentQueue;
import fr.phoenix.contracts.compat.adventure.resolver.AdventureTagResolver;
import fr.phoenix.contracts.utils.AdventureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdventureColorResolver implements AdventureTagResolver {

    @Override
    public @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue args) {
        return args.hasNext() ?
                AdventureUtils.getByName(args.peek().value())
                        .map(c -> "" + c)
                        .orElse(AdventureUtils.getByHex(args.pop().value())
                                .map(c -> "" + c)
                                .orElse(null))
                : null;
    }
}
