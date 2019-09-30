package com.synavos.maps.models;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.synavos.maps.beans.Location;
import com.synavos.maps.constants.CommonConstants;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.AccessLevel;

/**
 * The Class ReferencePointModel.
 *
 * @author Ibraheem Faiq
 * @since Mar 28, 2018
 */
@Entity
@Table(name = "reference_points")
@lombok.Data
@lombok.NoArgsConstructor
@lombok.ToString(exclude = { "types" })
public class ReferencePointModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer priority;

    @NotNull
    private Integer depth;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String placeTypes;

    @Transient
    @lombok.Setter(value = AccessLevel.PRIVATE)
    private List<String> types;

    @Transient
    private Location location;

    /**
     * Sets the lat lng.
     *
     * @param lat
     *            the lat
     * @param lng
     *            the lngR
     */
    public void setLatLng(Double lat, Double lng) {
	setLatitude(lat);
	setLongitude(lng);
    }

    /**
     * Gets the types.
     *
     * @return the types
     */
    @Transient
    public List<String> getTypes() {
	if (CommonUtils.isNull(types)) {

	    if (StringUtils.isNullOrEmptyStr(placeTypes)) {
		types = GoogleMapProperties.GOOGLE_SUPPORTED_PLACE_TYPES;
	    }
	    else {
		types = CommonUtils.getListFromString(placeTypes, CommonConstants.COMMA);

		if (!CommonUtils.isNullOrEmptyCollection(types)) {
		    types = types.stream().filter(type -> GoogleMapProperties.GOOGLE_SUPPORTED_PLACE_TYPES.contains(type))
			    .collect(Collectors.toList());
		}
		else {
		    types = null;
		}
	    }
	}

	return types;
    }

    @Transient
    public Location getLocation() {
	if (CommonUtils.isNull(location)) {
	    location = new Location(getLatitude(), getLongitude());
	}

	return location;
    }
}
