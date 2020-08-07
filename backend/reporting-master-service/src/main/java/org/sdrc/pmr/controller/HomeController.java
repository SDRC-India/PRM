package org.sdrc.pmr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.sdrc.pmr.collection.AllChecklistFormData;
import org.sdrc.pmr.model.AllChecklistFormDataDTO;
import org.sdrc.pmr.model.EditSubmissionModel;
import org.sdrc.pmr.service.ApiService;
import org.sdrc.pmr.service.EditSubmissionService;
import org.sdrc.pmr.util.OAuth2Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.co.sdrc.sdrcdatacollector.models.QuestionModel;

/**
 * @author Sarita
 *
 */
@RestController
public class HomeController {

	@Autowired
	private EditSubmissionService editSubmissionService;

	@Autowired
	private ApiService apiService;
	
	@Autowired
	private OAuth2Utility oAuth2Utility; 
	
	@GetMapping("/")
	public String home() {
		return "Welcome to PRM consumer service";
	}

	@PostMapping("/getSubmissionsToEdit")
	public ResponseEntity<List<EditSubmissionModel>> getSubmissionsToEdit(@RequestParam("mobileNo") String mobileNo,
			@RequestParam("formId") Integer formId) {
		return editSubmissionService.getSubmissionsToEdit(mobileNo, formId);
	}

	@GetMapping("/reviewViewMoreData")
	public Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(
			@RequestParam("formId") Integer formId, @RequestParam("submissionId") String submissionId,
			OAuth2Authentication auth, HttpSession session) {

		Map<String, Object> paramKeyValMap = new HashMap<>();
		paramKeyValMap.put("review", "reviewData");
		return editSubmissionService.getViewMoreDataForReview(formId, submissionId, paramKeyValMap, session);

	}

//	@GetMapping("/bypass/getInstance")
//	public List<String> getInstance() {
////		apiService.save();
////		return null;
//		return apiService.getAllUnAckInstances();
//	}
	
	/*@GetMapping("/getInstance")
	public String getInstance() {
		return oAuth2Utility.fetchInstances();
	}*/
	
}
