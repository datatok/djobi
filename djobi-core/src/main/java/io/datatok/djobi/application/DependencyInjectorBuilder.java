package io.datatok.djobi.application;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.datatok.djobi.engine.stage.ActionProvider;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create Dependencies Injection container.
 */
public class DependencyInjectorBuilder {

    static Logger logger = Logger.getLogger(DependencyInjectorBuilder.class);

    private final Map<Class, Object> bindToInstances = new HashMap<>();
    private final Map<Class, Class> bindToClass = new HashMap<>();
    private final List<Module> modules = new ArrayList<>();

    static public DependencyInjectorBuilder get() {
        return new DependencyInjectorBuilder();
    }

    public DependencyInjectorBuilder add(final Class<?> clazz, final Object instance) {
        bindToInstances.put(clazz, instance);

        return this;
    }

    public DependencyInjectorBuilder add(final Class<?> clazz, final Class<?> clazzTo) {
        bindToClass.put(clazz, clazzTo);

        return this;
    }

    public DependencyInjectorBuilder add(Module module) {
        modules.add(module);

        return this;
    }

    public DependencyInjectorBuilder add(List<ActionProvider> argModules) {
        modules.addAll(argModules);

        return this;
    }

    public Injector build() throws CreationException {
        final AbstractModule myModule = new AbstractModule() {
            @Override
            protected void configure() {
                bindToClass.forEach( (k, v) -> bind(k).to(v));
                bindToInstances.forEach( (k, v) -> bind(k).toInstance(v));
            }
        };

        modules.add(myModule);

        return Guice.createInjector(modules);
    }
}
