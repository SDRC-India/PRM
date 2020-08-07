package org.sdrc.fani.models;

import java.util.Date;
import java.util.List;

import lombok.Data;
@Data
public class SuccessStoriesModel {
	private String id;
	private String title;
	private String description;
	private List<FilepathCaptionModel> filepathCaptionModel;
	private String videoLink;
	private List<String> tags;
	private String submittedBy;
	private Date createdDate;
	private Date updatedDate;
	
	private List<String> tableColumn;
	
	private List<TableDataModel> tableData;

}
