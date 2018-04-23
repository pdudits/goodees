package io.github.goodees.ese.store.subscription;

import io.github.goodees.ese.core.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toSet;

/**
 * A sample implementation of subscriber to a {@link ObservableStore}.
 *
 * <p>It is designed as a mixin class to an EventSourcedEntity and handle the aspects of continuously query the store.</p>
 */
public abstract class Subscriber {
    private final Logger defaultLogger = LoggerFactory.getLogger(getClass());
    private final ObservableStore store;
    private String subscriberId;
    private Map<String, SubscriptionState> subscriptions;

    protected Subscriber(ObservableStore store, String subscriberId) {
        this.store = store;
        this.subscriberId = subscriberId;
    }

    /**
     * Register a subscription.
     * @param tag
     * @param specs
     */
    public void register(String tag, EventSpec... specs) {
        Subscription subscription = store.register(new Registration(tag, subscriberId, specs));
        SubscriptionState state = get(subscription.getId());
        persistEvent(eventFactory().subscriptionRegistered(subscription.getId(), tag,
                state.willPersistDescriptor(subscription.getDescriptor()),
                state.willPersistCursor(subscription.initialCursor())),
                () -> {
            if (shouldActivateNotification(tag)) {
                store.activateNotification(state.descriptor, notificationCallback());
            }
        });
    }

    public void unregister(String subscriptionId) {
        store.unregister(get(subscriptionId).descriptor);
    }

    private SubscriptionState get(String subscriptionId) {
        return subscriptions.computeIfAbsent(subscriptionId, SubscriptionState::new);
    }

    public void poll(String subscriptionId) {
        SubscriptionState state = get(subscriptionId);
        if (state.lastCursor == null) {
            logger().error("Poll was invoked for {}, but there is no cursor, which is quite impossible", subscriptionId);
        }
        store.poll(state.descriptor, state.lastCursor, preferredChunkSize(), pollCallback());
    }

    /**
     * Handler for incoming {@link EventStreamChunk} request. It shall pass all events to {@link #processEvent(String, String, Event)}
     * and then update the cursor.
     * <p>When store signals that there are more matchin events, it will invoke another poll with the new cursor immediately</p>
     * @param chunk
     */
    public void onEventStreamChunk(EventStreamChunk chunk) {
        SubscriptionState state = get(chunk.subscriptionId());
        for (Event event : chunk.events()) {
            processEvent(state.tag, state.subscriptionId, event);
        }

        persistEvent(eventFactory().cursorUpdated(state.subscriptionId, state.willPersistCursor(chunk.nextCursor())), () ->
        {
            if (chunk.hasMoreChunks()) {
                // if there are more events, ask for next chunk
                poll(state.subscriptionId);
            }
        });
    }

    /**
     * Handler for subscriber initialization. Owning entity should delegate to this call from its method {@code initialize}.
     * <p>It will poll for all active subscriptions and register notifications for any tags that require that</p>
     */
    public void initialize() {
        // we should poll all subscriptions first to get up-to-date as well.
        subscriptions.keySet().forEach(this::poll);

        // register notifications for relevant subscriptions
        subscriptions.values().stream().filter(s -> shouldActivateNotification(s.tag))
                .forEach(s -> store.activateNotification(s.descriptor, notificationCallback()));
    }

    /**
     * Main processing method for matched events.
     * @param tag tag of subscription
     * @param subscriptionId id of subscription
     * @param event matched event
     */
    protected abstract void processEvent(String tag, String subscriptionId, Event event);

    /**
     * Update state from any relevant subscription events.
     * <p>Entity's updateState must delegate any subscriber-relevant events to this method.</p>
     * @param event
     */
    public void updateState(SubscriptionEvent event) {
        SubscriptionState state = get(event.subscriptionId());
        state.update(event);
    }

    public Set<String> allSubscriptions() {
        return subscriptions.keySet();
    }

    public Set<String> allSubscriptions(String tag) {
        return subscriptions.values().stream().filter(s -> Objects.equals(tag, s.tag))
                .map(s -> s.subscriptionId).collect(toSet());
    }


    protected Logger logger() {
        return defaultLogger;
    }

    protected int preferredChunkSize() {
        return 15;
    }

    /**
     * Check if notification callback should be registered with the store.
     * @param tag
     * @return
     */
    protected abstract boolean shouldActivateNotification(String tag);

    /**
     * Notification callback should invoke action, that will queue the call of {@link #poll(String)}
     * @return
     */
    protected abstract Consumer<String> notificationCallback();

    /**
     * Dispatch method to pass to the store. The method should result in call of {@link #onEventStreamChunk(EventStreamChunk)}
     * @return
     */
    protected abstract Function<EventStreamChunk, CompletionStage<Void>> pollCallback();

    /**
     * Serves for constructing Event instances appropriate for the entity that uses subscriber mixin.
     * @return
     */
    protected abstract EventFactory eventFactory();

    /**
     * Serves for persisting events related to owner entity. Since the persist methods may be synchronous or asynchronous,
     * it is up to implementation to invoke {@code whenComplete} after events are successfully stored.
     * @param event
     * @param whenComplete
     */
    protected abstract void persistEvent(SubscriptionEvent event, Runnable whenComplete);

    interface EventFactory {
        SubscriptionRegistered subscriptionRegistered(String subscriptionId, String tag, String descriptor, String cursor);
        CursorUpdated cursorUpdated(String subscriptionId, String cursor);
    }

    interface SubscriptionEvent {
        String subscriptionId();
    }

    interface CursorUpdated extends SubscriptionEvent {
        String cursor();
    }

    interface SubscriptionRegistered extends CursorUpdated {
        String descriptor();
        String tag();
    }

    private class SubscriptionState {
        private final String subscriptionId;
        private String tag;

        private Cursor lastCursor;
        private Subscription.Descriptor descriptor;

        private Cursor cursorToBeStored;
        private String serializedCursorToBeStored;
        private String serializedDescriptorToBeStored;
        private Subscription.Descriptor descriptorToBeStored;

        SubscriptionState(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        private String willPersistCursor(Cursor c) {
            String serialized = c.toSerializedForm();
            this.serializedCursorToBeStored = serialized;
            this.cursorToBeStored = c;
            return serialized;
        }

        private String willPersistDescriptor(Subscription.Descriptor descriptor) {
            String serialized = descriptor.toSerializedForm();
            this.serializedDescriptorToBeStored = serialized;
            this.descriptorToBeStored = descriptor;
            return serialized;
        }

        void updateCursor(String serializedCursor) {
            if (serializedCursor.equals(serializedCursorToBeStored)) {
                this.lastCursor = cursorToBeStored;
            } else {
                this.lastCursor = Cursor.fromSerializedForm(serializedCursor);
            }
            this.cursorToBeStored = null;
            this.serializedCursorToBeStored = null;
        }

        void updateDescriptor(String serializedDescriptor) {
            if (serializedDescriptor.equals(serializedDescriptorToBeStored)) {
                this.descriptor = descriptorToBeStored;
            } else {
                this.descriptor = Subscription.Descriptor.fromSerializedForm(serializedDescriptor);
            }
            this.descriptorToBeStored = null;
            this.serializedCursorToBeStored = null;
        }

        public void update(SubscriptionEvent event) {
            if (event instanceof SubscriptionRegistered) {
                this.tag = ((SubscriptionRegistered) event).tag();
                updateDescriptor(((SubscriptionRegistered) event).descriptor());
            }
            if (event instanceof CursorUpdated) {
                updateCursor(((CursorUpdated)event).cursor());
            }
        }
    }
}
