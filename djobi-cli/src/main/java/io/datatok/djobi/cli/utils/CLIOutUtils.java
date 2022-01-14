package io.datatok.djobi.cli.utils;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.utils.bags.ParameterBag;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class CLIOutUtils {

    static public String str(final int width, final String text) {
        if (text.length() > width - 3) {
            return text.substring(0, width - 3) + "..";
        } else {
            return text;
        }
    }

    static public String table(ParameterBag parameters) {
        final List<List<String>> tableArgs = new ArrayList<>();
        final List blacklist = Arrays.asList("month0", "day_before", "day0", "month", "day", "year");

        for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            if (!blacklist.contains(entry.getKey())) {
                tableArgs.add(new ArrayList<String>() {{
                    add(entry.getKey());
                    add(entry.getValue().getValueAsString());
                }});
            }
        }

        return table(tableArgs);
    }

    static public String table(final List<List<String>> rows) {
        return table(rows, false);
    }

    static public String table(final List<List<String>> rows, boolean firstRowAsHeader) {
        final StringBuilder buffer = new StringBuilder();
        final Map<Integer, Integer> columns = new HashMap<>();

        for (List<String> cells : rows) {
            int i = 0;
            for (String cell : cells) {
                if (cell != null && (columns.getOrDefault(i, 0)) < Math.min(100, cell.length())) {
                    columns.put(i, Math.min(100, cell.length()));
                }

                i++;
            }
        }

        buffer.append("+");

        for (Integer w : columns.values()) {
            buffer.append(StringUtils.leftPad("", w + 6, "-"));
            buffer.append("+");
        }

        buffer.append("\n");

        for (List<String> cells : rows) {
            buffer.append("| ");
            int i = 0;
            for (String cell : cells) {
                buffer.append(cellWithoutBorder(columns.get(i) + 4, cell));
                buffer.append(" | ");

                i++;
            }

            buffer.append("\n");

            if (firstRowAsHeader) {
                buffer.append("+");

                for (Integer w : columns.values()) {
                    buffer.append(StringUtils.leftPad("", w + 6, "-"));
                    buffer.append("+");
                }

                buffer.append("\n");

                firstRowAsHeader = false;
            }
        }

        buffer.append("+");

        for (Integer w : columns.values()) {
            buffer.append(StringUtils.leftPad("", w + 6, "-"));
            buffer.append("+");
        }

        return buffer.toString();
    }

    static public String cellWithoutBorder(final int width, final String text) {
        return StringUtils.rightPad(str(width, text), width);
    }

    static public String cellCenterWithoutBorder(final int width, final String text) {
        return StringUtils.center(str(width, text), width);
    }

    static public String spaces(final int width) {
        return StringUtils.rightPad("", width, "");
    }

    static public String cell(final int width, final String text) {
        return StringUtils.center(" " + StringUtils.rightPad(str(width, text), width - 3), width, "|");
    }

    static public String cellCenter(final int width, final String text) {
        return StringUtils.center(StringUtils.center(str(width, text), width - 2), width, "|");
    }

    static public String cellCenter(final String text) {
        return cellCenter(text.length() + 4, text);
    }

    static public String cellBorder(final String text) {
        StringBuilder buffer = new StringBuilder();
        int width = text.length() + 4;

        buffer.append(CLIOutUtils.border(width - 1));
        buffer.append("\n");
        buffer.append(CLIOutUtils.cellCenter(width, text));
        buffer.append("\n");
        buffer.append(CLIOutUtils.border(width - 1));

        return buffer.toString();
    }


    static public String border(final int width) {
        return StringUtils.rightPad("+", width, "-") + "+";
    }

    static public void printBorder(final int width) {
        System.out.println(border(width));
    }

    static public String success(String text) {
        return big("green", text);
    }

    static public String error(String text) {
        return big("red", text);
    }

    static public String big(String color, String text) {
        int s = text.length() + 20;
        return String.format("\n\n@|bold,white,bg_%s %s |@\n@|bold,white,bg_%s %s |@\n@|bold,white,bg_%s %s |@\n", color, StringUtils.repeat(" ", s), color, StringUtils.center(text, s), color, StringUtils.repeat(" ", s));
    }

}
