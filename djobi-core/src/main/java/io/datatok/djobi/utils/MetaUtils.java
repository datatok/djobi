package io.datatok.djobi.utils;

import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class MetaUtils {

    final static private String FIELD_MODE = "mode";
    final static private String FIELD_TITLE = "title";

    public Map<String, String> clean(Map<String, String> metaIn) {

        if (metaIn == null) {
            metaIn = new HashMap<>();
        }

        final Map<String, String> res =
                metaIn.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toLowerCase(),
                            Map.Entry::getValue
                    ));

        if (!res.containsKey(FIELD_MODE)) {
            res.put(FIELD_MODE, "manual");
        }

        if (!res.containsKey(FIELD_TITLE)) {
            res.put(FIELD_TITLE, "unknown");
        }

        return res;
    }

}
