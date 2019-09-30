package com.synavos.maps.google;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synavos.maps.beans.PriorityCount;
import com.synavos.maps.config.EnvConfig;
import com.synavos.maps.models.ReferencePointModel;
import com.synavos.maps.models.ReferencePointModelRepository;
import com.synavos.maps.utils.CommonUtils;
import com.synavos.maps.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class ReferencePointService.
 *
 * @author Ibraheem Faiq
 * @since Apr 4, 2018
 */
@Service
@Slf4j
public class ReferencePointModelService {

    @Autowired
    private ReferencePointModelRepository referencePointModelRepository;

    public List<ReferencePointModel> fetchRequests(final Integer priority) {
	log.info(log.isInfoEnabled() ? "Fetching Reference points from DB... " : null);

	List<ReferencePointModel> referencePoints = fetchByPriority(priority);

	log.trace(log.isTraceEnabled() ? StringUtils.concatValues("Reference points found from DB ", referencePoints)
		: null);

	if (!CommonUtils.isNullOrEmptyCollection(referencePoints)) {
	    referencePoints = referencePoints.stream()
		    .filter(point -> !CommonUtils.isNullOrEmptyCollection(point.getTypes()))
		    .collect(Collectors.toList());

	    log.debug(log.isDebugEnabled()
		    ? StringUtils.concatValues("Reference Points filtered count [", referencePoints.size(), "]")
		    : null);
	}

	return referencePoints;
    }

    private List<ReferencePointModel> fetchByPriority(final Integer priority) {
	final List<ReferencePointModel> referencePoints;

	if (CommonUtils.isNotNull(priority)) {
	    referencePoints = referencePointModelRepository.findByPriority(priority);
	}
	else {
	    referencePoints = referencePointModelRepository.findAll();
	}

	return referencePoints;
    }

    /**
     * Fetch distinct defined priorities.
     *
     * @return distinct defined priorities
     */
    public Set<Integer> fetchDistinctDefinedPriorities() {
	log.debug(log.isDebugEnabled() ? "Fetching distinct defined priorities in ascending order..." : null);

	final Set<Integer> priorities = EnvConfig.DEV_ENV ? referencePointModelRepository.findDevUniquePriorities()
		: referencePointModelRepository.findUniquePriorities();

	log.debug(log.isDebugEnabled() ? StringUtils.concatValues("Found distinct defined priorities : ", priorities)
		: null);

	return priorities;
    }

    public ReferencePointModel saveByPriority(final ReferencePointModel _referencePointModel) {
	ReferencePointModel referencePointModel = _referencePointModel;

	if (CommonUtils.isNotNull(referencePointModel)) {
	    int priority;

	    final PriorityCount priorityCount = fetchMaxPriorityCount();

	    if (priorityCount.getPriority() < 10) {
		priority = 10;
	    }
	    else if (priorityCount.getRecordsCount() > 1) {
		priority = priorityCount.getPriority() + 1;
	    }
	    else {
		priority = priorityCount.getPriority();
	    }

	    referencePointModel.setPriority(priority);

	    referencePointModel = referencePointModelRepository.save(referencePointModel);
	}

	return referencePointModel;
    }

    /**
     * Fetch priority count.
     *
     * @return the priority count model
     */
    public PriorityCount fetchMaxPriorityCount() {
	PriorityCount countModel = new PriorityCount();

	Object[] resultSet = referencePointModelRepository.findMaxPriorityWithCount();

	if (!CommonUtils.isNullOrEmptyArray(resultSet)) {
	    resultSet = (Object[]) resultSet[0];
	    countModel.setRecordsCount((Long) resultSet[0]);
	    countModel.setPriority((Integer) resultSet[1]);
	}
	else {
	    countModel.setRecordsCount(0L);
	    countModel.setPriority(0);
	}

	return countModel;
    }

}
