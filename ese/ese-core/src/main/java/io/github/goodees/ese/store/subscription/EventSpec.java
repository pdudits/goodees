package io.github.goodees.ese.store.subscription;

/**
 * Specification of event of interest to a subscriber.
 *
 * Some of the specifications may be defined in general, but mostly they will be store implementation specific, based
 * on what the store can query efficiently. Usual examples of spec would be limit to specific entity Ids, or event types.
 *
 * <p>For consideration: Spec is a soft constraint - store may attempt to deliver only events matching spec, but may also return
 * non-matching events.</p>
 */
public interface EventSpec {
}
