package com.synavos.maps.google.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpeningHours {

    private List<Period> periods;

    @JsonProperty("weekday_text")
    private List<String> weekdayText;

}
