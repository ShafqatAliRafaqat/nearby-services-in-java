package com.synavos.maps.models;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * The Interface ReferencePointModelRepository.
 * 
 * @author Ibraheem Faiq
 * @since Mar 28, 2018
 */
public interface ReferencePointModelRepository extends JpaRepository<ReferencePointModel, Long> {

    /**
     * Find by priority.
     *
     * @param priority
     *            the priority
     * @return the list
     */
    public List<ReferencePointModel> findByPriority(final Integer priority);

    @Query("SELECT DISTINCT r.priority FROM ReferencePointModel r Order By r.priority ASC")
    public Set<Integer> findUniquePriorities();

    @Query("SELECT DISTINCT r.priority FROM ReferencePointModel r WHERE r.priority=1 Order By r.priority ASC")
    public Set<Integer> findDevUniquePriorities();

    @Query("SELECT count(r.id) as recordsCount, r.priority from ReferencePointModel r where r.priority = (SELECT max(r1.priority) from ReferencePointModel r1) group by r.priority")
    public Object[] findMaxPriorityWithCount();

}
