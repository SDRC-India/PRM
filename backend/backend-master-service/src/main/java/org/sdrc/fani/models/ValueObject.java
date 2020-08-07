package org.sdrc.fani.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@Data
@ToString
public class ValueObject implements Serializable{


	private static final long serialVersionUID = -4445121928859038308L;
	
	private Integer formId;
	private List<String> rejectionList;
	private String message;
	//this property define whether the the data is to be rejected or deleted 
	private List<String> isDelete;
	
	private Boolean isRejected;
	private String key;
	private Object value;
	private String description;
	private String groupName;
	private String shortNmae;
	private Boolean isSelected;
	private Integer id;
	private String keyValue;
	private Integer count;
	
	Map<String, List<String>> chartMap;
	Map<String, List<String>> legendsMap;
}
