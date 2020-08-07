package org.sdrc.fani.models;

import java.util.List;
import java.util.Map;

import lombok.Data;
@Data
public class PartnerOptionModel {
	private Map<String, Map<String, List<PartnerDetailsModel>>> partnerDetailsModel;
	private Map<String, Map<String,List<Map<String, PartnerDetailsModel>>>> optionalModel;

}
