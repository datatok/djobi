package io.datatok.djobi.cli;

import io.datatok.djobi.cli.utils.CLIUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CLIUtilsTest {

    @Test
    void parseEmptyArgumentsTest()
    {
        CLIUtils.parseParameters(new String[]{});
    }

    @Test
    void parseWrongArgumentsTest()
    {
        Assertions.assertEquals("toto", CLIUtils.parseParameters(new String[]{"--hello=toto", "--what=the"}).get("hello"));
    }
}
