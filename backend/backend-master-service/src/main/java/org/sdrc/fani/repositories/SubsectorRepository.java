package org.sdrc.fani.repositories;


import java.util.List;

import org.sdrc.fani.collections.Subsector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SubsectorRepository extends MongoRepository<Subsector, String> {

	Subsector findTopByOrderByIdDesc();
	
	
	@Query("{'sector.sectorName' :{$in:?0 }}")
	List<Subsector> findBySectorSectorNameIn(List<String> sectorName);
	
	@Query("{'sector.sectorName' :{$in:?0 }, 'sector.formId' :{$in:?1 }}")
	List<Subsector> findBySectorSectorNameInAndFormIdIn(List<String> sectorName,List<Integer> formId);

}
