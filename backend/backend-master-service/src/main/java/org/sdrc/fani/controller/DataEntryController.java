package org.sdrc.fani.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.service.SubmissionService;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import in.co.sdrc.sdrcdatacollector.engine.FormsService;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionUpdateModel;
import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;
import in.co.sdrc.sdrcdatacollector.models.ReviewPageModel;

/**
 * @author Azar
 * 
 * @author Subham Ashish(subham@sdrc.co.in)
 * 
 * @author Sarita
 */

@RestController
@RequestMapping("/api")
public class DataEntryController {

	@Autowired
	private FormsService dataEntryService;

	@Autowired
	private SubmissionService submissionService;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@GetMapping("/getQuestion")
	// @PreAuthorize("hasAnyAuthority('Data Entry',
	// 'dataentry_HAVING_write','Data Entry & Visualization')")
	public QuestionUpdateModel getQuestions(@RequestParam(value = "lastUpdatedDate", required = false) String lastUpdatedDate,HttpSession session,
			OAuth2Authentication auth) {

		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);

		Map<String, Object> map = new HashMap<>();
		String lastOpdatedOn = null;
		return dataEntryService.getQuestions(map, session, user, 2,lastUpdatedDate);
	}

	@RequestMapping(value = "/saveData", method = { RequestMethod.POST, RequestMethod.OPTIONS })
	public ResponseEntity<String> sendNewSubmissionCommand(@RequestBody ReceiveEventModel receiveEventModel,
			OAuth2Authentication oauth) throws Exception {
	   //return submissionService.saveSubmission(receiveEventModel, oauth);
		Gson gson = new Gson();
		return new ResponseEntity<String>(gson.toJson("इस ऐप का अपडेटेड  संस्करण  https://rahatup.in पोर्टल पर उपलब्ध है। कृपया डेटा संग्रह के लिए नवीनतम एप्लिकेशन का उपयोग करें।"), HttpStatus.PRECONDITION_FAILED);
	}

	@RequestMapping(value = "uploadFile", method = { RequestMethod.POST, RequestMethod.OPTIONS }, consumes = {
			"multipart/form-data" })
	public String uploadFiles(@RequestParam("file") MultipartFile file, @RequestParam("fileModel") String fileModel)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		FormAttachmentsModel readValue = objectMapper.readValue(fileModel, FormAttachmentsModel.class);

		int retryCount = 0;
		boolean uploaded = false;

		while (uploaded == false && retryCount < 1) {
			try {
				String response = submissionService.uploadFiles(file, readValue);
				uploaded = true;
				return response;
			} catch (OptimisticLockingFailureException e) {
				retryCount++;
				continue;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException("Error while uploading files with payload {} "+fileModel);

	}

	@GetMapping("/getAllForms")
	public ReviewPageModel getAllForms(HttpSession session, OAuth2Authentication auth) {
		UserModel user = tokenInfoExtracter.getUserModelInfo(auth);
		Map<String, Object> map = new HashMap<>();
		map.put("DISTRICT_ID", user.getAreas().get(0).getParentAreaId());
		map.put("BLOCK_ID", user.getAreas().get(0).getAreaId());

		return dataEntryService.getAllForms(map, session, user, user.getDesgSlugIds().get(0));
	}

}
