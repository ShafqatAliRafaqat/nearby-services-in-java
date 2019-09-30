package com.synavos.maps.models;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synavos.maps.beans.Location;
import com.synavos.maps.constants.CommonConstants;
import com.synavos.maps.google.api.response.Geometry;
import com.synavos.maps.google.api.response.Place;
import com.synavos.maps.google.api.response.PlaceDetails;
import com.synavos.maps.utils.CommonUtils;

@Entity
@Table(name = "places")
@lombok.Data
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull(message = "Id of place cannot be empty")
    private Long id;

    @NotNull
    @NotEmpty(message = "name of place cannot be empty")
    private String name;

    @NotNull(message = "lat/lng cannot be null")
    private Double latitude;

    @NotNull
    @NotNull(message = "lat/lng cannot be null")
    private Double longitude;

    private String icon;

    private Double rating;

    @NotNull
    @NotEmpty(message = "type(s) of place cannot be empty")
    private String types;

    @NotNull
    @NotEmpty(message = "vicinity of place cannot be empty")
    private String vicinity;

    private String contactNumber;

    private String website;

    private Timestamp updatedAt;

    private Boolean deleted;

    private String timing;

    private Boolean verified;

    private boolean getVerified() {
	return (null == this.verified) ? false : verified.booleanValue();
    }

    @Transient
    public Place toPlace() {
	final Place place = new Place();
	place.setPlaceId(this.getId().toString());

	place.setName(this.getName());
	place.setIcon(this.getIcon());
	place.setRating(this.getRating());
	place.setVicinity(this.getVicinity());
	place.setTiming(this.getTiming());
	place.setVerified(this.getVerified());

	place.setTypes(CommonUtils.getListFromString(this.getTypes(), CommonConstants.COMMA));

	final Geometry geometry = new Geometry();
	geometry.setLocation(new Location(this.getLatitude(), this.getLongitude()));
	place.setGeometry(geometry);

	final PlaceDetails details = new PlaceDetails();
	details.setFormattedPhoneNumber(this.getContactNumber());
	details.setWebsite(this.getWebsite());

	place.setDetails(details);
	place.setBuild(true);

	return place;
    }

}
