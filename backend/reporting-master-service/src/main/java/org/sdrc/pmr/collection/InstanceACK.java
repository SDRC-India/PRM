package org.sdrc.pmr.collection;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class InstanceACK {
	@Id
	private String id;
	private String submissionId;
	private Boolean ack = false;
	private Date syncDate;
	private Date lastUpdatedDate;
	private int ackStatusCode;
}
