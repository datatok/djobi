package io.datatok.djobi.cli.utils;

import java.util.List;

public interface CLIUtils {

    void init();

    void output(final String format, Object... args);

    void output(final String out);

    void printError(final String out);

    void outputHorizontalList(final List<String> texts);
}
