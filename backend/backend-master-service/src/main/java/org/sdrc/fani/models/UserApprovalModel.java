package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

@Data
public class UserApprovalModel {

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
	
	private String areaName;
	
	private int slNo;
	
	private List<Integer> areaId;
	
	private List<String> designationId;
	
	private String firstName;
	
	private String middleName;
	
	private  String lastName;
	
	private Integer degSlugId;

}
