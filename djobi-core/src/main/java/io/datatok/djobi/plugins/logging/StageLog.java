package io.datatok.djobi.plugins.logging;

public class StageLog {

    private String rawLogs;

    private Exception exception;

    public String getRawLogs() {
        return rawLogs;
    }

    public StageLog setRawLogs(String rawLogs) {
        this.rawLogs = rawLogs;

        return this;
    }

    public Exception getException() {
        return exception;
    }

    public StageLog setException(Exception exception) {
        this.exception = exception;

        return this;
    }
}
