package io.github.goodees.ese.store.subscription;

/**
 * A cursor into event stream. An opaque identifier into the stream of events. It actual meaning and form is
 * up to the store, however the cursor needs to be easily serializable in a event. This will allow the subscriber
 * to hold its current cursor in its state.
 *
 */
public interface Cursor<C> extends Comparable<C> {
    /**
     * Generate serialized version of a cursor.
     * @return
     */
    String toSerializedForm();

    /**
     * Generate deserialized representation of a cursor.
     * Store implementation is expected to accept its own cursor class and instance of SerializedCursor as valid parameter.
     * @param serializedForm
     * @return
     */
    static SerializedCursor fromSerializedForm(String serializedForm) {
        return new SerializedCursor(serializedForm);
    }

}
