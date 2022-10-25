package io.datatok.djobi.loaders.matrix;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.utils.MyMapUtils;
import io.datatok.djobi.utils.bags.ParameterBag;
import io.datatok.djobi.utils.templating.TemplateUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generate new jobs based on definition and parameters.
 */
@Singleton
public class MatrixGenerator {

    @Inject
    private TemplateUtils templateUtils;

    @Inject
    private DateMatrixGenerator dateGenerator;

    public List<ParameterBag> generate(final ParameterBag parameters) throws Exception {
        return generate(parameters, new HashMap<>());
    }

    /**
     * Transform parameters and matrix variants into job parameters.
     *
     * @param parameters Workflow and Job parameters
     * @param matrixVariants Job matrix variants
     * @return Merge of parameters and matrix variants
     * @exception Exception If something bad happen
     */
    public List<ParameterBag> generate(final ParameterBag parameters, final Map<String, ParameterBag> matrixVariants) throws Exception {
        final List<ParameterBag> runsFromParameters = new ArrayList<>();
        final List<Parameter> jobParameters = new ArrayList<>();

        // Expand parameters
        if (parameters != null && parameters.size() > 0) {
            for (Parameter parameter : parameters.values()) {
                switch (parameter.getType()) {
                    case Parameter.TYPE_DAILY_DATE:
                        runsFromParameters.addAll(dateGenerator.generate(parameter));
                    break;
                    default:
                        jobParameters.add(parameter);
                }
            }
        }

        // If no expandable parameters -> empty
        if (runsFromParameters.size() == 0) {
            runsFromParameters.addAll(new ArrayList<ParameterBag>(){{
                add(new ParameterBag());
            }});
        }

        // Merge other parameters
        runsFromParameters.forEach(run ->
            jobParameters.forEach(p -> run.put(p.getId(), p))
        );

        if (matrixVariants != null && matrixVariants.size() > 0) {
            final ArrayList<ParameterBag> ret = new ArrayList<>();

            matrixVariants.forEach( (contextKey, context) -> {
                runsFromParameters.forEach(run -> {
                    final ParameterBag runClone = ParameterBag.merge(run);

                    runClone.putAll(context);
                    runClone.put("_context_", new Parameter("context", contextKey));
                    runClone.putAll(postGenerationTransform(runClone));

                    ret.add(runClone);
                });
            });

            return ret;
        } else {
            runsFromParameters.forEach(run -> run.put("_context_", new Parameter("context", "default")));

            return runsFromParameters.stream().map(this::postGenerationTransform).collect(Collectors.toList());
        }
    }

    /**
     * After generation, apply templating.
     *
     * @param parameters Job parameters
     * @return job parameters templatized
     */
    private ParameterBag postGenerationTransform(final ParameterBag parameters) {
        Map<String, Parameter> buffer = parameters.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    final Parameter p = e.getValue();

                    if (p.getType().equals(Parameter.TYPE_STRING) &&
                            p.getValue() != null &&
                            p.getValueAsString().contains("{")) {
                        final Parameter pc = p.clone();

                        pc.setValue(templateUtils.renderTemplate((String) p.getValue(), MyMapUtils.map("args", parameters)));

                        return pc;
                    }

                    return p;
                }
            ));

        return ParameterBag.fromMap(buffer);
    }

    /**
     * Generate job run unique ID.
     * @param parameters List of job parameters
     * @return The job UUID
     */
    static public String toUUID(Collection<Parameter> parameters) {

        final boolean hasDate = parameters.stream().anyMatch(p -> p.getId() != null && p.getId().equals("date"));

        return
            StringUtils.strip(
                parameters.stream()
                    .filter(p -> p.getId() != null)
                    .filter(p -> !hasDate || !Arrays.asList("year", "month", "day", "month0", "day0", "day_before").contains(p.getId()))
                    .filter(p -> !p.getId().equals("context") && !p.getId().startsWith("_"))
                    .reduce(
                            "",
                            (ret, p) -> ret + String.format("&%s=%s", p.getId(), p.getValueDescription()),
                            (p1, p2) -> p1 + p2
                    )
                , "&"
            );
    }

}
