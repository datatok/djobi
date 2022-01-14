package io.datatok.djobi.test;

import io.datatok.djobi.utils.MyMapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class MyMapUtilsTest {

    @Test void browseMapTest() throws IOException {
        Assertions.assertEquals("toto", MyMapUtils.browse(MyMapUtils.map("hello", "toto"), "hello"));

        Assertions.assertEquals("toto", MyMapUtils.browse(MyMapUtils.map("hello", MyMapUtils.map("world", "toto")), "hello.world"));

        Assertions.assertEquals("2", MyMapUtils.browse(MyMapUtils.map("hello", Arrays.asList("1", "2")), "hello.1"));
    }

    @Test void flattenTest() {
        Map<String, Object> source = MyMapUtils.map("a", MyMapUtils.map("b", "b_v", "c", MyMapUtils.map("d", "e")), "f", "g");

        Map<String, Object> flatten = MyMapUtils.flattenKeys(source);

        Assertions.assertEquals(MyMapUtils.map("a.b", "b_v", "a.c.d", "e", "f", "g"), flatten);
    }

}
