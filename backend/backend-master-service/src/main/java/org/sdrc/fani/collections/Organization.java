package org.sdrc.fani.collections;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Organization implements Serializable{
	
	private static final long serialVersionUID = 1021351168080243607L;
	@Id
	private String id;

	private Integer organizationId;

	private String organizationName;

}
