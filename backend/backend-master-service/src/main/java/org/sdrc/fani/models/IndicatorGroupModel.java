package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

@Data
public class IndicatorGroupModel {
	
	private String indicatorName;
	private Integer indicatorId;
	private String indicatorValue;
	private String indicatorGroupName;
	private String timeperiod;
	private String periodicity;
	private Integer timeperiodId;
	private List<String> chartsAvailable;
	private String align;
	private String cardType;
	private String unit;
	private String chartAlign;
	private List<GroupChartDataModel> chartData;
	private String chartGroup;
	private String extraInfo;
}
