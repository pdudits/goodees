package io.github.goodees.ese.store.subscription;


import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ObservableStore {
    /**
     * Register a subscription. The store prepares its internal state for tracking a subscription.
     * @param registration
     * @return
     */
    Subscription register(Registration registration);

    /**
     * Register a request for chunk of events. The store will eventually query itself for new events for the subscription
     * past the given cursor, and will call {@code dispatch} function with a {@link EventStreamChunk} representing up to
     * {@code chunkSize} new matching events.
     *
     * <p>If there are no new events, it will call the dispatch function with an {@code EventStreamChunk} representing
     * 0 events.</p>.
     *
     * <p>As seen from the signature, the poll itself might be asynchronous, observable store may queue the request.</p>
     *
     * <p>The result of the dispatch function may be used to optimize notification flow, disabling any online matching
     * of events until the returned CompletionStage completes.</p>
     *
     *
     * @param subscription descriptor of the subscription either in original or serialized form
     * @param cursor subscription's cursor. Implementations should accept their own specific cursor as well as be able to
     *               deserialize a {@link SerializedCursor}.
     * @param chunkSize maximum number of events to provide in an EventStreamChunk.
     * @param dispatch
     */
    void poll(Subscription.Descriptor subscription, Cursor cursor, int chunkSize, Function<EventStreamChunk, CompletionStage<Void>> dispatch);

    /**
     * Register a notification callback for an subscription. When the store identifies a message matching this subscription
     * having been persisted, it will invoke the callback. The callback should invoke the subscriber who can then poll
     * for the new events.
     * <p>Store will cease calling futher callbacks until {@link #poll(String, Cursor, int, Function) poll} is invoked
     * for the subscription.</p>
     * @param subscription descriptor of the subscription either in original or serialized form
     * @param callback callback should be quick and just indirectly queue execution of actual poll process. It will be
     *                 called with subscriptionId passed as a parameter
     */
    void activateNotification(Subscription.Descriptor subscription, Consumer<String> callback);

    /**
     * Unregister notification callback for the subscription.
     * @param subscription
     */
    void deactivateNotification(Subscription.Descriptor subscription);

    /**
     * Unregister a subscription. Notification call back as well as all internal state for the subscription is removed
     * from the store.
     * @param subscription
     */
    void unregister(Subscription.Descriptor subscription);
}