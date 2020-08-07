package org.sdrc.pmr.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class IndicatorGroupModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6036059675099642100L;
	private String indicatorName;
	private Integer indicatorId;
	private String indicatorValue;
	private String tooltipValue;
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
	private boolean isLevelWise = false;
	//in case of card set numerator and denominator value
	private String numerator;
	
	private String denominator;
}