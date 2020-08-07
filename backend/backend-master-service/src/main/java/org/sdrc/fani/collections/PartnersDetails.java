package org.sdrc.fani.collections;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import in.co.sdrc.sdrcdatacollector.document.Type;
import in.co.sdrc.sdrcdatacollector.models.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PartnersDetails {

	@Id
	private String id;

	private Integer slugId;

	private Integer partnersDetailsId;

	private String section;

	private String subsection;

	private String partnersDetails;

	private String columnName;

	private String controllerType;
	
	private String partern;
	
	private Boolean required;
	
	private String features;

	private String parentColumnName;

	private String fieldType;

	private Type typeId;

	@Indexed
	private Integer questionOrder;

	private String tableName;

	private Boolean active = true;

	private String query;

	private String reviewHeader;

	private String fileExtensions;

	private String scoreExp;
	
	private Map<Language, String> languages = new HashMap<>();
	
	private Boolean optional;
	
	private int maxLenth;

}
