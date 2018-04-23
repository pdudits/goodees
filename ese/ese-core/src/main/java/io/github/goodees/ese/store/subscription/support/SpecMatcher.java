package io.github.goodees.ese.store.subscription.support;

import io.github.goodees.ese.core.Event;
import io.github.goodees.ese.store.subscription.EventSpec;
import io.github.goodees.ese.store.subscription.ObservableStoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class SpecMatcher {
    private final List<Predicate<Event>> specs;

    public SpecMatcher(Set<EventSpec> specs) {
        this.specs = specs.stream().map(SpecMatcher::matcher).collect(toList());
    }

    public boolean matches(Event e) {
        return specs.stream().allMatch(p -> p.test(e));
    }

    public static Predicate<Event> matcher(EventSpec spec) {
        if (spec instanceof EventSpec.EventTypeEquals) {
            return e -> ((EventSpec.EventTypeEquals) spec).getTypes().contains(e.getType());
        } else if (spec instanceof EventSpec.EntityIdEquals) {
            return e -> ((EventSpec.EntityIdEquals) spec).getIds().contains(e.entityId());
        } else {
            throw ObservableStoreException.unsupportedSpec(spec);
        }
    }
}
