package fr.phoenix.contracts.compat.adventure.tag.implementation;


import fr.phoenix.contracts.compat.adventure.resolver.implementation.GradientResolver;
import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class GradientTag extends AdventureTag {

    public GradientTag() {
        super("gradient", new GradientResolver(), false, true);
    }
}
