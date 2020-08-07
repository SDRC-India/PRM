package org.sdrc.fani.repositories;

import org.sdrc.fani.collections.PartnersDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PartnersDetailsRepository extends MongoRepository<PartnersDetails, String> {
	
}
