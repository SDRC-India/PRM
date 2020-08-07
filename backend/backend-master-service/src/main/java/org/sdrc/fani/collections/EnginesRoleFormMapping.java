package org.sdrc.fani.collections;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.util.Status;
import lombok.Data;

@Data
@Document
public class EnginesRoleFormMapping {

	@Id
	private String id;

	private Integer roleFormMappingId;

	private EngineRole role;

	private EnginesForm form;

	@CreationTimestamp
	private Date createdDate;

	private AccessType accessType;

	private Status status;

}
