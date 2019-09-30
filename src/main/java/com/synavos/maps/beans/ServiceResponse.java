package com.synavos.maps.beans;

import java.util.Collection;

import org.springframework.http.HttpStatus;

import com.synavos.maps.google.api.response.Place;

public class ServiceResponse {

    private Collection<Place> places;

    private HttpStatus httpStatus;

    private String msg;

    public ServiceResponse(HttpStatus httpStatus, String msg) {
	this.httpStatus = httpStatus;
	this.msg = msg;
    }

    public Collection<Place> getPlaces() {
	return places;
    }

    public void setPlaces(Collection<Place> places) {
	this.places = places;
    }

    public HttpStatus getHttpStatus() {
	return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
	this.httpStatus = httpStatus;
    }

    public String getMsg() {
	return msg;
    }

    public void setMsg(String msg) {
	this.msg = msg;
    }

    @Override
    public String toString() {
	return "NearbyServiceResponse [places=" + places + ", httpStatus=" + httpStatus + ", msg=" + msg + "]";
    }

}
