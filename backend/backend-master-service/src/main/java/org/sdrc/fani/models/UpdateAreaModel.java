package org.sdrc.fani.models;

import lombok.Data;

@Data
public class UpdateAreaModel {
	
	private Integer areaId;

	private String areaName;

	private int parentAreaId;

}
