package org.sdrc.fani.service;

import java.util.List;

import org.sdrc.fani.collections.Area;
import org.sdrc.fani.models.AccountTableJSONModel;
import org.sdrc.fani.models.RejectionModel;
import org.sdrc.fani.models.UserApprovalModel;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author subham
 *
 */
public interface PartnerRegistrationService {
	
	List<Designation> getAllRoles();

	ResponseEntity<String> getEmailVerificationCode(String email);

	ResponseEntity<String> oTPAndEmailAvailibility(String email, Integer varificationCode);

	AccountTableJSONModel getPartnersForApproval();

	ResponseEntity<String> approvePartner(List<String> ids);

	ResponseEntity<String> rejectPartner(List<String> ids, List<RejectionModel> rejectionModel);

	List<UserApprovalModel> getUsersByRoleAndPartner(List<String> roleIds);

	List<Area> getAreaList(int areaLevelId, int parentAreaId);

	List<String> uploadBulkTemplate(MultipartFile multipartfile) throws Exception;

	String downLoadBulkTemplate() throws Exception;
	


}
