package io.github.goodees.ese.store.subscription;

/**
 * The subscription for reception of events at Observable store.
 */
public interface Subscription {
    /**
     * Identifier of the subscription. Will be simple printable String as e. g. UUID.
     * @return
     */
    String getId();

    /**
     * Event-store specific descriptor of the subscription. Event stores might or might not persist information about
     * performed registrations. For the cases where they do not have any background processes for tracking matching
     * events, it is helpful to let client store the registration information. The descriptor is the place where
     * stores might store the data.
     * @return non-null descriptor. In case event store stores all the information, it might e. g. equal to subscription id.
     */
    Descriptor getDescriptor();

    /**
     * Cursor to use for initial fetch.
     * @return
     */
    Cursor initialCursor();

    /**
     * Store-specific representation of subscription, that is passed to client, and provided back to initialize the
     * event tracking processes for subscription upon activation.
     */
    interface Descriptor {
        /**
         * Serializable form of the description. Think something serializable as json string, as Base64 or json itself
         * @return
         */
        String toSerializedForm();

        static SerializedDescriptor fromSerializedForm(String serializedDescriptor) {
            return new SerializedDescriptor(serializedDescriptor);
        }
    }



}
