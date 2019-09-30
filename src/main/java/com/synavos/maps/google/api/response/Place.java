package com.synavos.maps.google.api.response;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.synavos.maps.beans.Location;
import com.synavos.maps.utils.CommonUtils;

/**
 * The Class Place.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.ToString
@Document(collection = "places")
public class Place extends Location {

    @Id
    private String id;

    /** The geometry. */
    private Geometry geometry;

    /** The icon. */
    private String icon;

    /** The name. */
    private String name;

    /** The place id. */
    @JsonProperty("place_id")
    @Indexed(unique = true)
    private String placeId;

    /** The rating. */
    private Double rating;

    /** The reference. */
    private String reference;

    /** The scope. */
    private String scope;

    /** The types. */
    private List<String> types;

    /** The vicinity. */
    private String vicinity;

    @JsonIgnore
    private String city;

    private PlaceDetails details;

    private boolean verified;

    @JsonIgnore
    private boolean build;

    private String timing;

    private Date cachedAt;

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((placeId == null) ? 0 : placeId.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Place other = (Place) obj;
	if (placeId == null) {
	    if (other.placeId != null)
		return false;
	}
	else if (!placeId.equals(other.placeId))
	    return false;
	return true;
    }

    public Geometry getGeometry() {
	return geometry;
    }

    public void setGeometry(Geometry geometry) {
	this.geometry = geometry;
    }

    public String getIcon() {
	return icon;
    }

    public void setIcon(String icon) {
	this.icon = icon;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getPlaceId() {
	return placeId;
    }

    public void setPlaceId(String placeId) {
	this.placeId = placeId;
    }

    public Double getRating() {
	return rating;
    }

    public void setRating(Double rating) {
	this.rating = rating;
    }

    public String getReference() {
	return reference;
    }

    public void setReference(String reference) {
	this.reference = reference;
    }

    public String getScope() {
	return scope;
    }

    public void setScope(String scope) {
	this.scope = scope;
    }

    public List<String> getTypes() {
	if (CommonUtils.isNull(types)) {
	    types = new LinkedList<>();
	}

	return types;
    }

    public void setTypes(List<String> types) {
	this.types = types;
    }

    public String getVicinity() {
	return vicinity;
    }

    public void setVicinity(String vicinity) {
	this.vicinity = vicinity;
    }

    public String getCity() {
	return city;
    }

    public void setCity(String city) {
	this.city = city;
    }

    @Override
    public Double getLatitude() {
	return this.getGeometry().getLocation().getLatitude();
    }

    @Override
    public Double getLongitude() {
	return this.getGeometry().getLocation().getLongitude();
    }

    public PlaceDetails getDetails() {
	return details;
    }

    public void setDetails(final PlaceDetails details) {
	this.details = details;
    }

    public boolean isBuild() {
	return build;
    }

    public void setBuild(boolean build) {
	this.build = build;
    }

    public String getTiming() {
	return timing;
    }

    public void setTiming(String timing) {
	this.timing = timing;
    }

    public boolean isVerified() {
	return verified;
    }

    public void setVerified(boolean verified) {
	this.verified = verified;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public Date getCachedAt() {
	if (null == cachedAt) {
	    cachedAt = new Date();
	}

	return cachedAt;
    }

}
