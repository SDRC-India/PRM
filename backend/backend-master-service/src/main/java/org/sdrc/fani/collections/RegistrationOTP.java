package org.sdrc.fani.collections;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class RegistrationOTP {
	
	@Id
	String id;
	
	private String emailId;
	
	private String ipAddress;
	
	private Date createdDateAndTime;
	
	private Integer varificationCode;
	
	private Boolean isActive;
	
}
