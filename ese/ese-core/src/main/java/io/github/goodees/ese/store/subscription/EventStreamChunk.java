package io.github.goodees.ese.store.subscription;

import io.github.goodees.ese.core.Event;
import io.github.goodees.ese.core.Request;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Entity request delivering batch of events matched for a subscription.
 * @see ObservableStore#poll(Subscription.Descriptor, Cursor, int, Function)
 */
public interface EventStreamChunk extends Request<Void> {
    /**
     * The id of subscription events relate to.
     * @return
     */
    String subscriptionId();

    /**
     * Store-specific subscription descriptor. If the request used {@link Subscription.Descriptor#fromSerializedForm(String)}
     * for the descriptor, this request contains deserialized version of it which will be more effective for subsequent calls.
     * The client should store and use this descriptor for subsequent invocations.
     * @return
     */
    Subscription.Descriptor descriptor();

    /**
     * Batch of events that matches the descriptor.
     * @return
     */
    List<Event> events();

    /**
     * Cursor to use for fetching next batch of events.
     * @return
     */
    Cursor nextCursor();

    /**
     * Indication that there are more matching events. If store sets this to true in response to {@link ObservableStore#poll(Subscription.Descriptor, Cursor, int, Function)}, it expects client to call poll again.
     * Specifically, it might not invoke callback provided to {@link ObservableStore#activateNotification(Subscription.Descriptor, Consumer)}
     * when there are more matching events.
     * @return true if next request is guaranteed to return more chunks.
     */
    boolean hasMoreChunks();
}
