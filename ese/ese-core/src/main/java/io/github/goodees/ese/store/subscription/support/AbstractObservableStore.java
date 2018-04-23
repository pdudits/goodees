package io.github.goodees.ese.store.subscription.support;

import io.github.goodees.ese.store.subscription.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class AbstractObservableStore<C extends Cursor<C>, D extends Subscription.Descriptor> implements ObservableStore {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final CursorSupport<C> cursorSupport;
    private final DescriptorSupport<D> descriptorSupport;
    private ConcurrentMap<String, ActiveSubscription> activeSubscriptions = new ConcurrentHashMap<>();

    protected AbstractObservableStore(CursorSupport<C> cursorSupport, DescriptorSupport<D> descriptorSupport) {
        this.cursorSupport = cursorSupport;
        this.descriptorSupport = descriptorSupport;
    }

    @Override
    public Subscription register(Registration registration) {
        String subscriptionId = UUID.randomUUID().toString();
        D descriptor = createDescriptor(subscriptionId, registration);
        C cursor = createInitialCursor(subscriptionId, registration, descriptor);
        return createSubscription(subscriptionId, descriptor, cursor);
    }

    protected Subscription createSubscription(String subscriptionId, D descriptor, C cursor) {
        return new SimpleSubscription(subscriptionId, descriptor, cursor);
    }

    protected abstract C createInitialCursor(String subscriptionId, Registration registration, D descriptor);

    protected abstract D createDescriptor(String subscriptionId, Registration registration);

    protected D parseDescriptor(Subscription.Descriptor descriptor) {
        return descriptorSupport.parseDescriptor(descriptor);
    }

    protected C parseCursor(Cursor<?> cursor) {
        return cursorSupport.parseCursor(cursor);
    }

    @Override
    public void poll(Subscription.Descriptor subscription, Cursor cursor, int chunkSize, EventStreamChunk.Dispatch dispatch) {
        D parsedDescriptor = parseDescriptor(subscription);
        C parsedCursor = parseCursor(cursor);
        String subscriptionId = getSubscriptionId(parsedDescriptor);
        SimpleEventStreamChunk.Builder builder = new SimpleEventStreamChunk.Builder(subscriptionId, parsedDescriptor);
        poll(parsedDescriptor, parsedCursor, chunkSize, builder);
        builder.build().ifPresent(r -> {
            ActiveSubscription activeSub = getActiveSubscription(subscriptionId);
            CompletionStage<Cursor<?>> completion = dispatch.dispatch(r);
            if (activeSub != null) {
                activeSub.sent(r, completion);
            }
        });
    }

    protected abstract void poll(D subscription, C cursor, int chunkSize, SimpleEventStreamChunk.Builder builder);

    protected String getSubscriptionId(D parsedDescriptor) {
        return descriptorSupport.getSubscriptionId(parsedDescriptor);
    }

    protected abstract ExecutorService executorService();

    @Override
    public void activateNotification(Subscription.Descriptor subscription, Consumer<String> callback) {
        ActiveSubscription sub = new ActiveSubscription(parseDescriptor(subscription), callback);
        if (activeSubscriptions.putIfAbsent(sub.id, sub) != null) {
            throw ObservableStoreException.duplicateActivation(sub.id);
        }
    }

    @Override
    public void deactivateNotification(Subscription.Descriptor subscription) {
        activeSubscriptions.remove(getSubscriptionId(parseDescriptor(subscription)));
    }

    @Override
    public void unregister(Subscription.Descriptor subscription) {
        deactivateNotification(subscription);
    }

    private ActiveSubscription getActiveSubscription(String subscriptionId) {
        return activeSubscriptions.get(subscriptionId);
    }

    protected abstract Set<EventSpec> getSubscriptionSpec(D descriptor);

    enum SubscriptionStatus {
        IDLE, NOTIFIED, INCOMPLETE_CHUNK_SENT, COMPLETE_CHUNK_SENT, PENDING_NOTIFICATION;
    }

    class ActiveSubscription {
        final String id;
        final AtomicReference<C> sentNextCursor = new AtomicReference<>(null);
        final AtomicReference<SubscriptionStatus> status = new AtomicReference<>(SubscriptionStatus.IDLE);
        final Set<EventSpec> spec;
        final Consumer<String> callback;

        public ActiveSubscription(D descriptor, Consumer<String> callback) {
            this.id = getSubscriptionId(descriptor);
            this.callback = callback;
            this.spec = getSubscriptionSpec(descriptor);
        }

        boolean shouldCheck() {
            SubscriptionStatus s = status.get();
            return s == SubscriptionStatus.COMPLETE_CHUNK_SENT || s == SubscriptionStatus.IDLE;
        }

        boolean shouldNotify() {
            return status.compareAndSet(SubscriptionStatus.IDLE, SubscriptionStatus.NOTIFIED)
                    || status.compareAndSet(SubscriptionStatus.COMPLETE_CHUNK_SENT, SubscriptionStatus.PENDING_NOTIFICATION);
        }

        void sent(EventStreamChunk request, CompletionStage<Cursor<?>> completion) {
            if (mostRecentSent(request)) {
                if (request.hasMoreChunks()) {
                    status.set(SubscriptionStatus.INCOMPLETE_CHUNK_SENT);
                } else {
                    status.set(SubscriptionStatus.COMPLETE_CHUNK_SENT);
                }
                completion.whenCompleteAsync((c, t) -> {
                    try {
                        C parsedCursor = parseCursor(c);
                        C sentCursor = sentNextCursor.get();
                        if (isSecondMoreRecent(sentCursor, parsedCursor)) {
                            completed();
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse returned cursor {} from chunk request {}", c, request.subscriptionId(), e);
                        // is it wise to complete here?
                        completed();
                    }
                }, executorService());
            }
        }

        private boolean isSecondMoreRecent(C sentCursor, C parsedCursor) {
            return sentCursor == null || parsedCursor.compareTo(sentCursor) >= 0;
        }


        private boolean mostRecentSent(EventStreamChunk request) {
            // most recent request has the biggest next cursor.
            C cursor = parseCursor(request.nextCursor());
            return sentNextCursor.updateAndGet(c -> isSecondMoreRecent(c, cursor) ? cursor : c) == cursor;
        }

        void completed() {
            if (status.compareAndSet(SubscriptionStatus.PENDING_NOTIFICATION, SubscriptionStatus.NOTIFIED)) {
                sendNotification();
            } else if (status.get() != SubscriptionStatus.INCOMPLETE_CHUNK_SENT) {
                status.set(SubscriptionStatus.IDLE);
            }
        }

        void sendNotification() {
            executorService().submit(() -> callback.accept(id));
        }
    }
}
