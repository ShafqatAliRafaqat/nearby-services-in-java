package com.synavos.maps.models.custom;

import java.sql.Timestamp;
import java.util.List;

import com.synavos.maps.models.PlaceModel;

public interface PlaceModelCustomRepository {

    public List<PlaceModel> fetchPlaceModels(final Timestamp timestamp);
}
