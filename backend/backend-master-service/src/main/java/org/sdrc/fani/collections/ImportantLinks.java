package org.sdrc.fani.collections;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
public class ImportantLinks implements Serializable {
	private static final long serialVersionUID = 1558538811474305739L;

	@Id
	private String id;
	private Integer importantLinksId;
	private String title;
	private String url;
	private String submittedBy;
	private Date createdDate;
	private Date updatedDate;
	private Boolean isActive;
	private Boolean isApprove;
	private String firstName;

}