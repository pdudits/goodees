package io.github.goodees.ese.store.subscription;

public interface Subscription {
    String getId();
    Cursor initialCursor();
}
