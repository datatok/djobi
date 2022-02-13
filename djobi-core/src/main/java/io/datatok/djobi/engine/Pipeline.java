package io.datatok.djobi.engine;

import io.datatok.djobi.executors.Executor;
import io.datatok.djobi.utils.Dumpable;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.interfaces.Labelized;
import io.datatok.djobi.utils.io.IOFileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Pipeline implements Dumpable, Labelized {

    private String uid;

    private PipelineExecutionRequest pipelineRequest;

    private List<Job> jobs;

    private Map<String, Job> jobsById;

    private File resourcesDir;

    private Executor executor;

    /**
     * @since v5.0.0
     */
    public Map<String, String> labels;

    public Pipeline setPipelineRequest(PipelineExecutionRequest pipelineRequest) {
        this.pipelineRequest = pipelineRequest;

        return this;
    }

    public PipelineExecutionRequest getPipelineRequest() {
        return pipelineRequest;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Job getJob(int index) {
        return jobs.get(index);
    }

    public Map<String, Job> getJobsById() {
        return jobsById;
    }

    public Pipeline setJobs(List<Job> jobs) {
        this.jobs = jobs;

        jobsById = new HashMap<>();

        for (Job job : jobs) {
            jobsById.put(job.getId(), job);
        }

        return this;
    }

    public File getResourcesDir() {
        return resourcesDir;
    }

    public Pipeline setResourcesDir(File resourcesDir) {
        this.resourcesDir = resourcesDir;

        return this;
    }

    /**
     * Load yaml relative to a pipeline.
     *
     * @param name
     * @return
     * @throws IOException
     */
    public String getResources(final String name) throws IOException
    {
        final String pathRelative = resolvePath(name);

        return IOFileUtils.getContent(pathRelative);
    }

    public String resolvePath(final String path) {
        // Don't mess with absolute path
        if (path.startsWith("/")) {
            return path;
        }

        return this.getResourcesDir().getAbsolutePath().concat("/").concat(path);
    }

    public String getName() {
        final String path = getPipelineRequest().getDefinitionURI();

        return StringUtils.strip(Arrays.stream(path.split("/")).reduce("", (a, b) -> {
            if (b.equals("pipelines")) {
                return "/";
            } else if (!a.isEmpty()) {
                return a.concat("/").concat(b);
            } else {
                return "";
            }
        }).trim(), "/");
    }

    public Executor getExecutor() {
        return executor;
    }

    public Pipeline setExecutor(Executor executor) {
        this.executor = executor;

        return this;
    }

    public String getUid() {
        return uid;
    }

    public Pipeline setUid(String uid) {
        this.uid = uid;

        return this;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getLabel(String key)
    {
        return labels.get(key);
    }

    public Pipeline setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    public Map<String, Object> toLog() {
        return MyMapUtils.map(
            "uid", uid,
            "name", getName(),
            "job_count", jobs.size(),
            "meta", MyMapUtils.map(
            "team", "?"
            )
        );
    }

    public Map<String, Object> toHash() {
        return MyMapUtils.map(
            "uid", uid,
                "name", getName(),
                "job_count", jobs.size(),
                "executor", executor.toHash(),
                "jobs", getJobs().stream().map(Job::toHash).collect(Collectors.toList())
        );
    }
}
