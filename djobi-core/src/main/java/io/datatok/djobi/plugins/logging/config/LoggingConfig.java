package io.datatok.djobi.plugins.logging.config;

public class LoggingConfig {

    private LoggingSinkConfig jobSink;

    private LoggingSinkConfig stageSink;

    private LoggingSinkConfig metricSink;

    public LoggingSinkConfig getSinkByType(String type) {
        if (type.equals("job") || type.equals("jobs")) {
            return getJobSink();
        }
        else if (type.equals("stage") || type.equals("stages")) {
            return getStageSink();
        }

        return getMetricSink();
    }

    public LoggingSinkConfig getJobSink() {
        return jobSink;
    }

    public LoggingConfig setJobSink(LoggingSinkConfig jobSink) {
        this.jobSink = jobSink;
        return this;
    }

    public LoggingSinkConfig getStageSink() {
        return stageSink;
    }

    public LoggingConfig setStageSink(LoggingSinkConfig stageSink) {
        this.stageSink = stageSink;
        return this;
    }

    public LoggingSinkConfig getMetricSink() {
        return metricSink;
    }

    public LoggingConfig setMetricSink(LoggingSinkConfig metricSink) {
        this.metricSink = metricSink;
        return this;
    }
}
