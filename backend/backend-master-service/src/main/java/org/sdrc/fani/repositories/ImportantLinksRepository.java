package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.ImportantLinks;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ImportantLinksRepository extends MongoRepository<ImportantLinks, String> {

	ImportantLinks findById(String id);

	@Query("{'submittedBy':{$in:?0}}")
	List<ImportantLinks> findBySubmittedByInAndIsActiveTrue(List<String> userName);

	List<ImportantLinks> findBySubmittedByAndIsActiveTrue(String name);

	List<ImportantLinks> findByIsActiveTrueAndIsApproveTrue();

}
