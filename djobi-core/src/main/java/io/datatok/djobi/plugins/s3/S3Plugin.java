package io.datatok.djobi.plugins.s3;

import com.google.inject.Module;
import io.datatok.djobi.application.plugins.Plugin;

public class S3Plugin extends Plugin {

    @Override
    public Module getDIModule() {
        return new S3ActionProvider();
    }

}
