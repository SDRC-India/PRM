package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.Partner;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PartnerRepository extends MongoRepository<Partner, String> {

	Partner findById(String partnertId);

	@Query(value = "{ $and: [{'data.organization_name' :?0},{'isApproved' :true},{'isActive' :true}]}")
	List<Partner> findByOrganizationName(String orgname);

	List<Partner> findByIdIn(List<String> partnerIds);

}
