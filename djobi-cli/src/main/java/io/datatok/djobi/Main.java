package io.datatok.djobi;

import com.google.inject.Injector;
import io.datatok.djobi.application.ApplicationBuilder;
import io.datatok.djobi.application.Djobi;
import io.datatok.djobi.application.exceptions.BuildApplicationException;
import io.datatok.djobi.cli.CommandKernel;
import io.datatok.djobi.cli.StdoutReporter;
import io.datatok.djobi.cli.utils.CLISimpleUtils;
import io.datatok.djobi.cli.utils.CLIUtils;
import io.datatok.djobi.plugins.report.Reporter;
import io.datatok.djobi.plugins.s3.S3Plugin;
import io.datatok.djobi.plugins.stages.DefaultActionsPlugin;
import io.datatok.djobi.spark.SparkPlugin;
import io.datatok.djobi.utils.ClassUtils;
import picocli.CommandLine;

import java.io.Serializable;

final public class Main implements Serializable {
    private static final long serialVersionUID = 3L;

    /**
     * @param args
     *
     * @since 0.0.1
     */
    public static void main(String[] args) {
        final Djobi application;

        try {
            final ApplicationBuilder builder = new ApplicationBuilder()
                .setJvmArgs(args)
                .readReleaseNote()
                .configure()
                .addPlugin(new DefaultActionsPlugin())
            ;

            if (ClassUtils.isClass("org.apache.spark.sql.SQLContext")) {
                builder.addPlugin(new SparkPlugin());
            }

            if (ClassUtils.isClass("org.apache.spark.sql.SQLContext") &&
                ClassUtils.isClass("org.apache.hadoop.fs.s3a.S3AFileSystem")) {
                builder.addPlugin(new S3Plugin());
            }

            builder
                .addDependency(Reporter.class, StdoutReporter.class)
                .addDependency(CLIUtils.class, CLISimpleUtils.class)
            ;

            application = builder.loadPlugins().build();
        } catch(BuildApplicationException e) {
            e.printStackTrace();

            System.exit(1);

            return ;
        }

        Injector injector = application.getInjector();

        CommandKernel commandKernel = injector.getInstance(CommandKernel.class);
        CommandLine rootCommandImpl = commandKernel.getRootCommand();

        try {
            rootCommandImpl.execute(args);
        } catch(RuntimeException e) {
            e.printStackTrace();
        }
    }
}
