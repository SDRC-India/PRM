package org.sdrc.fani.repositories;

import java.util.List;

import org.sdrc.fani.collections.DesignationPartnerFormMapping;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.data.mongodb.repository.MongoRepository;

import in.co.sdrc.sdrcdatacollector.models.AccessType;

/**
 * @author subham
 *
 */
public interface DesignationPartnerFormMappingRepository
		extends MongoRepository<DesignationPartnerFormMapping, String> {

	List<DesignationPartnerFormMapping> findBypartnerIdIn(List<String> partnerId);

	List<DesignationPartnerFormMapping> findBypartnerIdInAndAccessType(List<String> partnerId, AccessType dataEntry);

	DesignationPartnerFormMapping findByDesignationAndAccessType(Designation desg, AccessType downloadRawData);

	DesignationPartnerFormMapping findByDesignationAndAccessTypeAndPartnerId(Designation desg,
			AccessType downloadRawData, String partnerId);

	List<DesignationPartnerFormMapping> findBypartnerIdInAndAccessTypeAndDesignation(List<String> partnerId,
			AccessType accessType, Designation findById);

}
