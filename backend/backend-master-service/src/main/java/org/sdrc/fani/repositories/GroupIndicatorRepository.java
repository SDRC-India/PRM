package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.GroupIndicator;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupIndicatorRepository extends MongoRepository<GroupIndicator, String>{

	List<GroupIndicator> findBySector(String sector);
	
	List<GroupIndicator> findBySectorIn(List<String> sectorIds);
	
	List<GroupIndicator> findBySectorIdIn(List<String> sectorIds);
	

}
