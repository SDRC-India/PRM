package org.sdrc.fani.repositories;

import org.sdrc.fani.collections.EngineRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnginesRoleRepoitory extends MongoRepository<EngineRole, String>{

	EngineRole findByRoleCode(String trimWhitespace);

}
