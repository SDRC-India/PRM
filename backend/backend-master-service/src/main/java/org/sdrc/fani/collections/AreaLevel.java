package org.sdrc.fani.collections;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Document
@Data
public class AreaLevel implements Serializable {

	private static final long serialVersionUID = 1519381375815795764L;
	
	@Id
	private String id;

	private Integer areaLevelId;

	private String areaLevelName;

	@JsonIgnore
	private List<AreaLevelRoleMapping> areaLevelRoleMappings;
	
	public AreaLevel(){
		super();
	}
	
	public AreaLevel(Integer areaLevelId) {
		
		this.areaLevelId = areaLevelId;
	}
	
	

}
