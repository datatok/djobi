package io.datatok.djobi.utils.di;

import com.google.inject.Injector;

import javax.inject.Provider;

public class SimpleProvider<T> implements Provider<T> {

    private Injector injector;

    private Class<T> prototype;

    public SimpleProvider(Injector injector, Class<T> prototype) {
        this.injector = injector;
        this.prototype = prototype;
    }

    public Injector getInjector() {
        return injector;
    }

    public Class<T> getPrototype() {
        return prototype;
    }

    @Override
    public T get() {
        return injector.getInstance(this.prototype);
    }
}
