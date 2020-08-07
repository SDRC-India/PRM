package org.sdrc.fani.collections;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class GroupIndicator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String indicatorGroup;
	private Integer kpiIndicator;
	private List<String> chartType;
	private List<List<Integer>> chartIndicators;
	private String chartAlign;
	private String sector;
	private String sectorId;
	private String subSector;
	private String valueFrom;
	private String chartHeader;
	private String kpiChartHeader;
	private String align;
	private String cardType;
	private String chartLegends;
	private String colorLegends;
	private String unit;
	private String chartGroup;
	private String extraInfo;
	private String parentChart;
	private Integer orderBy;
}
