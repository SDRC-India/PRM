package org.sdrc.fani.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionModel implements Serializable{

	
	private static final long serialVersionUID = -140367218856854570L;
	

	private Integer key;

	private String value;

	private Integer order;

	private Boolean isSelected;

	private Integer parentId;

	private List<Integer> parentIds;

	private Integer parentId2;

	private Integer level;

	private boolean visible = true;

	private String score;

	private Map<String, String> autoPopulateValue;

	private String filterByExp;
	
	private Map<String, Object> extraKeyMap;
}
