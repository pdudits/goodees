package io.github.goodees.ese.store.subscription;

public class SerializedCursor implements Cursor {
    private final String serializedForm;

    SerializedCursor(String serializedForm) {
        this.serializedForm = serializedForm;
    }

    @Override
    public String toSerializedForm() {
        return serializedForm;
    }
}
