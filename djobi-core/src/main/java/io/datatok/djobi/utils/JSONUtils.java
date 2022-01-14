package io.datatok.djobi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.util.StdDateFormat;

import java.io.IOException;

public class JSONUtils {

    static private ObjectMapper objectMapper;

    static public String serialize(Object data) throws JsonProcessingException {
        return getMapper().writeValueAsString(data);
    }

    static public <T> T  parse(String json, Class<T> clazz) throws IOException, JsonProcessingException, JsonMappingException {
        return getMapper().readValue(json, clazz);
    }

    static public ObjectMapper getMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            objectMapper.setDateFormat(StdDateFormat.getBlueprintISO8601Format());

        }

        return objectMapper;
    }

}
