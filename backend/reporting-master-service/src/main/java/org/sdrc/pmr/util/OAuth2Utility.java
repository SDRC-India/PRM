package org.sdrc.pmr.util;
import java.util.Map;

import org.sdrc.pmr.model.UserModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sarita Panigrahi
 * email-sari.panigrahi@gmail.com
 * 24-Jun-2020 3:19:08 PM
 */
@Component
@Slf4j
public class OAuth2Utility {

	@Value(value = "${oauth2.authserver.url}")
	private String authServerURI;

//	@Autowired
//	private RestTemplate restTemplate;

	public UserModel getUserModel() {
		RestTemplate testTemplate = new RestTemplate();
		
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", attr.getRequest().getHeader("Authorization"));

		log.debug("get user By Using Access Token URL: {}", authServerURI + "/me");
		
		ResponseEntity<UserModel> restExchange = testTemplate.exchange(authServerURI + "/me", HttpMethod.GET,
				new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<UserModel>() {});

		return restExchange.getBody();

	}
	
	public Map<String, Integer> userWiseArea() {
		
		RestTemplate testTemplate = new RestTemplate();

		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", attr.getRequest().getHeader("Authorization"));

		log.debug("get user By Using Access Token URL: {}", authServerURI + "/me/userWiseArea");
		
		ResponseEntity<Map<String, Integer>> restExchange = testTemplate.exchange(authServerURI + "/me/userWiseArea", HttpMethod.GET,
				new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<Map<String, Integer>>() {});

		return restExchange.getBody();

	}
	
	
	public  Integer userCount() {
		RestTemplate testTemplate = new RestTemplate();
		
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", attr.getRequest().getHeader("Authorization"));

		log.debug("get user By Using Access Token URL: {}", authServerURI + "/me/userCount");
		
		ResponseEntity< Integer> restExchange = testTemplate.exchange(authServerURI + "/me/userCount", HttpMethod.GET,
				new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<Integer>() {});

		return restExchange.getBody();

	}
	
	
	/*public String fetchInstances() {

		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", attr.getRequest().getHeader("Authorization"));

		ResponseEntity<List<String>> restExchange = restTemplate.exchange("https://testserver.sdrc.co.in:8443/prm/api/v1/data/fetchUnACKInstances",
				HttpMethod.POST,
				new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<List<String>>() {});

		List<String> instances = restExchange.getBody();
		
		instances.forEach(inst -> {
			ResponseEntity<AllChecklistFormDataDTO> instanceExchange = restTemplate.exchange(
					"https://testserver.sdrc.co.in:8443/prm/api/v1/data/fetchSubmission?id="+inst,
					HttpMethod.POST,
					new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<AllChecklistFormDataDTO>() {});
			
			
			System.out.println(instanceExchange.getBody());
		});
		
		return "done";

	}*/
}