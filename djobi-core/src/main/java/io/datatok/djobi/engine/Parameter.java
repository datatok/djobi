package io.datatok.djobi.engine;

import com.fasterxml.jackson.annotation.JsonValue;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Parameter {

    final public static String TYPE_INT = "int";
    final public static String TYPE_STRING = "string";
    final public static String TYPE_LIST = "list"; // List = string comma separated
    final public static String TYPE_DAILY_DATE = "daily_date";

    private String id;
    private String type = TYPE_STRING;
    private Object value;

    public Parameter() {

    }

    public Parameter(final String id, final Object value) {
        this
            .setId(id)
            .setValue(value)
        ;
    }

    public Parameter(Object value) {
        this.setValue(value);
    }

    public String getId() {
        return id;
    }

    public Parameter setId(String id) {
        this.id = id;

        return this;
    }

    public String getType() {

        if (type == null) {
            setType(TYPE_STRING);
        }

        return type;
    }

    public Parameter setType(String type) {
        this.type = type;

        return this;
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        return this.getValue().toString();
    }

    public List<Map<Object, Object>> getValuesWithIndex() {
        List<String> v = (List<String>) this.value;

        return
            IntStream
                .range(0, v.size())
                .mapToObj(i -> MyMapUtils.map(
            "value", v.get(i),
                    "loop_index", i + 1,
                    "loop_first", i == 0,
                    "loop_last", i == (v.size() - 1)
                )
        ).collect(Collectors.toList());
    }

    public String getValueDescription() {
        final Object v = this.getValue();

        if (v == null) {
            return "";
        }

        if (v instanceof String) {
            return ((String) v).length() > 16 ? ((String) v).substring(0, 16) : (String) v;
        }

        if (v instanceof Number) {
            return v.toString();
        }

        return v.getClass().toString();
    }

    public Parameter setValue(Object value) {
        this.value = value;

        return this;
    }

    @Override
    @JsonValue
    public String toString() {
        if (this.getValue() == null) {
            return null;
        }
        return this.getValue().toString();
    }

    public Parameter clone() {
        final Parameter p = new Parameter();

        p
            .setValue(this.getValue())
            .setId(this.getId())
            .setType(this.getType())
        ;

        return p;
    }
}
