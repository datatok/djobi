package io.datatok.djobi.engine;

import io.datatok.djobi.engine.parameters.DateParameter;
import io.datatok.djobi.loaders.yaml.YAMLPipelineLoader;
import io.datatok.djobi.test.MyTestRunner;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.bags.ParameterBag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

@ExtendWith(MyTestRunner.class)
class TemplateUtilsTest {

    @Inject
    private TemplateUtils templateUtils;

    @Inject
    private YAMLPipelineLoader yamlPipelineLoader;

    @Test void stringTest() {
        Assertions.assertEquals("Hello, env is test", templateUtils.renderTemplate("Hello, env is {{env._meta_.config}}"));
    }

    @Test void testWithJobData() {
        final Job job = new Job();

        job.setParameters(new ParameterBag("hello", "world"));

        Assertions.assertEquals("Hello world", templateUtils.render("Hello {{hello}}", job));
    }

    @Test void testWithMap() {
        final Job job = new Job();

        job.setParameters(new ParameterBag("hello", MyMapUtils.map("toto", "world"), "date", new DateParameter("date", Calendar.getInstance())));

        Assertions.assertEquals("Hello world", templateUtils.render("Hello {{hello.value.toto}}", job));
        Assertions.assertEquals("today is the " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH), templateUtils.render("today is the {{date.day}}", job));
        Assertions.assertEquals("today is the " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH), templateUtils.render("today is the {{date.dayBefore.dayAfter.day}}", job));
    }

    @Test void testWithList() {
        final Job job = new Job();

        job.setParameters(
                new ParameterBag(
                "hello", Arrays.asList("hello", "world"),
                    "date", new DateParameter("date", Calendar.getInstance())
            )
        );

        Assertions.assertEquals("hello world ", templateUtils.render("{% for h in parameters.hello.value %}{{h}} {% endfor %}", job));

        Assertions.assertEquals("1 2 ", templateUtils.render("{% for h in parameters.hello.value %}{{loop.index}} {% endfor %}", job));
    }

    @Test void testWithPipelineData() throws IOException {
        final Pipeline pipeline = yamlPipelineLoader.get(
                PipelineExecutionRequest.build("./src/test/resources/pipelines/good.yml")
                    .addArgument("date", "yesterday")
                );

        final Calendar yesterday = Calendar.getInstance();

        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        Assertions.assertEquals(String.format("Hello %d-%02d-%02d", yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH) + 1, yesterday.get(Calendar.DAY_OF_MONTH)), templateUtils.render("Hello {{date}}", pipeline.getJob(0)));

        Assertions.assertEquals("p1 = p1", templateUtils.render("p1 = {{p1}}", pipeline.getJob(0)));
    }
}
