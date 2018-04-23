package io.github.goodees.ese.store.subscription;

import java.util.*;

/**
 * Specification of event of interest to a subscriber.
 *
 * Some of the specifications may be defined in general, but mostly they will be store implementation specific, based
 * on what the store can query efficiently. Usual examples of spec would be limit to specific entity Ids, or event types.
 *
 */
public interface EventSpec {

    /**
     * Spec that matches events for any of provided entities.
     * @param entityIds
     * @return
     */
    static EntityIdEquals forEntities(String... entityIds) {
        return forEntities(Arrays.asList(entityIds));
    }

    /**
     * Spec that matches events for any of provided entities.
     * @param entityIds
     * @return
     */
    static EntityIdEquals forEntities(Collection<String> entityIds) {
        return new EntityIdEquals(entityIds);
    }

    static EventTypeEquals forEventTypes(String... eventTypes) {
        return forEventTypes(Arrays.asList(eventTypes));
    }

    static EventTypeEquals forEventTypes(Collection<String> eventTypes) {
        return new EventTypeEquals(eventTypes);
    }

    class EntityIdEquals implements EventSpec {
        private final Set<String> ids;

        EntityIdEquals(Collection<String> entityIds) {
            this.ids = Collections.unmodifiableSet(new HashSet<>(entityIds));
        }

        public Set<String> getIds() {
            return ids;
        }
    }

    class EventTypeEquals implements EventSpec {
        private final Set<String> types;

        EventTypeEquals(Collection<String> eventTypes) {
            this.types = Collections.unmodifiableSet(new HashSet<>(eventTypes));
        }

        public Set<String> getTypes() {
            return types;
        }
    }

    // TODO: Retroactive if subscriptions should not be retroactive by default. But I believe they should. What's opposite to retroactive?
    //class Retroactive {   }

}
