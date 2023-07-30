package fr.phoenix.contracts.compat.adventure.tag.implementation;

import fr.phoenix.contracts.compat.adventure.resolver.implementation.VanillaColorResolver;
import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class VanillaColorTag extends AdventureTag {

    public VanillaColorTag() {
        super("black", new VanillaColorResolver(), true, true, "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white");
    }
}
