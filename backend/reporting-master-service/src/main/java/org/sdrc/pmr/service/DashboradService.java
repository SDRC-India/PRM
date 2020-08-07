package org.sdrc.pmr.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.sdrc.pmr.model.SectorModel;
import org.sdrc.pmr.model.ValueObject;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface DashboradService {

	void importIndicators() throws InvalidFormatException, IOException;

	String aggregate(Integer areaLevelId, Integer areaId);

	String pushIndicatorGroupData() throws InvalidFormatException, IOException;

//	List<SectorModel> getDashboardData(Integer areaLevel, Integer areaId,OAuth2Authentication oauth);

	Map<String, ValueObject>  getThematicMapData(Integer inid);

//	Map<String, ValueObject> getSkillWiseData(Integer areaId);
	List< ValueObject> getSkillWiseData(Integer areaId);

	boolean isDataAvailable();

//	List<SectorModel> getDashboardDataFromCache(Integer areaLevel, Integer areaId, OAuth2Authentication oauth);

	List<SectorModel> getDashboardData(Integer areaLevelId, Integer areaId, OAuth2Authentication oauth);

	Map<String, ValueObject> getSkillTypeThematicMapData(Integer inid);

	Map<String, ValueObject> getStateThematicMapData(Integer originStateId);

}
