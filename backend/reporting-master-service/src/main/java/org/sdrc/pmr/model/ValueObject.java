package org.sdrc.pmr.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ValueObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3992660311551196750L;
	private String numerator;
	private String denominator;
	private Double value;
	private String areaId;
	private String areaName;
	private Integer areaLevelId;
	private String cssClass;
	private String name;
	private Integer inid;
}
