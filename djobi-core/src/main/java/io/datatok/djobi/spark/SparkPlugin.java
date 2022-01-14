package io.datatok.djobi.spark;

import com.google.inject.Module;
import io.datatok.djobi.application.plugins.Plugin;
import io.datatok.djobi.spark.actions.SparkActionProvider;

public class SparkPlugin extends Plugin {

    @Override
    public Module getDIModule() {
        return new SparkActionProvider();
    }

}
