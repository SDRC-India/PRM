package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.WhatsNew;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface WhatsNewRepository extends MongoRepository<WhatsNew, String> {

	WhatsNew findById(String id);

	@Query("{'submittedBy':{$in:?0}}")
	List<WhatsNew> findBySubmittedByInAndIsActiveTrue(List<String> userName);

	List<WhatsNew> findBySubmittedByAndIsActiveTrue(String name);

	List<WhatsNew> findByIsActiveTrueAndIsApproveTrue();

}
