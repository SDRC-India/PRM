package org.sdrc.pmr.service;

import java.util.Map;

import org.sdrc.pmr.model.ReportChartModel;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * @author subham
 *
 */
public interface RawDataReportService {

	String getRawDataReport(Integer formId,String startDate,String endDate,OAuth2Authentication oauth)throws Exception;
	
	String getAreaAndUserWiseRawDataReport(Integer reportType,String startDate,String endDate,OAuth2Authentication oauth);
	
	Map<Integer,ReportChartModel> getDataForCard(OAuth2Authentication oauth);

	String getRawDataReportId(Integer formId, String startDate, String endDate, OAuth2Authentication oauth);

	String getAreaWiseSkillReport(Integer formId, String startDate, String endDate, OAuth2Authentication oauth);

}
