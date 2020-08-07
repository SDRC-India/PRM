package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

@Data
public class ParamModel {
	
	
	private String districtName;
	private String blockName;
	private Integer districtId;
	private Integer blockId;
	private String stateName;
	private Integer stateId;
	private Integer areaLevelId;
	private String dashboardType;
	private Integer sectorId;
	private Integer tpId;
	private Integer formId;
	List<SVGModel> listOfSvgs;
	private String areaLevelName;
	private String checklistName;
	private String sectorName;
	private String timeperiod;
	private String villageName;
	private Integer villageId;
	private String partnerId;
	private Integer areaId;
}
