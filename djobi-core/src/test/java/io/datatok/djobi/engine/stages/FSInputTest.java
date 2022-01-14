package io.datatok.djobi.engine.stages;

import io.datatok.djobi.engine.actions.fs.input.FSInputType;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.exceptions.NotFoundException;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.ActionTest;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.utils.Bag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

@ExtendWith(MyTestRunner.class)
class FSInputTest extends ActionTest {

    @Test void testMissingFile() {
        Assertions.assertThrows(NotFoundException.class, () -> stageTestUtils.run(FSInputType.TYPE, new Bag("path", "toto")));
    }

    @Test void testJSONFile() throws Exception {
        final File resourcesDirectory = new File("src/test/resources/data/json_1");

        ActionRunResult runResult =
            stageTestUtils
                .builder()
                    .attachExecutor(SparkExecutor.TYPE)
                    .configure(new Bag("path", resourcesDirectory.getAbsolutePath(), "format", "json"))
                    .addStageType(FSInputType.TYPE)
                    .run();

        Assertions.assertEquals(3, ((SparkDataframe) runResult.getData()).getData().count());
    }

}
