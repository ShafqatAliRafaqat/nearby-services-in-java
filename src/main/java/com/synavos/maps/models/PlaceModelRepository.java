package com.synavos.maps.models;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.synavos.maps.models.custom.PlaceModelCustomRepository;

/**
 * The Interface PlaceModelRepository.
 * 
 * @author Ibraheem Faiq
 * @since Apr 3, 2018
 */
public interface PlaceModelRepository extends JpaRepository<PlaceModel, Long>, PlaceModelCustomRepository {

    @Query(value = "SELECT CURRENT_TIMESTAMP FROM places Limit 1", nativeQuery = true)
    public Timestamp getCurrentTime();

    
    public List<PlaceModel> findByDeleted(final Boolean deleted);
    
}
