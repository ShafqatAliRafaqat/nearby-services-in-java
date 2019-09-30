package com.synavos.maps.mongo;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.synavos.maps.google.api.response.Place;

public interface PlaceRepository extends MongoRepository<Place, String> {

    public Optional<Place> findByPlaceId(final String placeId);

    public List<Place> findByPlaceIdIn(final List<String> placeIds);

    public Long deleteByPlaceId(final String placeId);

    public Long deleteByBuild(final boolean build);

    public Stream<Place> findByTypesIn(final List<String> types);

    public long countByTypesIn(final List<String> types);

    public long deleteByCachedAtLessThanAndBuild(final Date date, final boolean build);

}
