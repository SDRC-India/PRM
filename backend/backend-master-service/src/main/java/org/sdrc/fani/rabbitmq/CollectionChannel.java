package org.sdrc.fani.rabbitmq;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface CollectionChannel {

	String PMRSUBMISSIONCHANNEL_OUTPUTCHANNEL = "pmrsubmissionchannel-out";
	@Output(CollectionChannel.PMRSUBMISSIONCHANNEL_OUTPUTCHANNEL)
	MessageChannel dataSubmissionChannel();
	
//	String PMRSUBMISSIONCHANNEL_INPUTCHANNEL = "pmrsubmissionchannel-in";
//	@Input(CollectionChannel.PMRSUBMISSIONCHANNEL_INPUTCHANNEL)
//	SubscribableChannel submissionInputChannel();
}
