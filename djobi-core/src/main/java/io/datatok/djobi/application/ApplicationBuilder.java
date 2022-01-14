package io.datatok.djobi.application;

import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import io.datatok.djobi.application.exceptions.BuildApplicationException;
import io.datatok.djobi.application.plugins.Plugin;
import io.datatok.djobi.application.plugins.PluginBootstrap;
import io.datatok.djobi.configuration.Configuration;
import io.datatok.djobi.engine.phases.PhaseModule;
import io.datatok.djobi.utils.ClassUtils;
import io.datatok.djobi.utils.io.IOFileUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class ApplicationBuilder {

    private static final Logger logger = Logger.getLogger(ApplicationBuilder.class);

    final private ApplicationData runData = new ApplicationData();

    final private DependencyInjectorBuilder dependencyInjectorBuilder = new DependencyInjectorBuilder();

    private Configuration configuration;

    final private List<Plugin> plugins = new ArrayList<>();

    public static ApplicationBuilder newBuilder() {
        return new ApplicationBuilder();
    }

    public ApplicationBuilder() {
        dependencyInjectorBuilder
                .add(new PhaseModule());
        ;
    }

    public ApplicationBuilder setJvmArgs(String[] jvmArgs) {
        this.runData.setJvmArgs(jvmArgs);

        return this;
    }

    public ApplicationBuilder configure() throws BuildApplicationException {
        check();

        logger.debug("Building configuration");

        return configure(ConfigFactory.load());
    }

    public ApplicationBuilder configure(Config config) {
        this.configuration = new Configuration(config);

        this.dependencyInjectorBuilder.add(Configuration.class, configuration);

        return this;
    }

    /**
     * Add plugin (logging, report ...)
     *
     * @since v3.6.0
     * @param pluginPrototype Plugin
     * @return ApplicationBuilder
     */
    public ApplicationBuilder addPlugin(final Plugin pluginPrototype) {
        if (this.plugins.stream().noneMatch(p -> p.getClass().equals(pluginPrototype.getClass()))) {
            logger.debug(String.format("Add plugin \"%s\"", pluginPrototype.toString()));
            this.plugins.add(pluginPrototype);
        } else {
            logger.debug(String.format("Add plugin \"%s\"", pluginPrototype.toString()));
        }

        return this;
    }

    /**
     * Load plugins, from configuration.
     *
     * @since v3.7.0
     * @return ApplicationBuilder
     */
    public ApplicationBuilder loadPlugins() throws BuildApplicationException {
        if (this.configuration == null) {
            throw new BuildApplicationException("configuration must be not null to load plugins!");
        }

        if (!this.configuration.hasPath("djobi.plugins")) {
            throw new BuildApplicationException("configuration must provide \"djobi.plugins\" configuration!");
        }

        final ConfigObject config = this.configuration.getObject("djobi.plugins");

        config.forEach((key, value) -> {
            final boolean enabled = this.configuration.getBoolean("djobi.plugins." +  key + ".enabled");

            if (enabled) {
                final String clazz = this.configuration.getString("djobi.plugins." +  key + ".class");

                logger.info("Load plugin " + key);

                if (ClassUtils.isClass(clazz)) {
                    logger.info("Load plugin - " + key + " @ " + clazz);

                    try {
                        this.addPlugin((Plugin) Class.forName(clazz).getConstructor().newInstance());
                    } catch (ReflectiveOperationException e) {
                        logger.error("Loading plugin \"" + key + "\"", e);
                    }
                }
            }

        });

        return this;
    }

    public ApplicationBuilder readReleaseNote() {
        try {
            final Properties releaseNotes = new Properties();

            releaseNotes.load(IOFileUtils.openInClassPath("META-INF/djobi-app.properties"));

            this.runData.setReleaseNote(releaseNotes);

            logger.info(String.format(" Version %s", this.runData.getVersion()));

            //releaseNotes.forEach((k, v) -> logger.info(String.format("%s : %s", k, v)));

        } catch (IOException e) {
            logger.error(String.format("Cant read release note: \"%s\"", e.getMessage()));
        }

        return this;
    }

    public ApplicationBuilder addDependency(final Class clazz, final Object instance) {
        dependencyInjectorBuilder.add(clazz, instance);

        return this;
    }

    public ApplicationBuilder addDependency(final Class clazz, final Class clazzTo) {
        dependencyInjectorBuilder.add(clazz, clazzTo);

        return this;
    }

    /**
     * Build the application.
     *
     * @return Djobi
     */
    public Djobi build() throws CreationException {

        dependencyInjectorBuilder.add(ApplicationData.class, this.runData);

        // Get plugins DI module
        this.plugins.stream()
                .map(Plugin::getDIModule)
                .filter(Objects::nonNull)
                .forEach(dependencyInjectorBuilder::add)
        ;

        // Create the DI injector
        final Injector injector = dependencyInjectorBuilder.build();

        // Bootstrap plugins
        this.plugins.stream()
                .map(Plugin::getBootstrap)
                .filter(Objects::nonNull)
                .map(injector::getInstance)
                .forEach(PluginBootstrap::bootstrap)
        ;

        // Create the application that hold the DI injector
        return new Djobi(injector, runData);
    }

    private void check() throws BuildApplicationException {
        final String configFile = System.getProperty("config.file");

        if (configFile == null) {
            throw new BuildApplicationException("Property \"config.file\" is required!");
        }

        logger.info(String.format("check: use %s config file.", configFile));
    }
}
