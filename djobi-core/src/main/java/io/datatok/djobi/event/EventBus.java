package io.datatok.djobi.event;

import org.apache.log4j.Logger;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EventBus (lite) implementation. This allows plugins to listen engine events, such as logger.
 */
@Singleton
public class EventBus {

    static final public String NAME_ALL = "*";

    static Logger logger = Logger.getLogger(EventBus.class);

    final Map<String, List<Provider<? extends Subscriber>>> subscriberProviders = new HashMap<>();

    final Map<String, List<Subscriber>> subscribers = new HashMap<>();

    /**
     * Listen a particular event.
     *
     * @param eventName String
     * @param subscriber subscriber
     */
    public void subscribe(final String eventName, Provider<? extends Subscriber> subscriber) {
        final List<Provider<? extends Subscriber>> providers =  this.subscriberProviders.getOrDefault(eventName, new ArrayList<>());

        providers.add(subscriber);

        this.subscriberProviders.put(eventName, providers);
    }

    /**
     * Listen all events.
     *
     * @param subscriber subscriber
     */
    public void subscribe(Provider<? extends Subscriber> subscriber) {
        final List<Provider<? extends Subscriber>> providers =  this.subscriberProviders.getOrDefault(NAME_ALL, new ArrayList<>());

        providers.add(subscriber);

        this.subscriberProviders.put(NAME_ALL, providers);
    }

    /**
     * Stop listen all events.
     *
     * @param provider Provider
     */
    public void unsubscribe(final Provider<? extends Subscriber> provider) {
        final Class<? extends Subscriber> prototype = provider.get().getClass();

        if (this.subscribers.containsKey(NAME_ALL)) {
            this.subscribers.put(NAME_ALL,
                this.subscribers.get(NAME_ALL).stream().filter(p -> p.getClass().equals(prototype)).collect(Collectors.toList())
            );
        }
    }

    /**
     * Stop listen all avents.
     */
    public void unsubscribe() {
        this.subscribers.remove(NAME_ALL);
    }

    public void trigger(final String eventName) {
        trigger(new Event() {
            @Override
            public String getName() {
                return eventName;
            }
        });
    }

    /**
     * Trigger event.
     *
     * @param event Event
     */
    public void trigger(final Event event) {
        final String eventName = event.getName();

        logger.debug("Trigger " + eventName);

        if (this.subscriberProviders.containsKey(eventName)) {
            this.subscribers.put(eventName, this.subscriberProviders.get(eventName)
                    .stream().map(p -> (Subscriber) p.get()).collect(Collectors.toList())
            );

            this.subscriberProviders.remove(eventName);
        }

        if (this.subscribers.containsKey(eventName)) {
            this.subscribers.get(eventName).forEach(s -> s.call(event));
        }

        if (this.subscriberProviders.containsKey(NAME_ALL)) {
            this.subscribers.put(NAME_ALL, this.subscriberProviders.get(NAME_ALL)
                    .stream().map(p -> (Subscriber) p.get()).collect(Collectors.toList())
            );

            this.subscriberProviders.remove(NAME_ALL);
        }

        if (this.subscribers.containsKey(NAME_ALL)) {
            this.subscribers.get(NAME_ALL).forEach(s -> s.call(event));
        }
    }

}
