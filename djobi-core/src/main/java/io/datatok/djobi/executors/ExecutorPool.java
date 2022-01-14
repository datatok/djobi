package io.datatok.djobi.executors;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.datatok.djobi.spark.executor.SparkExecutor;
import io.datatok.djobi.utils.ClassUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ExecutorPool {

    private static Logger logger = Logger.getLogger(ExecutorPool.class);

    @Inject
    private Injector injector;

    private Map<String, Executor> executors = new HashMap<>();

    public Executor get(final String type) {
        if (executors.containsKey(type)) {
            return executors.get(type);
        }

        Executor executor = make(type);

        executors.put(type, executor);

        return executor;
    }

    public Executor getDefault() {
        return get("_default_");
    }

    protected Executor make(final String type) {
        switch(type) {
            case SparkExecutor.TYPE:
                return injector.getInstance(SparkExecutor.class);
            case LocalExecutor.TYPE:
                return injector.getInstance(LocalExecutor.class);
            case "_default_":
                // @todo hardcode horrible
                if (ClassUtils.isClass("org.apache.spark.sql.SQLContext")) {
                    return injector.getInstance(SparkExecutor.class);
                }

                return injector.getInstance(LocalExecutor.class);
            default:
                Class clazz;

                try {
                    clazz = Class.forName(type);
                    return (Executor) clazz.newInstance();
                } catch(ClassNotFoundException e) {
                    logger.fatal("Making executor " + type, e);
                } catch (IllegalAccessException e) {
                    logger.fatal("Making executor " + type, e);
                } catch (InstantiationException e) {
                    logger.fatal("Making executor " + type, e);
                }
        }

        return null;
    }
}
