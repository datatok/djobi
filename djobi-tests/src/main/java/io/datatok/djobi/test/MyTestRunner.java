package io.datatok.djobi.test;

import com.google.inject.Injector;
import io.datatok.djobi.application.ApplicationBuilder;
import io.datatok.djobi.application.Djobi;
import io.datatok.djobi.engine.Engine;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.plugins.s3.S3Plugin;
import io.datatok.djobi.plugins.stages.DefaultActionsPlugin;
import io.datatok.djobi.spark.SparkPlugin;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class MyTestRunner implements TestInstancePostProcessor {

    static public Injector injector;

    public MyTestRunner() {
        if (injector == null) {
            try {
                final Djobi application =
                    new ApplicationBuilder()
                        .configure()
                        .addDependency(Reporter.class, TestStdoutReporter.class)
                        .readReleaseNote()
                        .addPlugin(new DefaultActionsPlugin())
                        .addPlugin(new SparkPlugin())
                        .addPlugin(new S3Plugin())
                        .build()
                    ;

                injector = application.getInjector();

                injector.getInstance(Engine.class).setClearJobAfterExecution(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context)
            throws Exception {
        injector.injectMembers(testInstance);
    }
}
