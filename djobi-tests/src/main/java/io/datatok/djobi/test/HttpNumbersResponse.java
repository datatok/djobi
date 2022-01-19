package io.datatok.djobi.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpNumbersResponse {

    public Integer _int;

    public Float _float;

    public String _string;

    public HttpNumbersResponse() {

    }

}
