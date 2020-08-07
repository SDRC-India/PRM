package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

@Data
public class GroupChartDataModel {
	
	private String headerIndicatorName;
//	private Integer headerindicatorId;
	private Integer headerIndicatorValue;
	List<List<ChartDataModel>> chartDataValue;
	List<LegendModel> legends;

}