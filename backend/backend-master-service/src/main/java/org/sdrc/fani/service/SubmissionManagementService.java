package org.sdrc.fani.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.sdrc.fani.models.UpdateAreaModel;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.models.ValueObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;

/**
 * @author subham
 *
 */
public interface SubmissionManagementService {

	ResponseEntity<String> rejectSubmissions(ValueObject valueObject, OAuth2Authentication auth);

	Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(Integer formId, UserModel user,
			String submissionId, Map<String, Object> paramKeyValMap, HttpSession session);

	List<DataObject> getReiewDataHead(Integer formId, UserModel user, Map<String, Object> paramKeyValMap);
	
	List<EnginesForm> getAllEnginesForms(OAuth2Authentication oauth);
	
	Map<String,List<UpdateAreaModel>> getUpdatedAreas(String lastUpdatedDate);
	

	

}
