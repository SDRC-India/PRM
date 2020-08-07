package org.sdrc.fani.datacollector.implhandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.sdrc.fani.collections.AllChecklistFormData;
import org.sdrc.fani.repositories.AllChecklistFormDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.handlers.IDbFetchDataHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.RawDataModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;

/**
 * @author Subham Ashish (subham@sdrc.co.in)
 */
@Component
public class IDbFetchDataHandlerImpl implements IDbFetchDataHandler {

	@Autowired
	private AllChecklistFormDataRepository allChecklistFormDataRepository;

	@Autowired
	private EngineFormRepository engineFormRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<DataModel> fetchDataFromDb(EnginesForm engineForm, String type, Map<Integer, String> arg2,
			Date startDate, Date endDate, Map<String, Object> paramKeyValMap, HttpSession session, Object user) {

		List<DataModel> submissionDatas = new ArrayList<>();

		// @formatter:off
		List<?> entriesList = "dataReview".equals(type)
				? (mongoTemplate
						.aggregate(
								getAggregationResults(engineForm.getFormId(),
										"dataReview" , startDate, endDate ,user),
								AllChecklistFormData.class, AllChecklistFormData.class)
						.getMappedResults())
				: (mongoTemplate
						.aggregate(getAggregationResults(engineForm.getFormId(), "rejectedData",startDate,endDate,user),
								AllChecklistFormData.class, AllChecklistFormData.class)
						.getMappedResults().stream().filter(value -> value.isRejected() == true)
						.collect(Collectors.toList()));

		// @formatter:on

		if (entriesList != null && !entriesList.isEmpty()) {
			for (Object entry : entriesList) {
				if (entry instanceof AllChecklistFormData) {
					AllChecklistFormData data = (AllChecklistFormData) entry;

					Map<String, Object> extraKeys = new HashMap<>();

					if (data.getRejectMessage() != null) {

						extraKeys.put("rejectMessage", data.getRejectMessage());
						// if (data.getActionBy() == null) {
						// extraKeys.put("rejectedBy", " : Auto rejected by
						// system");
						// } else
						// extraKeys.put("rejectedBy", " : Rejected by "+
						// ((UserDetails)
						// data.getActionBy().getUserDetails()).getFirstName() +
						// " "
						// + ((UserDetails)
						// data.getActionBy().getUserDetails()).getLastName());
					}

					if (data.getRejectedApprovedDate() != null)
						extraKeys.put("actionDate", data.getRejectedApprovedDate());

					extraKeys.put("submissionId", data.getId());
					extraKeys.put("status", data.getChecklistSubmissionStatus());
					extraKeys.put("syncDate", data.getSyncDate());
					extraKeys.put("rejectedDate", data.getRejectedDate());

					DataModel model = new DataModel();
					model.setId(data.getId());
					model.setData(data.getData());
					model.setFormId(data.getFormId());
					model.setRejected(data.isRejected());
					model.setUniqueId(data.getUniqueId());
					model.setUniqueName(data.getUniqueName());
					model.setUpdatedDate(data.getUpdatedDate());
					model.setUserId(data.getUserId());
					model.setUserName(data.getUserName());
					model.setCreatedDate(data.getCreatedDate());
					model.setFormVersion(engineForm.getVersion());
					model.setExtraKeys(extraKeys);
					model.setAttachments(data.getAttachments());
					model.setRejectedDate(data.getRejectedDate());

					submissionDatas.add(model);
				}
			}
		}

		return submissionDatas;
	}

