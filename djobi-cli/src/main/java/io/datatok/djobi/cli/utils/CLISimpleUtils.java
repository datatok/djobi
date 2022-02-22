package io.datatok.djobi.cli.utils;

import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.Formatter;
import java.util.List;

@Singleton
public class CLISimpleUtils implements CLIUtils {

    public void init() {

    }

    public  void output(final String format, Object... args) {
        output(new Formatter().format(format, args).toString());
    }

    public  void output(final String out) {
        System.out.println(out);
    }

    public  void printError(final String out) {
        System.out.printf("\n\nFatal error: %s\n%n", out);
    }

    public  void outputHorizontalList(final List<String> texts) {
        StringBuilder buffer = new StringBuilder();

        for (final String text : texts) {
            buffer.append(String.format("+%s+     ", StringUtils.leftPad("", text.length() + 2, "-")));
        }

        buffer.append("\n");

        for (final String text : texts) {
            buffer.append(String.format("| %s | --> ", text));
        }

        buffer
            .delete(buffer.length() - 4, buffer.length())
            .append("\n");

        for (final String text : texts) {
            buffer.append(String.format("+%s+     ", StringUtils.leftPad("", text.length() + 2, "-")));
        }

        output(buffer.toString());
    }
}
