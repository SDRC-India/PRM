package org.sdrc.fani.service;

import java.util.List;
import java.util.Map;

import org.sdrc.fani.models.AreaModel;
import org.sdrc.fani.models.FilepathCaptionModel;
import org.sdrc.fani.models.SuccessStoriesModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.multipart.MultipartFile;


public interface PartnerService {
	
	ResponseEntity<String> getEmailVerificationCode(String email);
	 
	 ResponseEntity<String> oTPAndEmailAvailibility(String email,Integer varificationCode);
	  
	 Map<String, List<AreaModel>> getAllAreaList();
	 
	 ResponseEntity<String> saveSucessStories(SuccessStoriesModel successStoriesModel,OAuth2Authentication oauth);
	 
	 Boolean partnerAvailable(String partnerName);
	 
	 ResponseEntity<String> uploadFile(MultipartFile listOfmultipartfiles);
	 
	 SuccessStoriesModel getAllSuccessStories(OAuth2Authentication oauth);
	
	List<FilepathCaptionModel> getSuccessStoriesDetails(String id);
	
	ResponseEntity<String> deleteSuccessStory(String id,OAuth2Authentication oauth);
	
	ResponseEntity<String> approveSuccessStory(String id,OAuth2Authentication oauth);
	
	SuccessStoriesModel getAllSuccessStoriesForHomePage(OAuth2Authentication oauth);

}
