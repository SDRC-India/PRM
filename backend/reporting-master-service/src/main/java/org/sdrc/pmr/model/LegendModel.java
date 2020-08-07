package org.sdrc.pmr.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class LegendModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3839555748208814350L;
	private String cssClass;
	private String value;

}