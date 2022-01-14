package io.datatok.djobi.engine.actions.net.ftp.output;

import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.utils.templating.TemplateUtils;

import javax.inject.Inject;
import javax.inject.Provider;

public class FTPOutputTest {

    @Inject
    Provider<FTPOutputRunner> runnerProvider;

    @Inject
    private SparkExecutor sparkExecutor;

    @Inject
    private TemplateUtils templateUtils;
/*
    @Test
    public void testUnknownHost() {
        Assertions.assertThrows(ConnectException.class, () ->
            run(
                new Bag(

                )
            , "Hello World!")
        );
    }

    @Test
    public void testMustSuccess() throws Exception {
        Stage s = run(
                new Bag(
            "host", "",
                    "port", "2222",
                    "user", "foo",
                    "password", "pass"
                )
                , "Hello World!");
    }
*/


}
