package org.sdrc.fani.util;

import java.util.List;
import java.util.Map;

import lombok.Data;
@Data
public class Mail {

	private String fromUserName;

	private String toUserName;

	private List<String> toEmailIds;

	private List<String> ccEmailIds;

	private String subject;

	private String message;

	private Map<String, String> attachments;
	
	private String email;

}
