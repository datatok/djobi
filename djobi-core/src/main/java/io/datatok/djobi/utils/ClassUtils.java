package io.datatok.djobi.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassUtils {

    static public List<String> getClassFields(Class clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.getDeclaringClass().equals(clazz))
            .map(Field::getName)
            .collect(Collectors.toList());
    }

    static public <T> Map<String, String> getClassFieldValues(T holder) {
        final Class clazz = holder.getClass();

        return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.getDeclaringClass().equals(clazz))
            .collect(Collectors.toMap(
                    Field::getName,
                    f -> {
                        try {
                            return f.get(holder).toString();
                        } catch (IllegalAccessException e) {
                            return "_IllegalAccessException_";
                        }
                    }
            ));
    }

    static public boolean isClass(String className) {
        try  {
            Class.forName(className);
            return true;
        }  catch (ClassNotFoundException e) {
            return false;
        }
    }
}
