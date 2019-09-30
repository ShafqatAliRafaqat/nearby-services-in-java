package com.synavos.maps.google.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Review {

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("author_url")
    private String authorUrl;

    private String language;

    @JsonProperty("profile_photo_url")
    private String profilePhotoUrl;

    private Double rating;

    private String text;

    private Long time;

}
