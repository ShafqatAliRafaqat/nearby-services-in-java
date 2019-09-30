package com.synavos.maps.google.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Period {
    private DayTime open;
    private DayTime close;

    @lombok.Data
    @lombok.ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DayTime {
	private Integer day;
	private String time;
    }

}
