package io.datatok.djobi.utils.di;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SimpleProviderFactory {

    @Inject
    private Injector injector;

    public <T> SimpleProvider<T> get(Class<T> prototype) {
        return new SimpleProvider<T>(this.injector, prototype);
    }

}
