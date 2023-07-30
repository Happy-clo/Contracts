package fr.phoenix.contracts.compat.adventure.tag.implementation;


import fr.phoenix.contracts.compat.adventure.resolver.implementation.HexColorResolver;
import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class HexColorTag extends AdventureTag {

    public HexColorTag() {
        super("HEX", new HexColorResolver(), false,true,"#");
    }
}
