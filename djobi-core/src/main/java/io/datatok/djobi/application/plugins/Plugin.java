package io.datatok.djobi.application.plugins;

import com.google.inject.Module;

public class Plugin {

    public Module getDIModule() {
        return null;
    }

    public Class<? extends PluginBootstrap> getBootstrap() {
        return null;
    }

}
