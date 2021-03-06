package org.sdrc.pmr.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sdrc.pmr.collection.ChecklistSubmissionStatus;
import org.sdrc.pmr.collection.TimePeriod;

import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import lombok.Data;

@Data
public class AllChecklistFormDataDTO {

	private String id;

	private String userName;

	private String userId;

	private String submittedBy;

	private Date createdDate;

	private Date updatedDate;

	private Date syncDate;

	private Map<String, Object> data;

	private Integer formId;

	private String uniqueId;

	private boolean rejected = false;

	private String rejectMessage;

	private String uniqueName;

	private TimePeriod timePeriod;

	private Boolean isAggregated;

	private Boolean isValid = true;

	private Integer attachmentCount = 0;

	Map<String, List<FormAttachmentsModel>> attachments;

	private ChecklistSubmissionStatus checklistSubmissionStatus = ChecklistSubmissionStatus.COMPLETE;

	private Boolean latest;

	private Date rejectedApprovedDate;

	@org.springframework.data.annotation.Version
	private Integer version;

	private String rejectedBy;

	private Date rejectedDate;

}