package io.github.goodees.ese.store.subscription.support;

import io.github.goodees.ese.store.subscription.ObservableStoreException;
import io.github.goodees.ese.store.subscription.SerializedDescriptor;
import io.github.goodees.ese.store.subscription.Subscription;

public abstract class DescriptorSupport<D extends Subscription.Descriptor> {
    private Class<D> descriptorClass;

    protected DescriptorSupport(Class<D> descriptorClass) {
        this.descriptorClass = descriptorClass;
    }

    protected final D parseDescriptor(Subscription.Descriptor descriptor) {
        if (descriptor == null) {
            throw ObservableStoreException.invalidDescriptor(null, null);
        }
        if (descriptorClass.isInstance(descriptor)) {
            return descriptorClass.cast(descriptor);
        } else if (descriptor instanceof SerializedDescriptor) {
            D parsedDescriptor = null;
            try {
                parsedDescriptor = parseDescriptor(descriptor.toSerializedForm());
            } catch (Exception e) {
                throw ObservableStoreException.invalidDescriptor(descriptor, e);
            }
            if (parsedDescriptor == null) {
                throw ObservableStoreException.implementationError("parseDescriptor returned null descriptor for "+descriptor);
            } else {
                return parsedDescriptor;
            }
        } else {
            throw ObservableStoreException.invalidDescriptorClass(descriptor);
        }
    }

    protected abstract D parseDescriptor(String s) throws Exception;

    protected abstract String getSubscriptionId(D descriptor);
}
