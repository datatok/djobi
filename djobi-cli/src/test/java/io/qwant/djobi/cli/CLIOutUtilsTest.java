package io.datatok.djobi.cli;

import io.datatok.djobi.cli.utils.CLIOutUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CLIOutUtilsTest {

    @Test void testCell() {
        String out = CLIOutUtils.cellBorder("toto");

        Assertions.assertEquals("+------+\n| toto |\n+------+", out);
    }

}
