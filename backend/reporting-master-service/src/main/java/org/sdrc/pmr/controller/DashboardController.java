package org.sdrc.pmr.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.sdrc.pmr.collection.AllChecklistFormData;
import org.sdrc.pmr.collection.InstanceACK;
import org.sdrc.pmr.model.AllChecklistFormDataDTO;
import org.sdrc.pmr.model.SectorModel;
import org.sdrc.pmr.model.UserModel;
import org.sdrc.pmr.model.ValueObject;
import org.sdrc.pmr.repository.AllChecklistFormDataRepository;
import org.sdrc.pmr.repository.InstanceACKRepository;
import org.sdrc.pmr.service.DashboradService;
import org.sdrc.pmr.util.OAuth2Utility;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class DashboardController {

	@Autowired
	private DashboradService dashboradService;

	@Autowired
	private AllChecklistFormDataRepository allChecklistFormDataRepository;

	@Autowired
	private InstanceACKRepository instanceACKRepository;

	@Value(value = "${rahat.authserver.url}")
	private String rahatAuthURL;

	@Value(value = "${rahat.save.url}")
	private String rahatSaveURL;

	@Value(value = "${rahat.auth.username}")
	private String rahatAuthUsername;

	@Value(value = "${rahat.auth.password}")
	private String rahatAuthPassword;

	@Value(value = "${rahat.auth.separator}")
	private String rahatAuthSeparator;

	 @Autowired
	 RestTemplate restTemplate;
	 
		@Autowired
		private OAuth2Utility oAuth2Utility; 
		
	@PostMapping("/getDashboardData")
//	 @GetMapping("/bypass/getDashboardData")
	public List<SectorModel> getDashboardData(@RequestParam("areaLevelId") Integer areaLevelId, @RequestParam("areaId") Integer areaId,
			OAuth2Authentication oauth) throws InvalidFormatException, IOException {
		
//		if(areaId==null) {
			UserModel principal = oAuth2Utility.getUserModel();
			areaId = principal.getAreaIds().size() > 1 ?principal.getAreaIds().get(1) : principal.getAreaIds().get(0); 			
//		}
		
//		return dashboradService.getDashboardDataFromCache(areaLevelId, areaId, oauth);
		
		return dashboradService.getDashboardData(areaLevelId, areaId, oauth);
	}
	
	@GetMapping("/bypass/saveInds")
	public String saveInds() throws InvalidFormatException, IOException {
		return dashboradService.pushIndicatorGroupData();
	}
	
	@GetMapping("/bypass/importIndicators")
	public void importIndicators() throws InvalidFormatException, IOException {
		dashboradService.importIndicators();
	}

	@GetMapping("/bypass/aggregate")
	public String aggregate(@RequestParam("areaLevelId") Integer areaLevelId, @RequestParam("areaId") Integer areaId) {
		return dashboradService.aggregate(areaLevelId, areaId);
	}

	@GetMapping("/bypass/saveAll")
	public String hi() {

//		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders requestHeaders = new HttpHeaders();
		try {
			String encoded = Base64.getEncoder()
					.encodeToString((rahatAuthUsername + rahatAuthSeparator + rahatAuthPassword).getBytes("utf-8"));
			requestHeaders.add("Authorization", "Basic " + encoded);
		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
			log.error("Action : while encoding with payload {},{},{}",rahatAuthUsername, rahatAuthPassword, e);
		}
			
		allChecklistFormDataRepository.findByFormIdAndIsValidTrueAndLatestTrue(1).stream().forEach(submittedData -> {
			ResponseEntity<String> restExchange = null;
			try {
				restExchange = restTemplate.exchange(rahatAuthURL, HttpMethod.POST,
						new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<String>() {
						});
			} catch (Exception e) {
				log.error("Action : while authorizing to rahat portal with payload {},{},{}",rahatAuthUsername, rahatAuthPassword, e);
			}
			
//			ResponseEntity<String> restExchange = restTemplate.exchange(rahatAuthURL, HttpMethod.POST,
//					new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<String>() {
//					});

			if (restExchange.getStatusCode().equals(HttpStatus.OK)) {
				String token = restExchange.getHeaders().get("token").get(0);

				HttpHeaders saveHeaders = new HttpHeaders();
				saveHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				saveHeaders.setContentType(MediaType.APPLICATION_JSON);
				saveHeaders.add("Token", token);
				AllChecklistFormDataDTO allChecklistFormDataDTO = new AllChecklistFormDataDTO();

				BeanUtils.copyProperties(submittedData, allChecklistFormDataDTO);

				InstanceACK ack = new InstanceACK();
				ack.setSubmissionId(submittedData.getId());
				ack.setSyncDate(new Date());

				try {
					HttpEntity<AllChecklistFormDataDTO> request = new HttpEntity<AllChecklistFormDataDTO>(
							allChecklistFormDataDTO, saveHeaders);

					ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(rahatSaveURL, request,
							String.class);

					ack.setAckStatusCode(responseEntityStr.getStatusCodeValue());

					if (responseEntityStr.getStatusCodeValue() == 200 || responseEntityStr.getStatusCodeValue() == 300
							|| responseEntityStr.getStatusCodeValue() == 302) {
//						System.out.println("Acknowledge");
						ack.setAck(true);
					}

				} catch (Exception e) {
//					e.printStackTrace();
					log.error("Action : while sending new data to rahat portal with payload {},{},{}",SecurityContextHolder.getContext().getAuthentication().getName(),submittedData,e);
				} finally {
					instanceACKRepository.save(ack);
				}
			}
		});

		return "all submission pushed to rahat portal";
	}
	
	@GetMapping("/bypass/saveOne")
	public String callToRahatPortal() {
		
//		RestTemplate testTemplate = new RestTemplate();
//		String rahatAuthURL = "http://techcellence.biz/undpapi/api/authenticate";
//		String rahatSaveURL = "http://techcellence.biz/undpapi/api/UNDP?mode=SaveCampPersons";
		
		HttpHeaders requestHeaders = new HttpHeaders();
		ResponseEntity<String> restExchange = null;
		
		try {
//			String encoded = Base64.getEncoder().encodeToString("RahatMitra:pwd@rahatmitra#!123".getBytes("utf-8"));
			String encoded = Base64.getEncoder().encodeToString((rahatAuthUsername+rahatAuthSeparator+rahatAuthPassword).getBytes("utf-8"));
			requestHeaders.add("Authorization", "Basic "+ encoded);
			restExchange = restTemplate.exchange(rahatAuthURL, HttpMethod.POST,
					new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<String>() {});
		} 	catch (UnsupportedEncodingException e) {
			System.out.println("status 0");
			e.printStackTrace();
//			instanceACKRepository.save(ack);
			log.error("Action : while authorizing to rahat portal with payload {},{},{}",rahatAuthUsername, rahatAuthPassword, e);
		}
		catch (ResourceAccessException e) {
			System.out.println("status 1");
			e.printStackTrace();
//			ack.setAckStatusCode(1);
//			instanceACKRepository.save(ack);
			log.error("Action : while authorizing to rahat portal with payload {},{},{}",rahatAuthUsername, rahatAuthPassword, e);
		}
		
		
		
		if(restExchange.getStatusCode().equals(HttpStatus.OK)) {
			String token = restExchange.getHeaders().get("token").get(0);
			
			HttpHeaders saveHeaders = new HttpHeaders();
			saveHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			saveHeaders.setContentType(MediaType.APPLICATION_JSON);
			saveHeaders.add("Token", token);
			
			AllChecklistFormData submittedData = allChecklistFormDataRepository.findById("5ec653521dc4a75c30c7aae0");
			AllChecklistFormDataDTO allChecklistFormDataDTO = new AllChecklistFormDataDTO();
			
			BeanUtils.copyProperties(submittedData, allChecklistFormDataDTO);
			
			InstanceACK ack = new InstanceACK();
			ack.setSubmissionId(submittedData.getId());
			ack.setSyncDate(new Date());
			
			try {
				HttpEntity<AllChecklistFormDataDTO> request = new HttpEntity<AllChecklistFormDataDTO>(allChecklistFormDataDTO, saveHeaders);
				
				ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(rahatSaveURL, request, String.class);
			
				ack.setAckStatusCode(responseEntityStr.getStatusCodeValue());
				
				if(responseEntityStr.getStatusCodeValue() == 200 || 
						responseEntityStr.getStatusCodeValue() == 300 ||
							responseEntityStr.getStatusCodeValue() == 302) {
//					System.out.println("Acknowledge");
					ack.setAck(true);
				}
//				else {
//					System.out.println(responseEntityStr);
//					ack.setAck(false);
//				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				instanceACKRepository.save(ack);
			}

		}
		
		return "done";
	}
	
//	@GetMapping("/bypass/choroplethData")
	@PostMapping("/choroplethData")
	public Map<String, ValueObject>  getChoroplethData(@RequestParam("inid") Integer inid){
		return dashboradService.getThematicMapData(inid);
	}
	
//	public Map<String, ValueObject>  getSkillData(@RequestParam("areaId") Integer areaId){
//		return dashboradService.getSkillWiseData(areaId);
//	}
	@GetMapping("/getSkillData")
	public List< ValueObject>  getSkillData( @RequestParam(value="areaId", required=false) Integer areaId){
//		if(areaId==null) {
			UserModel principal = oAuth2Utility.getUserModel();
			areaId = principal.getAreaIds().size() > 1 ?principal.getAreaIds().get(1) : principal.getAreaIds().get(0); 	
//		}
		return dashboradService.getSkillWiseData(areaId);
	}
	@GetMapping(value="/isDataAvailable")
	public boolean isCoveargeDataAvailable() {
		return dashboradService.isDataAvailable();
	}
	
	@GetMapping("/skillChoroplethData")
	public Map<String, ValueObject>  getSkillChoroplethData(@RequestParam("inid") Integer inid){
		return dashboradService.getSkillTypeThematicMapData(inid);
	}
	
	@GetMapping("/stateChoroplethData")
	public Map<String, ValueObject>  getStateChoroplethData(@RequestParam("originStateId") Integer originStateId){
		return dashboradService.getStateThematicMapData(originStateId);
	}
}
