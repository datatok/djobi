package io.datatok.djobi.engine;

import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.check.CheckStatus;
import io.datatok.djobi.engine.data.BasicDataKind;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataListString;
import io.datatok.djobi.engine.enums.ExecutionStatus;
import io.datatok.djobi.engine.flow.ConditionFlow;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.phases.ConfigurePhase;
import io.datatok.djobi.engine.phases.PreCheckJobPhase;
import io.datatok.djobi.event.EventBus;
import io.datatok.djobi.event.Subscriber;
import io.datatok.djobi.loaders.yaml.YAMLWorkflowLoader;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.test.executor.DummyExecutor;
import io.datatok.djobi.utils.MyMapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MyTestRunner.class)
class EngineTest {

    @Inject
    private Engine engine;

    @Inject
    private EventBus eventBus;

    @Inject
    private YAMLWorkflowLoader yamlPipelineLoader;

    @Inject
    private Configuration configuration;

    @Inject
    private ConfigurePhase configurePhase;

    @Inject
    private PreCheckJobPhase preCheckJobPhase;

    @Inject
    private ConditionFlow conditionFlow;

    @Inject
    private ExecutionContext executionContext;

    @Test void testPreCheck() throws Exception {
        final Workflow workflow = getWorkflow("good_dummy.yml");

        workflow.setExecutor(new DummyExecutor());

        configurePhase.execute(workflow.getJob(0));
        configurePhase.execute(workflow.getJob(1));

        preCheckJobPhase.execute(workflow.getJob(0));
        preCheckJobPhase.execute(workflow.getJob(1));

        Assertions.assertEquals(CheckStatus.DONE_OK, workflow.getJob(0).getPreCheckStatus());
        Assertions.assertEquals(CheckStatus.DONE_OK, workflow.getJob(1).getPreCheckStatus());
    }

    @Test void testDummyExecutor() throws Exception {
        final Workflow workflow = getWorkflow("good_dummy2.yml");
//        final List<String> expectedEvents = Arrays.asList("pipeline-run-start,job-run-start,job-phase-start,job-phase-finish,job-phase-start,job-phase-finish,job-phase-start,stage-run-start,stage-run-finish,stage-run-start,stage-run-finish,stage-run-start,stage-run-finish,job-phase-finish,job-phase-start,stage-postcheck-done,stage-postcheck-done,stage-postcheck-start,stage-postcheck-done,job-phase-finish,job-run-finish,pipeline-run-finish".split(","));

        final List<String> expectedEvents = Arrays.asList("pipeline-run-start,job-run-start,job-phase-start,job-phase-finish,job-phase-start,stage-precheck-start,stage-precheck-done,job-phase-finish,job-phase-start,stage-run-start,stage-run-finish,stage-run-start,stage-run-finish,stage-run-start,stage-run-finish,job-phase-finish,job-phase-start,stage-postcheck-done,stage-postcheck-done,stage-postcheck-start,stage-postcheck-done,job-phase-finish,job-run-finish,pipeline-run-finish".split(","));

        workflow.setExecutor(new DummyExecutor());

        final List<String> eventsCaught = new ArrayList<>();
        final Provider<Subscriber> provider = () -> event -> eventsCaught.add(event.getName());

        this.eventBus.subscribe(provider);

        engine.run(workflow);

        this.eventBus.unsubscribe();

        final Job job0 = workflow.getJob(0);

        Assertions.assertEquals(ExecutionStatus.DONE_OK, job0.getExecutionStatus());
        Assertions.assertEquals(CheckStatus.DONE_OK, job0.getPostCheckStatus());
        Assertions.assertEquals(CheckStatus.DONE_OK, job0.getPreCheckStatus());
        Assertions.assertEquals(expectedEvents, eventsCaught);
    }

