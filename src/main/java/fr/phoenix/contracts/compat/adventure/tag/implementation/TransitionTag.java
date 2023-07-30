package fr.phoenix.contracts.compat.adventure.tag.implementation;


import fr.phoenix.contracts.compat.adventure.resolver.implementation.TransitionResolver;
import fr.phoenix.contracts.compat.adventure.tag.AdventureTag;

public class TransitionTag extends AdventureTag {

    public TransitionTag() {
        super("transition", new TransitionResolver(), false, true);
    }
}
