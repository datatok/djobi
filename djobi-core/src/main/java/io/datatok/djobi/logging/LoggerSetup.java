package io.datatok.djobi.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

public class LoggerSetup {

    /**
     * Initialize logger from java property info.
     */
    static public void init() {

        final String logLevel = System.getProperty("log.level", "info");
        final String logDestination = System.getProperty("log.out", "stdout");

        if (System.getProperty("debug.properties") != null) {
            System.getProperties().forEach((key, value) -> System.out.printf("%s -> %s%n", key, value));
        }

        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.valueOf(logLevel));
    }

}
