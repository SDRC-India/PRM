package org.sdrc.fani.repositories;


import org.sdrc.fani.collections.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationRepository extends MongoRepository<Organization, String> {

	Organization findByOrganizationId(Integer organizationId);
}
