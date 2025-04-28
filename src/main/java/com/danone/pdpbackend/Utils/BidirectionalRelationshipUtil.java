package com.danone.pdpbackend.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for handling bidirectional relationships between entities
 */
public class BidirectionalRelationshipUtil {

    /**
     * Updates a bidirectional relationship between a parent entity and its child entities
     *
     * @param parent           The parent entity that owns the collection
     * @param parentCollection The current collection of child entities in the parent
     * @param newChildIds      The list of new child entity IDs to set
     * @param childService     A function that retrieves child entities by their IDs
     * @param idGetter         A function that gets the ID from a child entity
     * @param childSetter      A function that sets the parent reference in a child entity
     * @param <P>              The parent entity type
     * @param <C>              The child entity type
     * @param <ID>             The ID type
     */
    public static <P, C, ID> void updateBidirectionalRelationship(
            P parent,
            Collection<C> parentCollection,
            List<ID> newChildIds,
            Function<List<ID>, List<C>> childService,
            Function<C, ID> idGetter,
            BiConsumer<C, P> childSetter) {

        if (newChildIds == null) {
            return;
        }

        // Get the new entities
        List<C> newChildren = childService.apply(newChildIds);

        // Remove parent reference from any entities that will be removed
        parentCollection.stream()
                .filter(child -> !newChildIds.contains(idGetter.apply(child)))
                .forEach(child -> childSetter.accept(child, null));

        // Clear the collection
        parentCollection.clear();

        // Add entities and maintain bidirectional relationship
        for (C child : newChildren) {
            childSetter.accept(child, parent);
            parentCollection.add(child);
        }
    }
}