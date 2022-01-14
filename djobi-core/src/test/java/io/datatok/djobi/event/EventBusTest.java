package io.datatok.djobi.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicInteger;

public class EventBusTest {

    @Inject
    private EventBus eventBus;

    @Test void testEventBus() {
        final AtomicInteger flag = new AtomicInteger(0);
        final Provider<Subscriber> provider = () -> event -> flag.incrementAndGet();

        this.eventBus.subscribe("toto", provider);
        this.eventBus.subscribe("toto", provider);

        this.eventBus.trigger(new Event() {
            @Override
            public String getName() {
                return "toto";
            }
        });

        Assertions.assertEquals(2, flag.get());

    }

    @Test void testEventBusCatchAll() {
        final AtomicInteger flag = new AtomicInteger(0);
        final Provider<Subscriber> provider = () -> event -> flag.incrementAndGet();

        this.eventBus.subscribe(provider);
        this.eventBus.subscribe("toto", provider);

        this.eventBus.trigger(new Event() {
            @Override
            public String getName() {
                return "tata";
            }
        });

        Assertions.assertEquals(1, flag.get());

    }

}
