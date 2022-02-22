package io.datatok.djobi.engine.actions.transformer.serializers;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.data.StageData;
import io.datatok.djobi.engine.data.StageDataString;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.engine.stage.livecycle.ActionRunResult;
import io.datatok.djobi.engine.stage.livecycle.ActionRunner;
import io.datatok.djobi.exceptions.NotFoundException;
import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.templating.TemplateUtils;

import javax.inject.Inject;

public class TemplateRenderActionRunner implements ActionRunner {

    final static public String TYPE = "transform.mustache";

    @Inject
    protected TemplateUtils templateUtils;

    @Override
    public ActionRunResult run(final Stage stage, StageData<?> contextData, Executor contextExecutor) throws Exception {
        final Job job = stage.getJob();
        final String content;
        String templateName = stage.getSpec().getString("template");

        if (templateName == null) {
            throw new NotFoundException(stage, "template is null!");
        }

        templateName = templateUtils.render(templateName, job);

        content = templateUtils.render(job.getPipeline().getResources(templateName), job);

        return ActionRunResult.success(new StageDataString(content));
    }
}
