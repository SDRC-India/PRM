package org.sdrc.fani.service;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.MessageModel;

/**
 * @author subham
 *
 */
public interface RawDataReportService {

	ResponseEntity<MessageModel> exportRawaData(Integer formId, String startDate, String endDate, Principal principal,
			OAuth2Authentication auth);

	List<EnginesForm> getRawDataReportAccessForms(OAuth2Authentication auth);
	
	ResponseEntity<String> getRawDataReport(Integer formId,OAuth2Authentication oauth)throws Exception;

}
