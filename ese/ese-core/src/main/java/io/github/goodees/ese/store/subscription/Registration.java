package io.github.goodees.ese.store.subscription;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Description of subscription requirements.
 */
public class Registration {
    private final String tag;
    private final Set<EventSpec> specs;
    private final String subscriberId;

    public Registration(String tag, String subscriberId, EventSpec... specs) {
        this.tag = tag;
        this.subscriberId = subscriberId;
        this.specs = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(specs)));
    }

    /**
     * Tag may be used to identify class of subscriptions, e. g. during initialization. Good tag value would be a
     * class name of implementation of subscriber.
     *
     * <p>The use case for tag and subscriber is to help query for the existing subscriptions.
     * When entity runtime initializes, so will find its subscriptions and start those entities. But tag itself might not
     * be enough here to identify the target entity.
     * @return
     */
    public String getTag() {
        return tag;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    /**
     * Specification of events subscriber is interested in. For event to be matched it needs to satisfy <em>ALL</em>
     * event specs.
     *
     * <p>The motivation for all is, that a subscriber can register multiple subscriptions in case it is interested
     * in more diverse set of events. Also the store may implement a compound spec representing logical or.</p>
     * @return
     */
    public Set<EventSpec> getEventSpecs() {
        return specs;
    }
}
