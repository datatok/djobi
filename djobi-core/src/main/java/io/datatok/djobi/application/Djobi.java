package io.datatok.djobi.application;

import com.google.inject.Injector;

public class Djobi {

    /**
     * @since v3.3.1
     */
    private Injector injector;

    /**
     * @since v3.3.1
     */
    private ApplicationData data;

    public Djobi(Injector injector, ApplicationData runData) {
        this.injector = injector;
        this.data = runData;
    }

    public ApplicationData getData() {
        return data;
    }

    public Injector getInjector() {
        return injector;
    }
}
