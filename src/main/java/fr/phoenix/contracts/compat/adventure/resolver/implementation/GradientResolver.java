package fr.phoenix.contracts.compat.adventure.resolver.implementation;

import fr.phoenix.contracts.compat.adventure.argument.AdventureArgumentQueue;
import fr.phoenix.contracts.compat.adventure.gradient.GradientBuilder;
import fr.phoenix.contracts.compat.adventure.gradient.Interpolator;
import fr.phoenix.contracts.compat.adventure.resolver.ContextTagResolver;
import fr.phoenix.contracts.utils.AdventureUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GradientResolver implements ContextTagResolver {

    @Override
    public @Nullable String resolve(@NotNull String src, @NotNull AdventureArgumentQueue argsQueue, @NotNull String context, @NotNull List<String> decorations) {
        if (!argsQueue.hasNext())
            return GradientBuilder.rgbGradient(context, Color.WHITE, Color.BLACK, 0, Interpolator.LINEAR, decorations);
        List<String> args = new ArrayList<>();
        while (argsQueue.hasNext())
            args.add(argsQueue.pop().value());
        double phase = getPhase(args);
        if (args.size() > 2)
            return GradientBuilder.multiRgbGradient(context, args.stream().map(AdventureUtils::color).toArray(Color[]::new), phase, Interpolator.LINEAR, decorations);
        final Color c1 = AdventureUtils.color(args.get(0));
        if (args.size() == 1)
            return GradientBuilder.rgbGradient(context, c1, Color.BLACK, phase, Interpolator.LINEAR, decorations);
        return GradientBuilder.rgbGradient(context, c1, AdventureUtils.color(args.get(1)), phase, Interpolator.LINEAR, decorations);
    }

    private double getPhase(List<String> args) {
        String lastArg = args.get(args.size() - 1);
        try {
            double phase = Double.parseDouble(lastArg);
            args.remove(args.size() - 1);
            return phase;
        } catch (NumberFormatException e) {
            return 1d;
        }
    }

}
