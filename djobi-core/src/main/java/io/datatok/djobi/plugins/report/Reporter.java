package io.datatok.djobi.plugins.report;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Pipeline;

import java.io.PrintStream;

public interface Reporter {

    void output(final String format, Object... args);

    String success(String text);

    String error(String text);

    void printSummary(final Pipeline pipeline);

    void printSummary(final Job job);

    void printSummary(final Job job, boolean displayParameters);

    PrintStream getPrintStream();

    void setPrintStream(PrintStream printStream);

}
