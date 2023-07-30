package fr.phoenix.contracts.compat.adventure.tag.implementation;


import fr.phoenix.contracts.compat.adventure.resolver.implementation.AdventureColorResolver;
import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class AdventureColorTag extends AdventureTag {

    public AdventureColorTag() {
        super("color", new AdventureColorResolver(), true,true);
    }
}
