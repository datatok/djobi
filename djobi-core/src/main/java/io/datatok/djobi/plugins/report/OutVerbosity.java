package io.datatok.djobi.plugins.report;

import com.google.inject.Singleton;

@Singleton
public class OutVerbosity {

    private VerbosityLevel verbosityLevel = VerbosityLevel.NORMAL;

    public boolean isQuiet() {
        return verbosityLevel.compareTo(VerbosityLevel.QUIET) <= 0;
    }

    public boolean isNotQuiet() {
        return verbosityLevel.compareTo(VerbosityLevel.QUIET) > 0;
    }

    public boolean isVerbose() {
        return verbosityLevel.compareTo(VerbosityLevel.VERBOSE) >= 0;
    }

    public boolean isVeryVerbose() {
        return verbosityLevel.compareTo(VerbosityLevel.VERY_VERBOSE) >= 0;
    }

    public VerbosityLevel getVerbosityLevel() {
        return verbosityLevel;
    }

    public void setVerbosityLevel(VerbosityLevel verbosityLevel) {
        this.verbosityLevel = verbosityLevel;
    }
}
