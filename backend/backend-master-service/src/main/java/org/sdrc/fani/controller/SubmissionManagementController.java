package org.sdrc.fani.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.sdrc.fani.models.UpdateAreaModel;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.models.ValueObject;
import org.sdrc.fani.service.SubmissionManagementService;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.engine.FormsService;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class SubmissionManagementController {

	@Autowired
	private SubmissionManagementService reviewService;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private FormsService dataEntryService;

	@RequestMapping(value = "/rejectMultipleSubmission", method = RequestMethod.POST)
	public ResponseEntity<String> rejectSubmissions(@RequestBody ValueObject valueObject, OAuth2Authentication auth) {

		return reviewService.rejectSubmissions(valueObject, auth);

	}

	@GetMapping("/getRejectedData")
	public Map<String, List<DataObject>> getRejectedData(HttpSession session, OAuth2Authentication auth) {
		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);

		Map<String, Object> map = new HashMap<>();

		return dataEntryService.getRejectedData(map, session, user, user.getDesgSlugIds().get(0));

	}

	

	/**
	 * This method returns only review data head
	 * 
	 * @param formId
	 * @param auth
	 * @return
	 */
	@GetMapping("/getReviewData")
	public List<DataObject> getDataForReview(@RequestParam("formId") Integer formId, OAuth2Authentication auth) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> paramKeyValMap = new HashMap<>();
		paramKeyValMap.put("review", "reviewData");
		return reviewService.getReiewDataHead(formId, user, paramKeyValMap);

	}

	@GetMapping("/reviewViewMoreData")
	public Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(
			@RequestParam("formId") Integer formId, @RequestParam("submissionId") String submissionId,
			OAuth2Authentication auth, HttpSession session) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> paramKeyValMap = new HashMap<>();
		paramKeyValMap.put("review", "reviewData");
		return reviewService.getViewMoreDataForReview(formId, user, submissionId, paramKeyValMap, session);

	}
	
	@GetMapping("/getAllEnginesForms")
	public List<EnginesForm> getDataForReview(OAuth2Authentication oauth) {
		return reviewService.getAllEnginesForms(oauth);

	}
	
	
	@GetMapping("/getUpdatedAreas")
	public Map<String,List<UpdateAreaModel>> getUpdatedAreas(@RequestParam(value = "lastUpdatedDate") String lastUpdatedDate) {
		return reviewService.getUpdatedAreas(lastUpdatedDate);
	}
	
}
