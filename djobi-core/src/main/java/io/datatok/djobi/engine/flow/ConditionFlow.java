package io.datatok.djobi.engine.flow;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.stage.Stage;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ConditionFlow {

    static private Logger logger = Logger.getLogger(ConditionFlow.class);

    @Inject
    private TemplateUtils templateUtils;

    public boolean test(final Stage stage) {
        final String condition = stage.getCondition();

        logger.debug("Test condition " + condition);

        if (condition.equals("true") || condition.equals("yes")) {
            return true;
        }

        final String[] items = condition.split("\\s+");

        final String operator = items[0];
        final String a = resolveValue(stage.getJob(), items[1]);
        final String b = resolveValue(stage.getJob(), items[2]);

        if (operator.equals("eq")) {
            return testEquals(a, b);
        } else if (operator.equals("neq")) {
            return !testEquals(a, b);
        }

        return false;
    }

    private String resolveValue(final Job job, final String holder) {
        if (holder.startsWith("$")) {
            return (String) templateUtils.getAsVariable(job, holder);
        }

        return holder;
    }

    private boolean testEquals(String a, String b) {
        return a.equals(b);
    }

}
