package io.datatok.djobi.engine.phases;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

public class PhaseModule extends AbstractModule {

    @Override
    protected void configure() {
        final MapBinder<String, AbstractPhase> mapBinder = MapBinder.newMapBinder(super.binder(), String.class, AbstractPhase.class);

        mapBinder.addBinding(ActionPhases.CONFIGURE).to(ConfigurePhase.class);
        mapBinder.addBinding(ActionPhases.PRE_CHECK).to(PreCheckJobPhase.class);
        mapBinder.addBinding(ActionPhases.RUN).to(RunJobPhase.class);
        mapBinder.addBinding(ActionPhases.POST_CHECK).to(PostCheckJobPhase.class);
    }

}
