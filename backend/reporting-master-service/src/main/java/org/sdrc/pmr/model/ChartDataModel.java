package org.sdrc.pmr.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ChartDataModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -862283395056227152L;

	private String axis;
	
	private String value;
	
	private Integer id;
	
	private String legend;
	
	private String cssClass;
	
	private String unit;
	
	private String numerator;
	
	private String denominator;
	
	private String label;
	
	private String tooltipValue;

	private Double dblVal;
}