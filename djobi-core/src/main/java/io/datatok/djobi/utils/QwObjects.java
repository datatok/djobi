package io.datatok.djobi.utils;

public class QwObjects {

    /**
     * Returns {@code true} if the provided reference is non-{@code null}
     * otherwise returns {@code false}.
     * @since 3.2.0
     */
    public static boolean nonNull(Object ...args) {
        for (Object arg : args) {
            if (arg == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns {@code true} if the provided reference is {@code null}
     * otherwise returns {@code false}.
     * @since 3.2.0
     */
    public static boolean isNull(Object ...args) {
        for (Object arg : args) {
            if (arg == null) {
                return true;
            }
        }

        return false;
    }

}
