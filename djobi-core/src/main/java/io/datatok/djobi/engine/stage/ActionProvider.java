package io.datatok.djobi.engine.stage;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import io.datatok.djobi.engine.flow.ExecutionContext;
import io.datatok.djobi.engine.phases.ActionPhases;
import io.datatok.djobi.engine.stage.livecycle.*;
import io.datatok.djobi.engine.utils.StageCandidate;

import java.util.HashMap;
import java.util.Map;

public abstract class ActionProvider extends AbstractModule {

    private MapBinder<String, ActionConfigurator> configurators;
    private MapBinder<String, ActionRunner> runners;
    private MapBinder<String, ActionPreChecker> preCheckers;
    private MapBinder<String, ActionPostChecker> postCheckers;

    /**
     * Store Action, not used by engine.
     *
     * @since v3.16.0
     */
    private final Map<String, Map<String, Class<? extends Action>>> actionsByPhase;

    /**
     * New provider pattern.
     *
     * @since v3.16.0
     */
    private MapBinder<String, ActionProvider> providers;

    public ActionProvider() {
        this.actionsByPhase = new HashMap<String, Map<String, Class<? extends Action>>>(){{
            put(ActionPhases.CONFIGURE, new HashMap<>());
            put(ActionPhases.PRE_CHECK, new HashMap<>());
            put(ActionPhases.POST_CHECK, new HashMap<>());
            put(ActionPhases.RUN, new HashMap<>());
        }};
    }

    protected void registerAction(String phase, String name, ActionProvider provider) {
        if (this.providers == null) {
            this.providers = MapBinder.newMapBinder(binder(), String.class, ActionProvider.class);
        }

        this.providers.addBinding(name).toInstance(provider);
    }

    protected void registerProvider(String name, ActionProvider provider) {
        if (this.providers == null) {
            this.providers = MapBinder.newMapBinder(binder(), String.class, ActionProvider.class);
        }

        this.providers.addBinding(name).toInstance(provider);
    }


    protected void registerConfigurator(String name, Class<? extends ActionConfigurator> serviceClass) {
        if (this.configurators == null) {
            this.configurators = MapBinder.newMapBinder(binder(), String.class, ActionConfigurator.class);
        }

        actionsByPhase.get(ActionPhases.CONFIGURE).put(name, serviceClass);

        configurators.addBinding(name).to(serviceClass);
    }

    protected void registerRunner(String name, Class<? extends ActionRunner> serviceClass) {
        if (runners == null) {
            this.runners = MapBinder.newMapBinder(binder(), String.class, ActionRunner.class);
        }

        actionsByPhase.get(ActionPhases.RUN).put(name, serviceClass);

        runners.addBinding(name).to(serviceClass);
    }

    protected void registerPreChecker(String name, Class<? extends ActionPreChecker> serviceClass) {
        if (preCheckers == null) {
            this.preCheckers = MapBinder.newMapBinder(binder(), String.class, ActionPreChecker.class);
        }

        actionsByPhase.get(ActionPhases.PRE_CHECK).put(name, serviceClass);

        preCheckers.addBinding(name).to(serviceClass);
    }

    protected void registerPostChecker(String name, Class<? extends ActionPostChecker> serviceClass) {
        if (postCheckers == null) {
            this.postCheckers = MapBinder.newMapBinder(binder(), String.class, ActionPostChecker.class);
        }

        actionsByPhase.get(ActionPhases.POST_CHECK).put(name, serviceClass);

        postCheckers.addBinding(name).to(serviceClass);
    }

    protected MapBinder<String, ? extends Action> getMapByPhase(final String phase) {
        switch (phase) {
            case ActionPhases.RUN:
                return runners;
            case ActionPhases.PRE_CHECK:
                return preCheckers;
            case ActionPhases.POST_CHECK:
                return postCheckers;
            case ActionPhases.CONFIGURE:
                return configurators;
        }

        return null;
    }

    protected boolean hasAction(final String phase, final String type) {
        return actionsByPhase.get(phase).containsKey(type);
    }

    /**
     * Get best stage implementation.
     *
     * @param stage
     * @param phase
     * @param executionContext
     * @return
     */
    abstract public StageCandidate getGenericActionCandidates(final Stage stage, String phase, ExecutionContext executionContext);

    abstract public String getName();
}
