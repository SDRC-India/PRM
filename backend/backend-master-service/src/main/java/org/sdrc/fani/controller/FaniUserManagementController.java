package org.sdrc.fani.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.sdrc.fani.models.AreaModel;
import org.sdrc.fani.models.FilepathCaptionModel;
import org.sdrc.fani.models.SuccessStoriesModel;
import org.sdrc.fani.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;



@RestController
public class FaniUserManagementController {
		
	@Autowired
	private PartnerService partnerService;
	
	
	
	@RequestMapping("/")
	public String test() {
		return ("Welcome to covid19rmrs data collection service");

	}
	
	
	 @GetMapping(value = "/getEmailVarificationCode")
	 public  ResponseEntity<String> getEmailVarificationCode(
				@RequestParam("email") String email) {
			return partnerService.getEmailVerificationCode(email);
		}
	 
	 @GetMapping(value = "/getEmailOTPAvailability")
	public ResponseEntity<String> OTPAndEmailAvailibility(@RequestParam("email") String email,
			@RequestParam("varificationCode") Integer varificationCode) {
		return partnerService.oTPAndEmailAvailibility(email, varificationCode);
	}
	 
	 @ResponseBody
		@RequestMapping(value = "/getAllArea")
		public Map<String, List<AreaModel>> getArea() {
			return partnerService.getAllAreaList();

		}
	 
	 @ResponseBody
		@RequestMapping(value = "/saveSucessStories")
		public ResponseEntity<String> saveSucessStories(@RequestBody SuccessStoriesModel successStoriesModel,OAuth2Authentication oauth) {
			return partnerService.saveSucessStories(successStoriesModel,oauth);

		}
	 
	 @ResponseBody
		@RequestMapping(value = "/getpartnerAvailable")
		public Boolean partnerAvailable(@RequestParam("partnername") String partnername) {
			return partnerService.partnerAvailable(partnername);

		}
	 
	 
	 @ResponseBody
		@RequestMapping(value = "/uploadFile")
		public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file) {
			return partnerService.uploadFile(file);

		}
	 
	 @ResponseBody
	 @RequestMapping(value="/getAllSuccessStories")
	 public SuccessStoriesModel getAllSuccessStories(OAuth2Authentication oauth){
		 return partnerService.getAllSuccessStories(oauth);
	 }

	 @ResponseBody
	 @RequestMapping(value="/getSuccessStoriesDetails")
	 public List<FilepathCaptionModel> getSuccessStoriesDetails(@RequestParam("storyId") String storyId){
		 return partnerService.getSuccessStoriesDetails(storyId);
	 }
	 
	 @ResponseBody
	 @RequestMapping(value="/deleteSuccessStory")
	 public ResponseEntity<String> deleteSuccessStory(@RequestParam("storyId") String storyId,OAuth2Authentication oauth){
		 return partnerService.deleteSuccessStory(storyId,oauth);
	 }
	 
	 
	 @ResponseBody
	 @RequestMapping(value="/approveSuccessStory")
	 public ResponseEntity<String> approveSuccessStory(@RequestParam("storyId") String storyId,OAuth2Authentication oauth){
		 return partnerService.approveSuccessStory(storyId,oauth);
	 }
	 
	 @ResponseBody
	 @RequestMapping(value="/getAllSuccessStoriesForHomePage")
	 public SuccessStoriesModel getAllSuccessStoriesForHomePage(OAuth2Authentication oauth){
		 return partnerService.getAllSuccessStoriesForHomePage(oauth);
	 }
}
