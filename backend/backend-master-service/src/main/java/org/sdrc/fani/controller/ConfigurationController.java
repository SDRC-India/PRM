package org.sdrc.fani.controller;

import org.sdrc.fani.models.PartnerOptionModel;
import org.sdrc.fani.service.CollectionService;
import org.sdrc.fani.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.co.sdrc.sdrcdatacollector.engine.UploadFormConfigurationService;
import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;

/**
 * @author Debiprasad Parida (debiprasad@sdrc.co.in)
 */

@RestController
@RequestMapping("/api")
public class ConfigurationController {

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private UploadFormConfigurationService uploadFormConfigurationService;

	@Autowired
	CollectionService collectionService;

	@GetMapping("/mongoClient")
	public String createMongoOauth2Client() {

		return configurationService.createMongoOauth2Client();

	}

	@GetMapping("/importQuestions")
	
	public ResponseEntity<String> importQuestions() {
		ResponseEntity<String> msg = uploadFormConfigurationService.importQuestionData();
		if(msg.getBody().equals("successfull")) {
			configurationService.updateEngineForm();
		}
		return msg;
	}

	@GetMapping("/area")
	public ResponseEntity<String> area() {
		return configurationService.importAreas();
	}

	@GetMapping("/config")
	public ResponseEntity<String> config() {
		return configurationService.config();
	}

	@GetMapping("/configureRoleFormMappingOfEngine2")
	public Boolean configureRoleFormMappingOfEngine() {
		configurationService.configureRoleFormMapping();
		return true;
	}

	@GetMapping("/formsValue")
	public ResponseEntity<String> formsValue() {
		return configurationService.formsValue();
	}

	@RequestMapping("/importOrganization")
	public String importOrganization() throws Exception {
		return collectionService.saveDate();

	}

	@RequestMapping("/importMonthlyTimeperiod")
	public String importTimeperiod() {
		return collectionService.saveTimePeriod();

	}

	@RequestMapping("/importDevelopmentPartners")
	public String importDevelopmentPartners() throws Exception {
		return collectionService.saveDevelopmentPartners();

	}

	@GetMapping("/importPartnerDetails")
	public String importPartnerDetails() {
		return collectionService.importPartnerDetails();
	}

	@GetMapping("/getPartnerDetails")
	public PartnerOptionModel getPartnerDetails() {
		return collectionService.getPartnerDetails();
	}

	@RequestMapping(value = "/savePartner", method = { RequestMethod.POST, RequestMethod.OPTIONS })
	public String savePartner(@RequestBody ReceiveEventModel receiveEventModel) {
		return collectionService.savePartner(receiveEventModel);
	}
	
	@GetMapping("/importDesgPartnerFormMapping")
	public ResponseEntity<String> importDesgPartnerFormMapping(){
		return configurationService.importDesgPartnerFormMapping();
	}
	
	@GetMapping("/importForms")
	public ResponseEntity<String> importForms(){
		return configurationService.importForms();
	}
	@PostMapping("/setUpdatedDate")
	public ResponseEntity<String> setUpdatedDate(@RequestParam String ids){
		return configurationService.setUpdatedDate(ids);
	}
	
	@GetMapping("/importAiimsData")
	public ResponseEntity<String> importAiimsData(){
		return configurationService.importAiimsData();
	}
}
