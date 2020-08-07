package org.sdrc.fani.models;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PushpinModel {
	
	 private Integer id;
	 private String villageName;
	 private String gpName;
	 private String areaName;
	private String dataValue;
	private String longitude;
	private String latitude;
	private String images;
	private String cciName;
	private String awcName;
	private String title;
	private boolean showWindow;
	private String icon;
	private String dateOfVisit;

}
