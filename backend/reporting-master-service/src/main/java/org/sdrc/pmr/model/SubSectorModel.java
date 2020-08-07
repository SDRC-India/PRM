package org.sdrc.pmr.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class SubSectorModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7308589643583627857L;
	private String subSectorName;
	private List<IndicatorGroupModel> indicators;
}