package org.sdrc.pmr.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.sdrc.pmr.collection.AllChecklistFormData;
import org.sdrc.pmr.collection.InstanceACK;
import org.sdrc.pmr.model.AllChecklistFormDataDTO;
import org.sdrc.pmr.rabbitmq.CollectionChannel;
import org.sdrc.pmr.repository.AllChecklistFormDataRepository;
import org.sdrc.pmr.repository.InstanceACKRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiServiceImpl implements ApiService {

	
	@Autowired
	private InstanceACKRepository instanceACKRepository;
	
	@Autowired
	private AllChecklistFormDataRepository allChecklistFormDataRepository;
	
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
	
	@StreamListener(value = CollectionChannel.RAHATSUBMISSIONCHANNEL_INPUTCHANNEL)
	public void callToRahatPortal(AllChecklistFormData submittedData) {
		
		HttpHeaders requestHeaders = new HttpHeaders();
		ResponseEntity<String> restExchange = null;
		InstanceACK ack = new InstanceACK();
		try {
			ack.setSubmissionId(submittedData.getId());
			ack.setSyncDate(new Date());
			
			String encoded = Base64.getEncoder().encodeToString((rahatAuthUsername+rahatAuthSeparator+rahatAuthPassword).getBytes("utf-8"));
			requestHeaders.add("Authorization", "Basic "+ encoded);
			restExchange = restTemplate.exchange(rahatAuthURL, HttpMethod.POST,
					new HttpEntity<Void>(requestHeaders), new ParameterizedTypeReference<String>() {});
		
		}catch (UnsupportedEncodingException e) {
			log.error("Action : while authorizing to rahat portal with payload {},{},{}",rahatAuthUsername, rahatAuthPassword, e);
		}
		catch (ResourceAccessException e) {
			ack.setAckStatusCode(1);
			log.error("Action : while connecting to rahat portal with payload {},{},{}",rahatAuthUsername, rahatAuthPassword, e);
		}catch (Exception e) {
			log.error("Action : while connecting to rahat portal with payload {},{},{}",rahatAuthUsername, rahatAuthPassword, e);
		}
		
		if(restExchange!=null && restExchange.getStatusCode().equals(HttpStatus.OK)) {
			
			String token = restExchange.getHeaders().get("token").get(0);
			
			HttpHeaders saveHeaders = new HttpHeaders();
			saveHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			saveHeaders.setContentType(MediaType.APPLICATION_JSON);
			saveHeaders.add("Token", token);
			
			try {
				
				AllChecklistFormDataDTO allChecklistFormDataDTO = new AllChecklistFormDataDTO();
				BeanUtils.copyProperties(submittedData, allChecklistFormDataDTO);
				
				HttpEntity<AllChecklistFormDataDTO> request = new HttpEntity<AllChecklistFormDataDTO>(allChecklistFormDataDTO, saveHeaders);
				
				ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(rahatSaveURL, request, String.class);
			
				ack.setAckStatusCode(responseEntityStr.getStatusCodeValue());
				
				if(responseEntityStr.getStatusCodeValue() == 200 || 
						responseEntityStr.getStatusCodeValue() == 300 ||
							responseEntityStr.getStatusCodeValue() == 302) {
					ack.setAck(true);
				}
				
			} 
			catch (ResourceAccessException e) {
				ack.setAckStatusCode(1);
				log.error("Action : Read time out while sending data to rahat portal with payload {},{}",submittedData,e);
			}
			catch (Exception e) {
				log.error("Action : while sending new data to rahat portal with payload {},{}",submittedData,e);
			}
			finally {
				instanceACKRepository.save(ack);
			}

		}else {
			log.info("-----------Null response received due to authentication failure---------");
			instanceACKRepository.save(ack);
		}
		
	}
	
	@Override
	public List<String> getAllUnAckInstances(){
		List<String> instances = new ArrayList<String>();
		instanceACKRepository.findByAckFalse().forEach(inst -> instances.add(inst.getSubmissionId()));
		return instances;
	}
	@Override
	public AllChecklistFormDataDTO fetchSubmissionDetail(String submissionId) {

		AllChecklistFormData submittedData = allChecklistFormDataRepository.findById(submissionId);
			
			if(submittedData!=null) {
				try {
					AllChecklistFormDataDTO allChecklistFormDataDTO = new AllChecklistFormDataDTO();
					BeanUtils.copyProperties(submittedData, allChecklistFormDataDTO);
					InstanceACK ack = instanceACKRepository.findBySubmissionId(submissionId);
					ack.setAck(true);
					ack.setLastUpdatedDate(new Date());
					instanceACKRepository.save(ack);
					return allChecklistFormDataDTO;
				} catch (Exception e) {
					log.error("Error occurred for id  {},{}", submissionId, e);
					throw new RuntimeException("Error occured: ", e);
				}
				
			}else {
				log.error("No submission found for id  {}", submissionId);
				throw new RuntimeException("No submission found for submissionId: "+ submissionId);
			}
			
	}
	
	
}
