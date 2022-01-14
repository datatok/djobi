package io.datatok.djobi.test;

import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.engine.Pipeline;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.executors.ExecutorPool;
import io.datatok.djobi.loaders.yaml.YAMLPipelineLoader;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MyTestRunner.class)
public class ActionTest {
    @Inject
    protected Engine engine;

    @Inject
    protected YAMLPipelineLoader yamlPipelineLoader;

    @Inject
    protected ExecutorPool executorPool;

    @Inject
    protected TemplateUtils templateUtils;

    @Inject
    protected StageTestUtils stageTestUtils;

    final public static class TestDec {
        public Double d;
        public Double d2;
        public Integer i;
        public String s;

        public TestDec(Double d, Integer i, String s) {
            this.d = d;
            this.d2 = d;
            this.i = i;
            this.s = s;
        }

        public Double getD() {
            return d;
        }

        public Integer getI() {
            return i;
        }

        public String getS() {
            return s;
        }

        public void setD(Double d) {
            this.d = d;
        }

        public void setI(Integer i) {
            this.i = i;
        }

        public void setS(String s) {
            this.s = s;
        }

        public Double getD2() {
            return d2;
        }

        public void setD2(Double d2) {
            this.d2 = d2;
        }
    }

    protected Pipeline getPipeline(final String pipeline) throws Exception {
        return yamlPipelineLoader.get(
                PipelineExecutionRequest.build( "./src/test/resources/pipelines/" + pipeline)
                        .addArgument("date", "yesterday")
        );
    }

    protected Dataset<Row> buildDataset(int count) throws IOException {
        final RandomDataGenerator generator = new RandomDataGenerator();
        final List<TestDec> beans = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final TestDec bean = new TestDec(
                    generator.nextBeta(10, 10),
                    generator.nextInt(10, 100),
                    generator.nextHexString(16)
            );

            beans.add(bean);
        }

        return getSparkExecutor().getSQLContext().createDataFrame(beans, TestDec.class);
    }

    protected SparkExecutor getSparkExecutor() {
        return (SparkExecutor) executorPool.get(SparkExecutor.TYPE);
    }
}
