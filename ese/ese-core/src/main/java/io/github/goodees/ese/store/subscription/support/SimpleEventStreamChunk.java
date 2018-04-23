package io.github.goodees.ese.store.subscription.support;

import io.github.goodees.ese.core.Event;
import io.github.goodees.ese.store.subscription.Cursor;
import io.github.goodees.ese.store.subscription.EventStreamChunk;
import io.github.goodees.ese.store.subscription.ObservableStoreException;
import io.github.goodees.ese.store.subscription.Subscription;
import org.immutables.value.Value;

import java.util.*;

public class SimpleEventStreamChunk implements EventStreamChunk {

    private final String subscriptionId;
    private final Subscription.Descriptor descriptor;
    private final Cursor nextCursor;
    private final boolean hasMoreChunks;
    private final List<Event> events;

    private SimpleEventStreamChunk(Builder builder) {
        this.subscriptionId = builder.subscriptionId;
        this.descriptor = builder.descriptor;
        this.nextCursor = builder.nextCursor;
        this.hasMoreChunks = builder.hasMoreChunks;
        this.events = builder.events.size() == 1
                ? Collections.singletonList(builder.events.get(0))
                : Collections.unmodifiableList(builder.events);
    }

    @Override
    public String subscriptionId() {
        return subscriptionId;
    }

    @Override
    public Subscription.Descriptor descriptor() {
        return descriptor;
    }

    @Override
    public List<Event> events() {
        return events;
    }

    @Override
    public Cursor nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean hasMoreChunks() {
        return hasMoreChunks;
    }

    public static class Builder {
        private final String subscriptionId;
        private final Subscription.Descriptor descriptor;
        private Cursor nextCursor;
        private boolean hasMoreChunks;
        private final List<Event> events;

        Builder(String subscriptionId, Subscription.Descriptor descriptor) {
            this.subscriptionId = Objects.requireNonNull(subscriptionId, "subscriptionId must be provided");
            this.descriptor = Objects.requireNonNull(descriptor, "descriptor must be provided");
            this.events = new ArrayList<>();
        }

        public Builder add(Event event) {
            events.add(event);
            return this;
        }

        public Builder add(Collection<? extends Event> events) {
            this.events.addAll(events);
            return this;
        }

        public Builder nextCursor(Cursor cursor) {
            this.nextCursor = cursor;
            return this;
        }

        public Builder hasMoreChunks(boolean hasMore) {
            this.hasMoreChunks = hasMore;
            return this;
        }

        public Optional<EventStreamChunk> build() {
            if (events.isEmpty()) {
                return Optional.empty();
            }
            if (nextCursor == null) {
                throw ObservableStoreException.implementationError("Next cursor not set when events are emmited");
            }
            return Optional.of(new SimpleEventStreamChunk(this));
        }
    }
}
