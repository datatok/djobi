package io.datatok.djobi.engine.stage;

import com.google.inject.Provider;
import io.datatok.djobi.engine.actions.csv.CSVType;
import io.datatok.djobi.engine.actions.fs.input.FSInputType;
import io.datatok.djobi.engine.actions.fs.output.FSOutputType;
import io.datatok.djobi.engine.actions.json.JSONType;
import io.datatok.djobi.engine.actions.sql.SQLType;
import io.datatok.djobi.engine.actions.utils.generator.RandomGeneratorType;
import io.datatok.djobi.engine.actions.utils.stdout.StdoutType;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.phases.ActionPhases;
import io.datatok.djobi.engine.stage.livecycle.*;
import io.datatok.djobi.engine.utils.StageCandidate;
import io.datatok.djobi.utils.MyMapUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ActionFactory {

    @Inject
    private Map<String, Provider<ActionConfigurator>> configurators;

    @Inject
    private Map<String, Provider<ActionPreChecker>> preCheckers;

    @Inject
    private Map<String, Provider<ActionRunner>> runners;

    @Inject
    private Map<String, Provider<ActionPostChecker>> postCheckers;

    @Inject
    private Map<String, ActionProvider> providers;

    @Inject
    private ExecutionContext executionContext;

    private static final Logger logger = Logger.getLogger(ActionFactory.class);

    static private final List<String> genericTypes = Arrays.asList(
        SQLType.NAME,
        RandomGeneratorType.NAME,
        FSInputType.TYPE,
        FSOutputType.TYPE,
        StdoutType.NAME,
        CSVType.NAME,
        JSONType.NAME
    );

    public Map<String, Map<String, Provider<? extends Action>>> getAll() {
        return MyMapUtils.map(
            ActionPhases.CONFIGURE, configurators,
            ActionPhases.PRE_CHECK, preCheckers,
            ActionPhases.POST_CHECK, postCheckers,
            ActionPhases.RUN, runners
        );
    }

    public ActionConfigurator getConfigurator(final Stage stage) {
        return getObjectFromMap(ActionPhases.CONFIGURE, configurators, stage);
    }

    public ActionPreChecker getPreChecker(final Stage stage) {
        return getObjectFromMap(ActionPhases.PRE_CHECK, preCheckers, stage);
    }

    public ActionPostChecker getPostChecker(final Stage stage) {
        return getObjectFromMap(ActionPhases.POST_CHECK, postCheckers, stage);
    }

    public ActionRunner getRunner(final Stage stage) {
        return getObjectFromMap(ActionPhases.RUN, runners, stage);
    }

    /**
     * Get action implementation or null if not exist.
     *
     * @param phase string
     * @param map Map<String, Provider<V>>
     * @param stage Stage
     * @param <V>
     * @return
     */
    private <V> V getObjectFromMap(final String phase, final Map<String, Provider<V>> map, final Stage stage) {

        // Check if this is a generic stage type
        if (genericTypes.contains(stage.getKind())) {
            List<StageCandidate> candidates = getCandidates(providers, stage, phase);

            if (candidates != null && !candidates.isEmpty()) {
                StageCandidate bestCandidate = candidates.get(0);

                logger.debug(String.format("[phase:%s] [action:%s] Found candidate (score %d)", phase, bestCandidate.getFullNameQualified(), bestCandidate.getScore()));

                if (map.containsKey(bestCandidate.getFullNameQualified())) {
                    return map.get(bestCandidate.getFullNameQualified()).get();
                } else {
                    logger.warn(String.format("[phase:%s] [action:%s] Missing implementation!", phase, bestCandidate.getFullNameQualified()));
                }
            }
        }

        final String stageType = stage.getKind();

        // Else, legacy way
        String[] candidates;

        candidates = new String[]{stageType};

        for (String k : candidates) {
            if (map.containsKey(k)) {
                logger.debug(String.format("[phase:%s] stage %s => action %s", phase, k, map.get(k).getClass().getCanonicalName()));
                return map.get(k).get();
            }
        }

        // Still not found? Maybe a generic override,
        // If force org.spark.XXX but we dont register it
        // ex: org.spark.sql configurator ==> forward to generic sql configurator
        for (ActionProvider provider : providers.values()) {
            final String providerName = provider.getName();

            if (stageType.startsWith(providerName + ".")) {
                final String shortName = stageType.substring(providerName.length() + 1);

                if (map.containsKey(shortName)) {
                    V action = map.get(shortName).get();

                    logger.debug(String.format("stage %s (aka %s) => provider %S action %s",
                            stageType,
                            shortName,
                            providerName,
                            action.getClass().getCanonicalName())
                    );

                    return action;
                }
            }
        }

        return null;
    }

    /**
     * Get Stage processor FQN candidate.
     *
     * @param providers Map<String, StageProvider>
     * @param stage Stage
     * @return List<ImmutablePair<String, Integer>>
     */
    public List<StageCandidate> getCandidates(Map<String, ActionProvider> providers, Stage stage, String phase) {
        return
                providers.values()
                        .stream()
                        .map(stageProvider -> stageProvider.getGenericActionCandidates(stage, phase, executionContext))
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparingInt(StageCandidate::getScore).reversed())
                        .collect(Collectors.toList())
                ;
    }
}
