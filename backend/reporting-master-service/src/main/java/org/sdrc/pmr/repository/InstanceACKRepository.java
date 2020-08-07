package org.sdrc.pmr.repository;

import java.util.List;

import org.sdrc.pmr.collection.InstanceACK;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InstanceACKRepository extends MongoRepository<InstanceACK, String> {

	List<InstanceACK> findByAckFalse();
	
	InstanceACK findBySubmissionId(String id);
}
