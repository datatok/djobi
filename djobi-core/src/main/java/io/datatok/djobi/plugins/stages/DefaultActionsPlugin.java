package io.datatok.djobi.plugins.stages;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.datatok.djobi.application.plugins.ActionProviderDiscover;
import io.datatok.djobi.application.plugins.Plugin;
import io.datatok.djobi.engine.actions.DjobiActionProvider;
import io.datatok.djobi.plugins.stages.api.APIModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultActionsPlugin extends Plugin {

    public Module getDIModule() {
        final List<Module> modules = new ArrayList<>(Arrays.asList(
            new DjobiActionProvider(),
            new APIModule()
        ));

        modules.addAll(new ActionProviderDiscover().discover());

        return Modules.combine(modules);
    }

}
