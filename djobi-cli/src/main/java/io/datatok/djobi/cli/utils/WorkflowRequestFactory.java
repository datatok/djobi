package io.datatok.djobi.cli.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.engine.ExecutionRequest;
import io.datatok.djobi.plugins.report.VerbosityLevel;
import io.datatok.djobi.utils.EnvProvider;
import io.datatok.djobi.utils.MetaUtils;
import io.datatok.djobi.utils.MyMapUtils;

import java.util.Arrays;
import java.util.Map;

@Singleton
public class WorkflowRequestFactory {

    static public final String ENV_PREFIX_META = "META";

    static public final String ENV_PREFIX_ARG = "ARG";

    @Inject
    MetaUtils metaUtils;

    @Inject
    EnvProvider envProvider;

    /**
     * For dev, test purposes
     */
    private ExecutionRequest lastObjectBuilt;

    public ExecutionRequest build(final String url) {
        return lastObjectBuilt =  new ExecutionRequest(url);
    }

    public ExecutionRequest build(
            final String definitionURL,
            final Map<String, String> inArgumentMap,
            final Map<String, String> inMetaMap,
            final String inJobsFilter,
            final String inPhasesFilter,
            final String verbosity
    ) {
        final ExecutionRequest pipelineRequest = new ExecutionRequest();

        final Map<String, String> argsMap = MyMapUtils.merge(envProvider.getScopedStartsWith(ENV_PREFIX_ARG), inArgumentMap);
        final Map<String, String> metasMap =
                metaUtils.clean(
                    MyMapUtils.merge(envProvider.getScopedStartsWith(ENV_PREFIX_META), inMetaMap)
                );

        pipelineRequest
            .setDefinitionURI(definitionURL)
            .setArguments(argsMap)
            .setJobsFilter(Arrays.asList(inJobsFilter.split(",")))
            .setJobPhases(Arrays.asList(inPhasesFilter.split(",")))
            .setMetaDataLabels(metasMap)
            .setVerbosity(getVerbosity(verbosity))
        ;

        return lastObjectBuilt = pipelineRequest;
    }

    /**
     * For dev, test purposes
     * @return PipelineExecutionRequest
     */
    public ExecutionRequest getLastObjectBuilt() {
        return lastObjectBuilt;
    }

    private VerbosityLevel getVerbosity(String verbosity) {
        if (verbosity == null || verbosity.isEmpty()) {
            return VerbosityLevel.NORMAL;
        }

        return switch (verbosity) {
            case "quiet" -> VerbosityLevel.QUIET;
            case "verbose" -> VerbosityLevel.VERBOSE;
            case "alicia" -> VerbosityLevel.ALICIA;
            default -> VerbosityLevel.NORMAL;
        };
    }

}
