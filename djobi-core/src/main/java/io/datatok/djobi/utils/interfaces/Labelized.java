package io.datatok.djobi.utils.interfaces;

import java.util.Map;

public interface Labelized {

    Map<String, String> getLabels();

    String getLabel(String key);

    Object setLabels(Map<String, String> labels);

}
