package org.sdrc.fani.collections;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Partner {

	@Id
	private String id;
	
	private Integer  slugId;
	
	private String partnerId;

	private Date createdDate;

	private Date updatedDate;

	private Map<String, Object> data;

	private Boolean isApproved;

	private Boolean isActive;

}
