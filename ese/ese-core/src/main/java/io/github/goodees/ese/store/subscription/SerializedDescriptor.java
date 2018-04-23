package io.github.goodees.ese.store.subscription;

public class SerializedDescriptor implements Subscription.Descriptor {
    private final String serializedForm;

    SerializedDescriptor(String serializedForm) {
        this.serializedForm = serializedForm;
    }

    @Override
    public String toSerializedForm() {
        return serializedForm;
    }
}
