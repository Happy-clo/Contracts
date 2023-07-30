package fr.phoenix.contracts.compat.adventure.tag.implementation;


import fr.phoenix.contracts.compat.adventure.resolver.implementation.RainbowResolver;
import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class RainbowTag extends AdventureTag {

    public RainbowTag() {
        super("rainbow", new RainbowResolver(), false, true);
    }
}
