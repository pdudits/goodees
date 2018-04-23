package io.github.goodees.ese.store.subscription;

public class ObservableStoreException extends RuntimeException {
    public static ObservableStoreException serializationError(Exception e) {
        return new ObservableStoreException(Fault.STORE_LOGIC_ERROR, "Error during de/serialization", e);
    }

    public static ObservableStoreException unsupportedSpec(EventSpec spec) {
        return new ObservableStoreException(Fault.UNSUPPORTED_SPEC, "Spec is not supported: "+spec, null);
    }

    public static ObservableStoreException invalidDescriptor(Subscription.Descriptor descriptor, Exception e) {
        return new ObservableStoreException(Fault.INVALID_DESCRIPTOR, "Invalid descriptor: "+descriptor, e);
    }

    public static ObservableStoreException invalidCursor(Cursor cursor, Exception e) {
        return new ObservableStoreException(Fault.INVALID_CURSOR, "Invalid cursor: "+cursor, e);
    }

    public static ObservableStoreException invalidDescriptorClass(Subscription.Descriptor descriptor) {
        return new ObservableStoreException(Fault.INVALID_DESCRIPTOR, "Unsupported descriptor class "+descriptor.getClass().getName(), null);
    }

    public static ObservableStoreException invalidCursorClass(Cursor cursor) {
        return new ObservableStoreException(Fault.INVALID_CURSOR, "Unsupported cursor class "+cursor.getClass().getName(), null);
    }

    public static ObservableStoreException implementationError(String message) {
        return new ObservableStoreException(Fault.STORE_LOGIC_ERROR, message, null);
    }

    public static ObservableStoreException duplicateActivation(String id) {
        return new ObservableStoreException(Fault.CLIENT_LOGIC_ERROR, "Duplicate activation for subscription "+id, null);
    }

    public enum Fault {
        UNSUPPORTED_SPEC, INVALID_DESCRIPTOR, INVALID_CURSOR, STORE_IO_FAULT, STORE_LOGIC_ERROR, CLIENT_LOGIC_ERROR;
    }

    private Fault fault;

    protected ObservableStoreException(Fault type, String message, Throwable cause) {
        super(message, cause);
        this.fault = type;
    }

    public Fault getFault() {
        return fault;
    }


}
