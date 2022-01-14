package io.datatok.djobi.engine.actions.net.sftp.output;

import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.StageTestUtils;
import io.datatok.djobi.utils.Bag;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;

public class SFTPOutputTest extends ActionTest {

    @Inject
    Provider<SFTPOutputRunner> runnerProvider;

    @Test
    public void testMustSuccess() throws Exception {
        /*Stage s = run(
                new Bag(
            "host", "sftp",
                    "port", "22",
                    "user", "foo",
                    "password", "pass",
                    "path", "test/my.txt"
                )
                , "Hello World!");*/
    }

    private Stage run(Bag args, StageData<?> data) throws Exception {
        final Stage stage = StageTestUtils.getNewStage();

        stage.setParameters(new SFTPOutputConfig(args, null, templateUtils));

        runnerProvider.get().run(stage, data, getSparkExecutor());

        return stage;
    }
}
