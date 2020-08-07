package org.sdrc.fani.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.sdrc.fani.collections.Area;
import org.sdrc.fani.models.AccountTableJSONModel;
import org.sdrc.fani.models.RejectionModel;
import org.sdrc.fani.models.UserApprovalModel;
import org.sdrc.fani.service.PartnerRegistrationService;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author subham
 *
 */
@RestController
public class PartnerRegistrationController {
	
	@Autowired
	private PartnerRegistrationService partnerRegistrationService;
	
	@GetMapping("/getAllRoles")
	public List<Designation> getAllRoles(){
		return partnerRegistrationService.getAllRoles();
	}
	
	
	/*@GetMapping(value = "/getEmailVarificationCode")
	 public  ResponseEntity<String> getEmailVarificationCode(
				@RequestParam("email") String email) {
			return partnerRegistrationService.getEmailVerificationCode(email);
		}*/
	 
	/* @GetMapping(value = "/getEmailOTPAvailability")
	public ResponseEntity<String> OTPAndEmailAvailibility(@RequestParam("email") String email,
			@RequestParam("varificationCode") Integer varificationCode) {
		return partnerRegistrationService.oTPAndEmailAvailibility(email, varificationCode);
	}*/
	 
	 @GetMapping("/getAllUser")
	 @PreAuthorize("hasAuthority('USER_MGMT_ALL_API')")
		public AccountTableJSONModel getPartnersForApproval() {
			return partnerRegistrationService.getPartnersForApproval();
		}
	 

		@GetMapping("/approveUser")
		@PreAuthorize("hasAuthority('USER_MGMT_ALL_API')")
		public ResponseEntity<String> approvePartner(@RequestParam("ids") List<String> ids) {
			return partnerRegistrationService.approvePartner(ids);
		}

		@PostMapping("/rejectUser")
		@PreAuthorize("hasAuthority('USER_MGMT_ALL_API')")
		public ResponseEntity<String> rejectPartner(@RequestParam("ids") List<String> ids,@RequestBody List<RejectionModel> rejectionModel) {
			return partnerRegistrationService.rejectPartner(ids,rejectionModel);
		}
		
		@GetMapping("/getUsersByRole")
		@PreAuthorize("hasAuthority('USER_MGMT_ALL_API')")
		public List<UserApprovalModel> getUsersByRoleAndPartner(@RequestParam("roleId") List<String> roleIds){
			return partnerRegistrationService.getUsersByRoleAndPartner(roleIds);
			
			
		}@RequestMapping(value="/area")
		public List<Area> getAreaList(@RequestParam("areaLevelId") String areaLevelId, 
				@RequestParam("parentAreaId") String parentAreaId){
			return partnerRegistrationService.getAreaList(Integer.parseInt(areaLevelId), Integer.parseInt(parentAreaId));
		}
		

//		@RequestMapping(value = "/bypass/downloadTemplate", method = RequestMethod.GET)
		@PostMapping(value = "/downloadTemplate")
		public ResponseEntity<InputStreamResource> downLoad() throws Exception {
//		       userManagementService.downLoadFile((userManagementService.downLoadBulkTemplate()), response, inline);
			String filePath = "";
			try {
				filePath = partnerRegistrationService.downLoadBulkTemplate();
				File file = new File(filePath);

				HttpHeaders respHeaders = new HttpHeaders();
				respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
				InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

				file.delete();
				return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
			}
		}
		
		 @PostMapping(value = "/uploadTemplate")
		 @PreAuthorize("hasAuthority('USER_MGMT_ALL_API')")
	     public List<String> uploadTemplate(@RequestParam(value = "templateFile", required=false) MultipartFile bulkUserFile) throws Exception {
	 		return partnerRegistrationService.uploadBulkTemplate(bulkUserFile);
	  }  

}
