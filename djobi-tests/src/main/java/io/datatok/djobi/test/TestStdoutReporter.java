package io.datatok.djobi.test;

import io.datatok.djobi.engine.Job;
import io.datatok.djobi.engine.Workflow;
import io.datatok.djobi.plugins.report.Reporter;

import javax.inject.Singleton;
import java.io.PrintStream;

@Singleton
public class TestStdoutReporter implements Reporter {

    private PrintStream printStream;

    @Override
    public void output(String format, Object... args) {
        System.out.println(format);

        if (printStream != null) {
            printStream.println(format);
        }
    }

    @Override
    public String success(String text) {
        return null;
    }

    @Override
    public String error(String text) {
        return null;
    }

    @Override
    public void error(Throwable exception) {

    }

    @Override
    public void printSummary(Workflow workflow) {

    }

    @Override
    public void printSummary(Job job) {
        printSummary(job, false);
    }

    @Override
    public void printSummary(Job job, boolean displayParameters) {

    }

    @Override
    public PrintStream getPrintStream() {
        return printStream;
    }

    @Override
    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }
}
