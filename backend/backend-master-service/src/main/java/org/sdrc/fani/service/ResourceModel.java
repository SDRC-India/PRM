package org.sdrc.fani.service;

import java.util.Date;
import java.util.List;

import org.sdrc.fani.models.FilepathCaptionModel;
import org.sdrc.fani.models.TableDataModel;

import lombok.Data;
@Data
public class ResourceModel {
	private String id;
	private String title;
	private String description;
	private List<FilepathCaptionModel> filepathCaptionModel;
	private String resourceType;
	private String videoLink;
	private List<String> tags;
	private String submittedBy;
	private Date createdDate;
	private Date updatedDate;
	
	private List<String> tableColumn;
	
	private List<TableDataModel> tableData;

}
