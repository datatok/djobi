package io.datatok.djobi.utils;

import io.datatok.djobi.engine.PipelineExecutionRequest;

import java.util.regex.Pattern;

public class PipelineUtils {

    static public boolean acceptJob(final PipelineExecutionRequest pipelineRequest, final String job) {
        if (pipelineRequest.getJobsFilter() == null || pipelineRequest.getJobsFilter().size() == 0) {
            return true;
        }

        for (final String filter : pipelineRequest.getJobsFilter()) {
            if (filter.equals(job)) {
                return true;
            }

            final Pattern pattern = Pattern.compile(filter);

            if (pattern.matcher(job).find()) {
                return true;
            }
        }

        return false;
    }
}
