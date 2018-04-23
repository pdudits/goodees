package io.github.goodees.ese.store.subscription;

public class ObservableStoreException extends RuntimeException {
    public enum Fault {
        UNSUPPORTED_SPEC, INVALID_DESCRIPTOR, INVALID_CURSOR, STORE_FAULT;
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
