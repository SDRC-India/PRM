package org.sdrc.fani.collections;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Subsector implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	
	private Integer formId;

	private Integer subSectorId;

	private String subsectorName;
	
	private Sector sector;

}
