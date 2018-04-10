package io.github.goodees.ese.store.subscription;

import io.github.goodees.ese.core.Event;
import io.github.goodees.ese.core.Request;

import java.util.List;

public interface EventStreamChunk extends Request<Void> {
    String subscriptionId();
    List<Event> events();
    Cursor nextCursor();
    boolean hasMoreChunks();
}