    @Test void testCondition() throws Exception {
        final Workflow workflow = getWorkflow("good_2.yml");

        Assertions.assertEquals("true", workflow.getJob(0).getStages().get(0).getCondition());

        Assertions.assertTrue(conditionFlow.test(workflow.getJob(0).getStages().get(0)));
        Assertions.assertTrue(conditionFlow.test(workflow.getJob(0).getStages().get(2)));
        Assertions.assertFalse(conditionFlow.test(workflow.getJob(1).getStages().get(2)));
    }

    @Test void testRunJob() throws Exception {
        final Workflow workflow = getWorkflow("good.yml");

        Method method = Engine.class.getDeclaredMethod("run", Job.class);
        method.setAccessible(true);

        final Job job0 = workflow.getJob(0);

        method.invoke(engine, job0);

        Assertions.assertTrue(Files.exists(Paths.get("/tmp/djobi_test_engine")));

        Assertions.assertEquals(ExecutionStatus.DONE_OK, job0.getExecutionStatus());
        Assertions.assertEquals(CheckStatus.DONE_OK, job0.getPostCheckStatus());
        Assertions.assertEquals(CheckStatus.DONE_OK, job0.getPreCheckStatus());
    }

    @Test void runMonoPipeline() throws Exception {
        final Workflow workflow = getWorkflow("mono.yml");

        Assertions.assertEquals(1, workflow.getJobs().size());



        engine.run(workflow);

        Assertions.assertEquals(1, workflow.getJobs().size());
        Assertions.assertTrue(executionContext.getData() instanceof StageDataListString);
        Assertions.assertEquals("FRENCH", ( (StageDataListString) executionContext.getData()).getData().get(0) );
    }

    /*@Test void simulateLotOfJobs() throws Exception {
        final SimplePipeline engine = getEngine();
        final Pipeline pipeline = getPipeline("mono_sql.yml");

        Assertions.assertEquals(1, pipeline.getJobs().size());

        final Job model = pipeline.getJob(0);

        for (int i = 0; i < 1000; i++) {
            final Job job = new Job();

            job
                .setPipeline(model.getPipeline())
                .setStages(model.getStages())
                .setParameters(model.getParameters())
                .setName("clone_" + i)
            ;

            pipeline.getJobs().add(job);
        }

        Method method = SimplePipeline.class.getDeclaredMethod("run", Pipeline.class);
        method.setAccessible(true);

        method.invoke(engine, pipeline);

        Assertions.assertTrue(pipeline.getJob(0).getData() instanceof SerializedData);
        Assertions.assertEquals("FRENCH", ( (SerializedData) pipeline.getJob(0).getData()).toList().get(0) );
    }*/

    @Test void testCollectAsJSONPipeline() throws Exception {
        final Workflow workflow = getWorkflow("mono_sql.yml");

        Assertions.assertEquals(1, workflow.getJobs().size());

        engine.run(workflow);

        Assertions.assertEquals(1, workflow.getJobs().size());

        List<String> data = executionContext.getData().asListOfString();

        Assertions.assertEquals("{\"id\":1,\"title\":\"Tom\",\"label\":\"Tom\"}", data.get(0));
    }

    @Test void testCollectAsMapPipeline() throws Exception {
        final Workflow workflow = getWorkflow("collect_as_map.yml");

        engine.run(workflow);

        Assertions.assertEquals(1, workflow.getJobs().size());

        StageData<?> data = executionContext.getData();

        Assertions.assertEquals(BasicDataKind.TYPE_LIST, data.getKind().getType());

        List<?> dataAsList = (List<?>) data.getData();

        Assertions.assertEquals(MyMapUtils.map("id", Long.parseLong("1"), "title", "Tom"), dataAsList.get(0));
    }

    @Test void testRunPipeline() throws Exception {
        final Workflow workflow = getWorkflow("good.yml");

        engine.run(workflow);

        Assertions.assertTrue(Files.exists(Paths.get("/tmp/djobi_test_engine")));
    }

    private Workflow getWorkflow(final String workflow) throws Exception {
        return yamlPipelineLoader.get(
                ExecutionRequest.build( "./src/test/resources/pipelines/" + workflow)
                        .addArgument("date", "yesterday")
        );
    }

}
