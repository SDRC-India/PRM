package org.sdrc.pmr.service;

import java.util.Date;
import java.util.List;

import org.sdrc.pmr.collection.AllChecklistFormData;
import org.sdrc.pmr.collection.TimePeriod;
import org.sdrc.pmr.rabbitmq.CollectionChannel;
import org.sdrc.pmr.repository.AllChecklistFormDataRepository;
import org.sdrc.pmr.repository.TimePeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sarita Panigrahi
 *	This service consumes new data entry collection service 
 */
@Service
@Slf4j
public class DataEntryConsumer {


	@Autowired
	private AllChecklistFormDataRepository allChecklistFormDataRepository;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private CollectionChannel collectionChannel;
	
	@StreamListener(value = CollectionChannel.PMRSUBMISSIONCHANNEL_INPUTCHANNEL)
	public void persistSubmissionToDb(AllChecklistFormData dataSubmit) {
		try {
			
			if(dataSubmit.getData().get("undp_f1_q0") != null) {

				TimePeriod lastTp = timePeriodRepository.findTop1ByPeriodicityOrderByTimePeriodIdDesc("0");
				dataSubmit.setTimePeriod(lastTp);
				
				/**
				 * check for same unique id and formid whether any submission is exist which is fresh data that is reject=false
				 * if exist than make the previous data as invalid 
				 */
				List<AllChecklistFormData> dupData = allChecklistFormDataRepository.findByFormIdAndUniqueIdAndRejectedFalse(dataSubmit.getFormId(),dataSubmit.getUniqueId());
				
				if(dupData!=null && !dupData.isEmpty()) {
					
					dataSubmit.setUpdatedDate(new Date()); // when user edits the record
					dupData.forEach(d->d.setIsValid(false));
					allChecklistFormDataRepository.save(dupData);
				}
				AllChecklistFormData submittedData = allChecklistFormDataRepository.save(dataSubmit);
				
				//publish the data in rabbitmq... for rahat portal push api
//				collectionChannel.rahatSubmissionChannel().send(MessageBuilder.withPayload(submittedData).build());	
			}
			
		} catch (Exception e) {
			log.error("Failed to send payload to dashboard service with payload : {}", dataSubmit, e);
			throw new RuntimeException("custom error", e);
		}
	}

}
