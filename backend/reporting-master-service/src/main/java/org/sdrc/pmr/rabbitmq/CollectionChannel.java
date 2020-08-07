package org.sdrc.pmr.rabbitmq;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author Sarita Panigrahi
 *
 */
public interface CollectionChannel {
	String PMRSUBMISSIONCHANNEL_INPUTCHANNEL = "pmrsubmissionchannel-in";
	@Input(CollectionChannel.PMRSUBMISSIONCHANNEL_INPUTCHANNEL)
	SubscribableChannel submissionInputChannel();
	
	//this channel is responsible to publish data in rahat queue .. for API call to rahat portal
	String RAHATSUBMISSIONCHANNEL_OUTPUTCHANNEL = "rahatsubmissionchannel-out";
	@Output(CollectionChannel.RAHATSUBMISSIONCHANNEL_OUTPUTCHANNEL)
	MessageChannel rahatSubmissionChannel();
	
	//this channel will consume the message from rahat portal queue
	String RAHATSUBMISSIONCHANNEL_INPUTCHANNEL = "rahatsubmissionchannel-in";
	@Input(CollectionChannel.RAHATSUBMISSIONCHANNEL_INPUTCHANNEL)
	SubscribableChannel rahatInputChannel();
}
