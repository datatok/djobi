package io.datatok.djobi.utils.bags;

import io.datatok.djobi.engine.Parameter;

import java.util.HashMap;
import java.util.Map;

public class ParameterBag  extends HashMap<String, Parameter>
{
    static public ParameterBag fromMap(Map<String, Parameter> source) {
        ParameterBag bag = new ParameterBag();

        bag.putAll(source);

        return bag;
    }

    public ParameterBag() {

    }

    public ParameterBag(Object... args) {
        super();
        String key = null;
        for (Object arg : args) {
            if (key == null) {
                key = (String) arg;
            } else {
                put(key, arg instanceof Parameter ? (Parameter) arg : new Parameter(key, arg));
                key = null;
            }
        }
    }

    public ParameterBag add(Parameter... args) {
        for (Parameter arg : args) {
            put(arg.getId(), arg);
        }

        return this;
    }

    public ParameterBag add(String key, Object value) {
        return add(new Parameter(key, value));
    }

    static public ParameterBag build(Parameter... args) {
        final ParameterBag instance = new ParameterBag();

        return instance.add(args);
    }

    static public ParameterBag merge(ParameterBag... args) {
        final ParameterBag buffer = new ParameterBag();

        for (Map m : args) {
            buffer.putAll(m);
        }

        return buffer;
    }
}