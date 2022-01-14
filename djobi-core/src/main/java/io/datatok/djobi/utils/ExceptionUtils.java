package io.datatok.djobi.utils;

import java.util.ArrayList;
import java.util.List;

public class ExceptionUtils {

    public static List<String> getExceptionMessageChain(Throwable throwable) {
        List<String> result = new ArrayList<>();
        while (throwable != null) {
            result.add(throwable.getMessage());
            throwable = throwable.getCause();
        }
        return result; //["THIRD EXCEPTION", "SECOND EXCEPTION", "FIRST EXCEPTION"]
    }

    public static String getExceptionMessageChainAsString(Throwable throwable) {
        return String.join(", ", getExceptionMessageChain(throwable));
    }

}
