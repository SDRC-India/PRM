package org.sdrc.fani.service;

import java.util.List;

import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.Partner;
import org.sdrc.fani.models.PushpinModel;
import org.sdrc.fani.models.SectorModel;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface DashboardService {

	public String pushIndicatorGroupData();

	public List<SectorModel> getDashboardData(Integer areaId,String partnerId,String sectorName);
	
	public List<String> getAllSectors(OAuth2Authentication auth);
	public List<Partner> getSectorWisePartner(String SectorName);

	public List<Area> getPartnerWiseArea(String partnerId);

	public List<PushpinModel> getpushpinData(Integer indicatorId ,Integer areaId);
	
	public String exportGroupIndicator() throws Exception;
	
	public String exportIndicator() throws Exception;
	
	public String getLastAggregationTime() throws Exception;
	
	public String exportDataValue() throws Exception;
}
