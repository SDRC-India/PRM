package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.Gallery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GalleryRepository  extends MongoRepository<Gallery, String> {

	Gallery findById(String id);

	@Query("{'submittedBy':{$in:?0}}")
	List<Gallery> findBySubmittedByInAndIsActiveTrue(List<String> userName);

	List<Gallery> findBySubmittedByAndIsActiveTrue(String name);

	List<Gallery> findByIsActiveTrueAndIsApproveTrue();

}
