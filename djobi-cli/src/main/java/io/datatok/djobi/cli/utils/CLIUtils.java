package io.datatok.djobi.cli.utils;

import org.apache.commons.lang.StringUtils;

import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

public class CLIUtils {

    public static void output(final String format, Object... args) {
        output(new Formatter().format(format, args).toString());
    }

    public static void output(final String out) {
        System.out.println( ansi().render(out));
    }

    public static void printError(final String out) {
        System.out.println( ansi().render(String.format("\n\n@|bold,white,bg_red Fatal error: %s|@\n", out)));
    }

    public static void outputHorizontalList(final List<String> texts) {
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

        CLIUtils.output(buffer.toString());
    }

    /**
     * @deprecated v2.2.0
     */
    public static Map<String, String> parseParameters(final String[] args) {

        final Map<String, String> parameters = new HashMap<>();

        for (final String arg : args) {
            if (!arg.startsWith("--")) {
                System.err.println("Bad parameter: " + arg);
                System.exit(2);
            }

            final String[] tab = arg.substring(2).split("=");
            parameters.put(tab[0], tab.length == 2 ? tab[1] : "");
        }

        return parameters;
    }

}