	private Aggregation getAggregationResults(Integer formId, String type, Date startDate, Date endDate, Object user) {

		MatchOperation match = null;

		String userName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		/**
		 * if admin user than all the forms rejected data is visible
		 */
		// @formatter:off
		if(userName.equalsIgnoreCase("admin")){
			
			if ("rejectedData".equals(type)) {
				
				match = Aggregation.match(Criteria.where("formId").is(formId)
						.and("isValid").is(true)
						.and("isAggregated").is(false)
						.and("checklistSubmissionStatus").is("COMPLETE"));
			}
		}else{
			
			if ("rejectedData".equals(type)) {
				
				match = Aggregation.match(Criteria.where("formId").is(formId)
						.and("isValid").is(true)
						.and("isAggregated").is(false)
						.and("userName").is(userName)
						.and("checklistSubmissionStatus").is("COMPLETE"));
			}
		}
		
		SortOperation sort = Aggregation
				.sort(Sort.Direction.ASC, "userId")
				.and(Sort.Direction.ASC, "formId")
				.and(Sort.Direction.DESC, "uniqueId")
				.and(Sort.Direction.ASC, "syncDate");

		
		if ("dataReview".equals(type)) {
			
			SortOperation sortreviewData = Aggregation
					.sort(Sort.Direction.DESC, "uniqueId")
					.and(Sort.Direction.DESC,"syncDate");

			match = Aggregation.match(
						Criteria.where("formId").is(formId)
						.and("isValid").is(true)
						.and("isAggregated").is(false)
						.and("checklistSubmissionStatus").is("COMPLETE"));

			GroupOperation groupReview = Aggregation.group("uniqueId")
					.push("$$ROOT").as("submissions");
			
			
			AggregationOperation replaceRoot = Aggregation.replaceRoot()
					.withValueOf(ArrayOperators.ArrayElemAt.arrayOf("submissions").elementAt(0));
					
			return Aggregation.newAggregation(match, sortreviewData, groupReview, replaceRoot);
		}else{
			
			GroupOperation group = Aggregation.group("uniqueId")
					.last("syncDate").as("syncDate")
					.last("submittedBy").as("submittedBy")
					.last("data").as("data")
					.last("userName").as("userName")
					.last("userId").as("userId")
					.last("createdDate").as("createdDate")
					.last("updatedDate").as("updatedDate")
					.last("formId").as("formId")
					.last("uniqueId").as("uniqueId")
					.last("uniqueName").as("uniqueName")
					.last("rejected").as("rejected")
					.last("rejectMessage").as("rejectMessage")
					.last("isAggregated").as("isAggregated")
					.last("attachmentCount").as("attachmentCount")
					.last("attachments").as("attachments")
					.last("isValid").as("isValid")
					.last("timePeriod").as("timePeriod")
					.last("checklistSubmissionStatus").as("checklistSubmissionStatus")
					.last("id").as("id")
					.last("rejectedBy").as("rejectedBy")
					.last("rejectedDate").as("rejectedDate")
					.last("version").as("version")
					.last("latest").as("latest")
					.last("rejectedApprovedDate").as("rejectedApprovedDate");
			
			return Aggregation.newAggregation(match, group, sort);
		}
		
		// @formatter:on
	}

	@Override
	public RawDataModel findAllByRejectedFalseAndSyncDateBetween(Integer formId, Date startDate, Date endDate) {

		return null;
	}

	@Override
	public DataModel getSubmittedData(String submissionId, Integer formId) {

		AllChecklistFormData submittedData = allChecklistFormDataRepository.findByIdAndFormId(submissionId, formId);
		EnginesForm form = engineFormRepository.findByFormId(submittedData.getFormId());

		DataModel model = new DataModel();

		model.setAttachments(submittedData.getAttachments());
		model.setCreatedDate(submittedData.getCreatedDate());
		model.setData(submittedData.getData());
		model.setFormId(submittedData.getFormId());
		model.setFormVersion(form.getVersion());
		model.setId(submittedData.getId());
		model.setRejected(submittedData.isRejected());
		model.setUniqueId(submittedData.getUniqueId());
		model.setUpdatedDate(submittedData.getUpdatedDate());
		model.setUserId(submittedData.getUserId());
		model.setUserName(submittedData.getUserName());

		return model;

	}

}
