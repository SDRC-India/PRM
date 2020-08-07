package org.sdrc.fani.models;

import lombok.Data;

@Data
public class PartnerApprovalModel {

	private String id;

	private String userName;

	private String name;

	private String dob;

	private String gender;

	private String mobileNumber;

	private String organization;

	private String designation;

	private String submittedOn;

	private String email;

	private String status;
	
	private Boolean isActive;
	
	private String partnerName;

}
