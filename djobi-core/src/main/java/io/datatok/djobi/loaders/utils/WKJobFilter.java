package io.datatok.djobi.loaders.utils;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.ExecutionRequest;

import java.util.regex.Pattern;

public class WKJobFilter {

    static public boolean accept(final ExecutionRequest pipelineRequest, final Job job) {
        if (pipelineRequest.getJobsFilter() == null || pipelineRequest.getJobsFilter().size() == 0) {
            return true;
        }

        final String jobID = job.getId();

        for (final String filter : pipelineRequest.getJobsFilter()) {
            if (filter.equals(jobID)) {
                return true;
            }

            final Pattern pattern = Pattern.compile(filter);

            if (pattern.matcher(jobID).find()) {
                return true;
            }
        }

        return false;
    }
}
