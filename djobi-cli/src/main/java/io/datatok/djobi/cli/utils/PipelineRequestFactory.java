package io.datatok.djobi.cli.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.datatok.djobi.engine.PipelineExecutionRequest;
import io.datatok.djobi.plugins.report.VerbosityLevel;
import io.datatok.djobi.utils.EnvProvider;
import io.datatok.djobi.utils.MetaUtils;
import io.datatok.djobi.utils.MyMapUtils;

import java.nio.channels.Pipe;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class PipelineRequestFactory {

    static public final String ENV_PREFIX_META = "META";

    static public final String ENV_PREFIX_ARG = "ARG";

    @Inject
    MetaUtils metaUtils;

    @Inject
    EnvProvider envProvider;

    /**
     * For dev, test purposes
     */
    private PipelineExecutionRequest lastObjectBuilt;

    public PipelineExecutionRequest build(final String pipelineDefinitionPath) {
        return lastObjectBuilt =  new PipelineExecutionRequest(pipelineDefinitionPath);
    }

    public PipelineExecutionRequest build(
            final String inPipelinePath,
            final Map<String, String> inArgumentMap,
            final Map<String, String> inMetaMap,
            final String inJobsFilter,
            final String inPhasesFilter,
            final boolean[] verbosity
    ) {
        final PipelineExecutionRequest pipelineRequest = new PipelineExecutionRequest();

        final Map<String, String> argsMap = MyMapUtils.merge(envProvider.getScopedStartsWith(ENV_PREFIX_ARG), inArgumentMap);
        final Map<String, String> metasMap =
                metaUtils.clean(
                    MyMapUtils.merge(envProvider.getScopedStartsWith(ENV_PREFIX_META), inMetaMap)
                );

        pipelineRequest
            .setPipelineDefinitionPath(inPipelinePath)
            .setRaw(argsMap)
            .setJobsFilter(Arrays.asList(inJobsFilter.split(",")))
            .setJobPhases(Arrays.asList(inPhasesFilter.split(",")))
            .setMeta(metasMap)
            .setVerbosity(getVerbosity(verbosity))
        ;

        return lastObjectBuilt = pipelineRequest;
    }

    /**
     * For dev, test purposes
     * @return PipelineExecutionRequest
     */
    public PipelineExecutionRequest getLastObjectBuilt() {
        return lastObjectBuilt;
    }

    private VerbosityLevel getVerbosity(boolean[] verbosity) {
        int l = verbosity.length;

        if (l < 1) {
            return VerbosityLevel.NORMAL;
        }

        if (l < 2) {
            return VerbosityLevel.VERBOSE;
        }

        if (l < 3) {
            return VerbosityLevel.VERY_VERBOSE;
        }

        if (l < 4) {
            return VerbosityLevel.VERY_VERY_VERBOSE;
        }

        return VerbosityLevel.ALICIA;
    }

}
