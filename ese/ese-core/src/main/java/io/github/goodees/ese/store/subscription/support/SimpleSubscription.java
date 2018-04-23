package io.github.goodees.ese.store.subscription.support;

import io.github.goodees.ese.store.subscription.Cursor;
import io.github.goodees.ese.store.subscription.Subscription;

public class SimpleSubscription implements Subscription {
    private final String id;
    private final Descriptor descriptor;
    private final Cursor cursor;

    public SimpleSubscription(String id, Descriptor descriptor, Cursor cursor) {
        this.id = id;
        this.descriptor = descriptor;
        this.cursor = cursor;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public Cursor initialCursor() {
        return cursor;
    }
}
