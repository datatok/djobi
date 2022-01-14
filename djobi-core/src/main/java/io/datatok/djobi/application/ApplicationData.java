package io.datatok.djobi.application;

import javax.inject.Singleton;
import java.util.Properties;

@Singleton
public class ApplicationData {

    private Properties releaseNote;

    private String[] jvmArgs;

    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Properties getReleaseNote() {
        return releaseNote;
    }

    public void setReleaseNote(Properties releaseNote) {
        this.releaseNote = releaseNote;
        this.version = releaseNote.getProperty("Implementation-Version");
    }

    public String[] getJvmArgs() {
        return jvmArgs;
    }

    public void setJvmArgs(String[] jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public String userAgent() {
        return "Djobi/" + version;
    }
}
