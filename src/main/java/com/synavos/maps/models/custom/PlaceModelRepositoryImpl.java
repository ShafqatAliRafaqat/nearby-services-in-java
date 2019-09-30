package com.synavos.maps.models.custom;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;

import com.synavos.maps.models.PlaceModel;
import com.synavos.maps.properties.GoogleMapProperties;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlaceModelRepositoryImpl implements PlaceModelCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<PlaceModel> fetchPlaceModels(final Timestamp timestamp) {
	List<PlaceModel> places = null;

	log.info(log.isInfoEnabled() ? "Fetching places from DB for supported types" : null);

	final CriteriaBuilder cb = em.getCriteriaBuilder();
	final CriteriaQuery<PlaceModel> query = cb.createQuery(PlaceModel.class);

	final Root<PlaceModel> place = query.from(PlaceModel.class);
	query.select(place);

	final Predicate[] predicates = new Predicate[GoogleMapProperties.SUPPORTED_PLACE_TYPES.size()];
	for (int i = 0; i < GoogleMapProperties.SUPPORTED_PLACE_TYPES.size(); i++) {
	    final String supportedType = GoogleMapProperties.SUPPORTED_PLACE_TYPES.get(i);
	    predicates[i] = cb.like(place.<String>get("types"), StringUtils.concatValues("%", supportedType, "%"));
	}
	
	final Predicate typesPredicates = cb.or(predicates);

	List<Predicate> predicatesList = new LinkedList<>();

	predicatesList.add(typesPredicates);
	predicatesList.add(cb.isNotNull(place.get("latitude")));
	predicatesList.add(cb.isNotNull(place.get("longitude")));
	predicatesList.add(cb.isNotNull(place.get("longitude")));


	if (CommonUtils.isNotNull(timestamp)) {
	    predicatesList.add(cb.greaterThan(place.<Timestamp>get("updatedAt"), timestamp));
	}

	query.where(predicatesList.toArray(new Predicate[0]));

	final TypedQuery<PlaceModel> typedQuery = em.createQuery(query);
	places = typedQuery.getResultList();

	log.debug(log.isDebugEnabled() ? StringUtils.concatValues("Fetched places from DB : ", places) : null);

	return places;
    }
}
