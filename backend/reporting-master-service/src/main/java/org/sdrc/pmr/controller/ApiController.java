package org.sdrc.pmr.controller;

import java.util.List;

import org.sdrc.pmr.model.AllChecklistFormDataDTO;
import org.sdrc.pmr.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RequestMapping("/api/v1/data/")
public class ApiController {

	@Autowired
	private ApiService apiService;

	/*@Value("${oauth.token:http://localhost:8080/covid19rmrs/oauth/token}")
//	@Value("${oauth.token:https://testserver.sdrc.co.in:8443/covid19rmrs-v2/oauth/token}")
	private String tokenUrl;*/

	@Value(value = "${oauth2.authserver.url}")
	private String tokenUrl;
	
	@ApiOperation(value="Access token api for Rahat Mitra ",
			notes = "This is the authentication API for Rahat Mitra. "
			+ "Any external user/application who wants to access authorized information from Rahat Mitra application, shall first request for an "
			+ "access token by providing a valid username and password.",
//			authorizations = { @Authorization(scopes = {
//			@AuthorizationScope(scope = "global", description = "") }, value = "jwt") },
			httpMethod = "POST", produces = "application/json", consumes = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 202, message = "ACCEPTED"),
			@ApiResponse(code = 200, responseContainer = "List", response = String.class, message = "OK"),
			@ApiResponse(code = 401, message = "Unauthorized Error"),
			@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR") })
	@PostMapping(value = "accessToken", produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> fetchToken(@RequestParam String username, @RequestParam String password) {

		ResponseEntity<String> response = null;
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
		map.add("username", username.toLowerCase());
		map.add("password", password.toLowerCase());
		map.add("grant_type", "password");
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, requestHeaders);
		response = restTemplate.exchange(tokenUrl+"/oauth/token", HttpMethod.POST, request, String.class);
		
		if(response!=null && response.getStatusCodeValue() == 200) {
			return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
		}else {
			return response;
		}
		
//		ResponseEntity<String> finalResponse =new ResponseEntity<>(response.getBody(), HttpStatus.OK);
//		return response;
	}

	@ApiOperation(value = "Fetch submission by instance id",
			notes = "This API returns the unacknowledged submission data for a valid instance id."
					+ " This API requires a valid access token and instance id to accept the request and return the submitted data. ",
//			authorizations = { @Authorization(scopes = {
//			@AuthorizationScope(scope = "global", description = "") }, value = "jwt") },
			httpMethod = "POST", produces = "application/json", consumes = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 202, message = "ACCEPTED"),
			@ApiResponse(code = 200, responseContainer = "List", response = AllChecklistFormDataDTO.class, message = "OK"),
			@ApiResponse(code = 401, message = "Unauthorized Error"),
			@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR") })
	@PostMapping(value = "fetchSubmission", produces = "application/json")
	@PreAuthorize("hasAuthority('API_ACCESS')")
	@ResponseBody
	public ResponseEntity<AllChecklistFormDataDTO> fetchSubmission(@RequestParam String id) {
		return new ResponseEntity<AllChecklistFormDataDTO>(apiService.fetchSubmissionDetail(id), HttpStatus.OK);
	}

	@ApiOperation(value = "Fetch all un-acknowledged submission ids ",
			notes = "This API shall provide a list of instance ids (submissions) which were not acknowledge by the extenal application during the submission "
					+ "process in Raha Mitra application. This API requires a valid access token to accept the request and returns a list of "
					+ "available instance ids.",
//			authorizations = { @Authorization(scopes = {
//			@AuthorizationScope(scope = "global", description = "") }, value = "jwt") }, 
			httpMethod = "POST", produces = "application/json", consumes = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 202, message = "ACCEPTED"),
			@ApiResponse(code = 200, responseContainer = "List", response = String.class, message = "OK"),
			@ApiResponse(code = 401, message = "Unauthorized Error"),
			@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR") })
	@PostMapping(value = "fetchUnACKInstances", produces = "application/json")
	@PreAuthorize("hasAuthority('API_ACCESS')")
	@ResponseBody
	public ResponseEntity<List<String>> fetchUnACKInstances() {
		return new ResponseEntity<List<String>>(apiService.getAllUnAckInstances(), HttpStatus.OK);
	}
}
