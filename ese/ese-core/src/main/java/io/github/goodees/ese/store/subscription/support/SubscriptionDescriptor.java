package io.github.goodees.ese.store.subscription.support;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.goodees.ese.store.subscription.EventSpec;
import io.github.goodees.ese.store.subscription.ObservableStoreException;
import io.github.goodees.ese.store.subscription.Registration;
import io.github.goodees.ese.store.subscription.Subscription;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Value.Immutable
public interface SubscriptionDescriptor extends Subscription.Descriptor {

    String storeImplementation();

    @Value.Default
    default int version() {
        return 1;
    }

    String subscriptionId();
    Optional<String> tag();
    String subscriberId();
    List<SerializedSpec> serializedSpec();

    @Value.Auxiliary
    default Set<EventSpec> eventSpec() {
        return serializedSpec().stream().map(SerializedSpec::toEventSpec).collect(toSet());
    }

    @JsonSubTypes({
            @JsonSubTypes.Type(name = "entityIds", value=EntityIds.class),
            @JsonSubTypes.Type(name = "eventTypes", value= EventTypes.class)})
    interface SerializedSpec {
        EventSpec toEventSpec();
    }

    @Value.Immutable
    interface EntityIds extends SerializedSpec {
        List<String> entityIds();

        @Override
        default EventSpec toEventSpec() {
            return EventSpec.forEntities(entityIds());
        }
    }

    @Value.Immutable
    interface EventTypes extends SerializedSpec {
        List<String> eventTypes();

        @Override
        default EventSpec toEventSpec() {
            return EventSpec.forEventTypes(eventTypes());
        }
    }

    @Override
    default String toSerializedForm() {
        try {
            return JacksonSerialization.mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw ObservableStoreException.serializationError(e);
        }
    }

    static Builder builder(String storeImplementation) {
        return new Builder().storeImplementation(storeImplementation);
    }

    static SubscriptionDescriptor forRegistration(String storeImplementation, String subscriptionId,
                                                  Registration registration) {
        return new Builder()
                .storeImplementation(storeImplementation)
                .subscriptionId(subscriptionId)
                .fromRegistration(registration)
                .build();
    }

    class Builder extends ImmutableSubscriptionDescriptor.Builder {
        public Builder addEventSpec(EventSpec spec) {
            if (spec instanceof EventSpec.EntityIdEquals) {
                return addSerializedSpec(ImmutableEntityIds.builder().addAllEntityIds(((EventSpec.EntityIdEquals) spec).getIds()).build());
            }
            if (spec instanceof EventSpec.EventTypeEquals) {
                return addSerializedSpec(ImmutableEventTypes.builder().addAllEventTypes(((EventSpec.EventTypeEquals) spec).getTypes()).build());
            }
            throw ObservableStoreException.unsupportedSpec(spec);
        }

        private Builder addAllEventSpecs(Iterable<? extends EventSpec> specs) {
            specs.forEach(this::addEventSpec);
            return this;
        }

        public Builder fromRegistration(Registration r) {
            return subscriberId(r.getSubscriberId())
                    .tag(r.getTag())
                    .addAllEventSpecs(r.getEventSpecs());
        }
    }

    static Support SUPPORT = new Support();

    class Support extends DescriptorSupport<SubscriptionDescriptor> {

        protected Support() {
            super(SubscriptionDescriptor.class);
        }

        @Override
        protected SubscriptionDescriptor parseDescriptor(String s) throws Exception {
            return JacksonSerialization.mapper.readValue(s, SubscriptionDescriptor.class);
        }

        @Override
        protected String getSubscriptionId(SubscriptionDescriptor descriptor) {
            return descriptor.subscriptionId();
        }

        @Override
        protected Set<EventSpec> getEventSpec(SubscriptionDescriptor descriptor) {
            return descriptor.eventSpec();
        }
    }
}
