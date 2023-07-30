package fr.phoenix.contracts.compat.adventure.argument;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;

@ApiStatus.Internal
public class EmptyArgumentQueue extends AdventureArgumentQueue{

    public EmptyArgumentQueue() {
        super(Collections.emptyList());
    }
}
