package org.sdrc.fani.models;

import java.util.List;

import lombok.Data;

@Data
public class PartnerDetailsModel {

	private Integer key;
	private String label;
	private String type;
	private List<OptionModel> options;
	private Integer typeId;
	private String controlType;
	private Object value;
	private String columnName;
	private Boolean required;
	private Boolean selectAllOption;
	private Boolean allChecked;
	private String dependentCondition;
	private String parentColumnName;
	private Integer partnerOrder;
	private String pattern;
	private int maxLength;
}
