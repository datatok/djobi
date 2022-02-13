package io.datatok.djobi.utils;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.utils.bags.ParameterBag;
import org.apache.log4j.Logger;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;

@Singleton
public class ActionArgFactory {

    private static final Logger logger = Logger.getLogger(ActionArgFactory.class);

    public ParameterBag resolve(final Map<String, Object> definitionBag, final PipelineExecutionRequest pipelineRequest) {
        final Map<String, String> jobRequestArgs = pipelineRequest.getArguments();
        final ParameterBag bag = new ParameterBag();

        if (definitionBag == null) {
            return bag;
        }

        definitionBag.forEach((parameterId, definition) -> {
            bag.put(parameterId, get(parameterId, definition, jobRequestArgs));
        });

        return bag;
    }

    /**
     * Factory method.
     *
     * @param parameterId String
     * @param definition Object
     * @param jobRequestArgs  Map<String, String>
     * @return Parameter
     */
    public Parameter get(final String parameterId, final Object definition, final Map<String, String> jobRequestArgs) {
        final Parameter parameter;
        String rawValue = null;

        if (definition instanceof String) {
            rawValue = (String) definition;
            parameter = new Parameter(parameterId, definition);
        } else if (definition instanceof Map) {
            final Map<String, String> argDefinition = (Map<String, String>) definition;

            parameter = new Parameter();

            parameter.setId(parameterId);

            if (argDefinition.containsKey("type")) {
                parameter.setType(argDefinition.get("type"));
            }

            if (argDefinition.containsKey("value")) {
                Object bufferValue = argDefinition.get("value");
                rawValue = bufferValue.toString();
            }
        } else {
            logger.warn(String.format("argument %s default value should be a string!", parameterId));
            rawValue = definition.toString();
            parameter = new Parameter(parameterId, definition);
        }

        if (jobRequestArgs.containsKey(parameterId)) {
            rawValue = jobRequestArgs.get(parameterId);
            logger.debug(String.format("argument found %s = %s", parameterId, jobRequestArgs.get(parameterId)));
        } else if (rawValue != null) {
            logger.debug(String.format("argument %s not provided, default to %s", parameterId, rawValue));
        } else {
            logger.error(String.format("argument %s not provided and no default", parameterId));
        }

        switch (parameter.getType()) {
            case Parameter.TYPE_LIST:
                if (rawValue != null) {
                    parameter.setValue(Arrays.asList(rawValue.split(",")));
                }
                break;
            case Parameter.TYPE_INT:
                if (rawValue != null) {
                    parameter.setValue(Integer.parseInt(rawValue));
                }
                break;
            default:
                parameter.setValue(rawValue);
        }

        return parameter;
    }
}
