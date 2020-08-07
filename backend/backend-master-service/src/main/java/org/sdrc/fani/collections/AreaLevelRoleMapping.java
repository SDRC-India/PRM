package org.sdrc.fani.collections;

import java.io.Serializable;

import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class AreaLevelRoleMapping implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3889339582370832819L;

	@Id
	private String id;
	
	private Integer alrm_id_pk;
	
	
	private AreaLevel areaLevel;


	private Designation designation;
	
	
	/*@CreationTimestamp
	private Timestamp createdDate;*/
	
}
