package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

@Data
public class TableDataModel {
	private Integer sl_No;
	private String title;
	private String publishedBy;
	private String id;
	private List<String> tag;
	private String uploadedOn;
	private String videoLink;
	private String description;
	private Boolean isApprove;
	private String firstName;
	private String resourceType;
	private String resourceTypeId;
	private String galleryType;
	private String galleryTypeId;
	private String caption;
	private String url;
	

}
