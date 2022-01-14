package io.datatok.djobi.utils;

import io.datatok.djobi.test.MyTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(MyTestRunner.class)
public class ActionArgFactoryTest {

    @Inject
    private ActionArgFactory actionArgFactory;


    @Test
    public void get() {
        Assertions.assertEquals("world", actionArgFactory.get("hello", "world", MyMapUtils.mapString()).getValue());
        Assertions.assertEquals("1", actionArgFactory.get("hello", 1, MyMapUtils.mapString()).getValue());
        Assertions.assertEquals("world", actionArgFactory.get("hello", MyMapUtils.mapString("value", "world"), MyMapUtils.mapString()).getValue());
        Assertions.assertEquals("1", actionArgFactory.get("hello", MyMapUtils.map("value", 1), MyMapUtils.mapString()).getValue());
    }

}
