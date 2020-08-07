package org.sdrc.fani.models;

import lombok.Data;

@Data
public class SVGModel {
	
	private String indicatorGroupName;
	private String svg;
	private String chartType;
	private String chartAlign;
	private Integer showValue;
	private String showNName;
	private String indName;

}
