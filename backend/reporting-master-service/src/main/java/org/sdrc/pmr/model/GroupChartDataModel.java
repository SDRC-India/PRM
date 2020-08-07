package org.sdrc.pmr.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class GroupChartDataModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7055558632310926128L;
	private String headerIndicatorName;
//	private Integer headerindicatorId;
	private Integer headerIndicatorValue;
	private List<List<ChartDataModel>> chartDataValue;
	private List<LegendModel> legends;

}
