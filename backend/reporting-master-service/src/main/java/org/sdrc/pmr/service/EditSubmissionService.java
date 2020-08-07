package org.sdrc.pmr.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.sdrc.pmr.model.EditSubmissionModel;
import org.springframework.http.ResponseEntity;

import in.co.sdrc.sdrcdatacollector.models.QuestionModel;

/**
 * @author Sarita Panigrahi
 *
 */
public interface EditSubmissionService {

	ResponseEntity<List<EditSubmissionModel>> getSubmissionsToEdit(String mobileNo, Integer formId);

	Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(Integer formId,
			String submissionId, Map<String, Object> paramKeyValMap, HttpSession session);

}
