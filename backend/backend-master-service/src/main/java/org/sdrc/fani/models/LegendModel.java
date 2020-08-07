package org.sdrc.fani.models;

import lombok.Data;

@Data
public class LegendModel {
	
	private String cssClass;
	private String value;
	private String color;
	private String range;
	private Double startRange;
	private Double endRange;
}
