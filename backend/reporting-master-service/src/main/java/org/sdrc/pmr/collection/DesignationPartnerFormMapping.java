package org.sdrc.pmr.collection;

import java.util.Date;
import java.util.List;

import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.util.Status;
import lombok.Data;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Data
@Document
public class DesignationPartnerFormMapping {

	@Id
	private String id;

	private Integer desgPartnerFormMappingId;

	private Designation designation;

	private List<Integer> formId;

	private Date createdDate;

	private AccessType accessType;

	private Status status;
	
	private String partnerId;

}
