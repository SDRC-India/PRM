package org.sdrc.fani.service;

import org.sdrc.fani.models.PartnerOptionModel;

import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;

public interface CollectionService {

	String updateArea();

	String saveDate() throws Exception;

	String saveTimePeriod();

	String saveDevelopmentPartners();
	
	String importPartnerDetails();
	
	PartnerOptionModel getPartnerDetails();
	
	String savePartner(ReceiveEventModel receiveEventModel);

}
