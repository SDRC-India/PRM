package org.sdrc.fani.collections;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class EngineRole {

	@Id
	private String id;
	
	private Integer roleId;

	private String roleCode;

	private String roleName;
	
	private String description;
}
