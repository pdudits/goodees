package io.github.goodees.ese.store.subscription;

public class SerializedCursor implements Cursor<SerializedCursor> {

    private final String serializedForm;

    SerializedCursor(String serializedForm) {
        this.serializedForm = serializedForm;
    }

    @Override
    public String toSerializedForm() {
        return serializedForm;
    }

    @Override
    public int compareTo(SerializedCursor o) {
        throw new UnsupportedOperationException("Comparing serialized forms is not supported. Only implementation specific cursors are comparable");
    }
}
