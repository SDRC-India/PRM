package org.sdrc.fani.collections;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.sdrc.fani.models.Gender;
import org.sdrc.fani.models.UserStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetails implements Serializable {

	private static final long serialVersionUID = -4503442884290702891L;

	private String firstName;

	private String middleName;

	private String lastName;
	
	private Gender gender;

	private String mobNo;
	
	private String dob;

	private String rejectionMessage;
	
	private Date rejectedOn;
	
	private Date approvedOn;
	
	private UserStatus userStatus;
	
	private String rejectedBy;
	
	private String approvedBy;
	
	private Date createdDate;
	
	private List<Integer> areaId;

}
