package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.Resources;
import org.sdrc.fani.service.ResourceModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ResourcesRepository extends MongoRepository<Resources, String> {
	
	Resources findById(String id);
	
	List<Resources> findBySubmittedByAndIsActiveTrue(String name);
	
	@Query("{'submittedBy':{$in:?0}}")
	List<Resources> findBySubmittedByInAndIsActiveTrue(List<String> name);

	List<Resources> findByIsActiveTrueAndIsApproveTrue();


	
	

}
