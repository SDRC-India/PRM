package org.sdrc.fani.service;

import org.springframework.http.ResponseEntity;

public interface ConfigurationService {

	ResponseEntity<String> importAreas();

	ResponseEntity<String> formsValue();

	String createMongoOauth2Client();

	ResponseEntity<String> config();

	ResponseEntity<String> importFilterExpInTypeDetail();

	ResponseEntity<String> persistIndicatorMapping();

	Boolean configureRoleFormMapping();

	ResponseEntity<String> importDesgPartnerFormMapping();

	ResponseEntity<String> importForms();

	ResponseEntity<String> setUpdatedDate(String ids);

	ResponseEntity<String> importAiimsData();

	void updateEngineForm();
}
