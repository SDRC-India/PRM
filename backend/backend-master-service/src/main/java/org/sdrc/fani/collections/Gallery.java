package org.sdrc.fani.collections;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.sdrc.fani.models.FilepathCaptionModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;
@Document
@Data
@NoArgsConstructor
public class Gallery implements Serializable {
	private static final long serialVersionUID = 1558538811474305739L;

	@Id
	private String id;

	private Integer galleryId;
	private String caption;
	private List<FilepathCaptionModel> filepathCaptionModel;
	private String videoLink;
	private String galleryType;
	private String submittedBy;
	private Date createdDate;
	private Date updatedDate;
	private Boolean isActive;
	private Boolean isApprove;
	private String firstName;

}
