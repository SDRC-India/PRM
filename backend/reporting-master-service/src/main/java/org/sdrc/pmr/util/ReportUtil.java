package org.sdrc.pmr.util;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sdrc.pmr.collection.AllChecklistFormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Sarita Panigrahi
 * email-sari.panigrahi@gmail.com
 * 16-Jun-2020 3:19:08 PM
 */
@Component
@Slf4j
public class ReportUtil {

	@Autowired
	private MongoTemplate mongoTemplate;
	

	
	@Cacheable(value="districtmap",key="{ #projectionValue, #districtCol, #districtid }", condition = "{#startdateDate == null, #enddateDate == null}")	
	public List<Map> getDistrictMap(String projectionValue,Date startdateDate,Date enddateDate, String districtCol, List<Integer> districtid) {
		System.out.println("districtmap not cached");
		MatchOperation match = null;
		if(districtCol == null && startdateDate != null && enddateDate != null) {
			match = Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1).and("syncDate").gte(startdateDate).lte(enddateDate));
		}else if(districtCol != null && startdateDate != null && enddateDate != null)  {
			match = Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1).and("data."+districtCol).in(districtid).and("syncDate").gte(startdateDate).lte(enddateDate));
		}else if(districtCol == null && startdateDate == null && enddateDate == null) {
			match = Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1));			
		}else if(districtCol != null && startdateDate == null && enddateDate == null) {
			match = Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1).and("data."+districtCol).in(districtid));
			
		}
		ProjectionOperation dataProject = Aggregation.project("data");

		ProjectionOperation projection = Aggregation.project().and("data."+projectionValue).as("area")
				.and(when(where("data.undp_f1_q0").is(1)).then(1).otherwise(0)).as("Shelter")
				.and(when(where("data.undp_f1_q0").is(2)).then(1).otherwise(0)).as("Transit")
				.and(when(where("data.undp_f1_q0").is(3)).then(1).otherwise(0)).as("Individual");

		GroupOperation group = Aggregation.group("area").sum("Shelter").as("Shelter").sum("Transit").as("Transit")
				.sum("Individual").as("Individual");

		LookupOperation lookup = Aggregation.lookup("area", "_id", "areaId", "area");
		UnwindOperation unwind = Aggregation.unwind("area");

		ProjectionOperation projectParentArea = Aggregation.project().and("area.areaName").as("area")
				.and("area.areaLevel.areaLevelName").as("areaLevel").and("Shelter").as("Shelter").and("Transit")
				.as("Transit").and("Individual").as("Individual");

		Aggregation resultQuery = Aggregation.newAggregation(match, dataProject, projection, group, lookup, unwind,
				projectParentArea);

		List<Map> reportMap = mongoTemplate.aggregate(resultQuery, AllChecklistFormData.class, Map.class)
				.getMappedResults();
		return reportMap;
	}
	
}
