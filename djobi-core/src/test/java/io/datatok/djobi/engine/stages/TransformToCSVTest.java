package io.datatok.djobi.engine.stages;

import com.google.inject.Inject;
import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.spark.actions.transformers.SparkCSVRunner;
import io.datatok.djobi.spark.data.SparkDataframe;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.test.StageTestUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class TransformToCSVTest {

    @Inject
    private StageTestUtils stageTestUtils;

    @Inject
    private Executor sparkExecutor;

    @Test()
    void testJSONFile() throws Exception {
        final Job job = getJob();

        Dataset<Row> ds = ((SQLContext) sparkExecutor.get("sql_context")).read().format("json").load("./src/test/resources/data/json_1").limit(2);
        SparkDataframe df = new SparkDataframe(ds);

        ActionRunResult runResult = stageTestUtils
            .builder()
                .attachJob(job)
                .addStageType(SparkCSVRunner.TYPE)
                .attachExecutor(SparkExecutor.TYPE)
            .run(df);

        Assertions.assertEquals(BasicDataKind.TYPE_STRING, runResult.getData().getKind().getType());

        Assertions.assertEquals("id,title\n1,Tom\n2,Alicia\n", runResult.getData().getData());
    }

    private Job getJob() throws Exception {
        final Job job = new Job();
        final Workflow workflow = new Workflow();

        job
            .setWorkflow(workflow)
        ;

        workflow
            .setExecutor(sparkExecutor)
            .setResourcesDir(new File("./src/test/resources"))
        ;

        return job;
    }
}
