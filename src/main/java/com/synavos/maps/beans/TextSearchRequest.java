package com.synavos.maps.beans;

import javax.validation.constraints.NotEmpty;

import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

/**
 * Instantiates a new text search request.
 */
@lombok.Data
@lombok.ToString
public class TextSearchRequest {

    /** The text. */
    @NotEmpty(message = "Empty text in text search request")
    private String text;

    /** The city. */
    private String city;

    /** The count. */
    private Integer count;
    
    /** The location. */
    private Location location;

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {
	return CommonUtils.isNotNull(count) && count > 0 ? count : 20;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
	return StringUtils.isNullOrEmptyStr(text) ? null : text.toLowerCase().trim();
    }
}
