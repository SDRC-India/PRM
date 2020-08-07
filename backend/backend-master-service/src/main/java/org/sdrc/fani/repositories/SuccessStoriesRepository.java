package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.SuccessStories;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SuccessStoriesRepository extends MongoRepository<SuccessStories, String> {

	
	
	SuccessStories findById(String id);
	List<SuccessStories> findBySubmittedByAndIsActiveTrue(String name);
	
	@Query("{'submittedBy':{$in:?0}}")
	List<SuccessStories> findBySubmittedByInAndIsActiveTrue(List<String> name);
	
	List<SuccessStories> findByIsActiveTrueAndIsApproveTrue();

	

}
