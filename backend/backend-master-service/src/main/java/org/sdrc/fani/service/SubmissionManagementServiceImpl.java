package org.sdrc.fani.service;

import static java.util.Comparator.comparing;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.sdrc.fani.collections.AllChecklistFormData;
import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.AreaLevel;
import org.sdrc.fani.collections.CollectionAudit;
import org.sdrc.fani.collections.DesignationPartnerFormMapping;
import org.sdrc.fani.models.AreaModel;
import org.sdrc.fani.models.UpdateAreaModel;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.models.ValueObject;
import org.sdrc.fani.repositories.AllChecklistFormDataRepository;
import org.sdrc.fani.repositories.AreaLevelRepository;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.CollectionAuditRepository;
import org.sdrc.fani.repositories.DesignationPartnerFormMappingRepository;
import org.sdrc.fani.repositories.EnginesFormRepository;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.engine.FormsServiceImpl;
import in.co.sdrc.sdrcdatacollector.handlers.ICameraAndAttachmentsDataHandler;
import in.co.sdrc.sdrcdatacollector.handlers.IDbFetchDataHandler;
import in.co.sdrc.sdrcdatacollector.handlers.IDbReviewQueryHandler;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.DataObject;
import in.co.sdrc.sdrcdatacollector.models.OptionModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.QuestionRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.util.EngineUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author subham
 *
 */
@Service
@Slf4j
public class SubmissionManagementServiceImpl implements SubmissionManagementService {

	private final String BEGIN_REPEAT = "beginrepeat";

	@Autowired
	private AllChecklistFormDataRepository allChecklistFormDataRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;

	@Autowired
	private IDbFetchDataHandler iDbFetchDataHandler;

	@Autowired
	private EngineFormRepository engineFormRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private IDbReviewQueryHandler iDbReviewQueryHandler;

	@Autowired
	private TypeDetailRepository typeDetailRepository;

	@Autowired
	private EngineUtils engineUtils;

	@Autowired
	private FormsServiceImpl formsServiceImpl;

	@Autowired
	private DesignationRepository designationRepository;

	@Autowired
	private ICameraAndAttachmentsDataHandler iCameraDataHandler;

	@Autowired
	private EnginesFormRepository enginesFormRepository;

	@Autowired
	private DesignationPartnerFormMappingRepository designationPartnerFormMappingRepository;
	
	@Autowired
	private AreaRepository areaRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired 
	AreaLevelRepository areaLevelRepository;
	
	@Autowired
	private CollectionAuditRepository collectionAuditRepository;

	private SimpleDateFormat sdfDateTimeWithSeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private DateFormat ymdDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public ResponseEntity<String> rejectSubmissions(ValueObject valueObject, OAuth2Authentication auth) {

		try {
			UserModel user = tokenInfoExtracter.getUserModelInfo(auth);

			Account acc = accountRepository.findById(user.getUserId());

			Gson gson = new Gson();

			List<AllChecklistFormData> dataSubmit = allChecklistFormDataRepository
					.findByIdIn(valueObject.getRejectionList());
			// for rejection isValis is true and isRejected true
			for (int i = 0; i < dataSubmit.size(); i++) {
				AllChecklistFormData data = dataSubmit.get(i);
				data.setRejected(true);
				data.setRejectedDate(new Date());
				data.setRejectMessage(valueObject.getMessage());
				data.setRejectedBy(acc);

				// for deleted data isRejected is true and isValid is false
				if (valueObject.getIsDelete().get(i).equals("delete")) {
					data.setIsValid(false);
				}
			}

			allChecklistFormDataRepository.save(dataSubmit);
			log.info("Action : Rejection of data successfull with payload {}", valueObject);
			return new ResponseEntity<String>(
					gson.toJson(valueObject.getRejectionList().size() + " no. of record(s) rejected successfully."),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Action : Rejection of data with payload {}", valueObject);
			throw new RuntimeException(e);
		}

	}

	@Override
	public List<DataObject> getReiewDataHead(Integer formId, UserModel user, Map<String, Object> paramKeyValMap) {

		EnginesForm form = engineFormRepository.findByFormId(formId);

		List<TypeDetail> typeDetails = typeDetailRepository.findByFormId(form.getFormId());

		Map<Integer, TypeDetail> typeDetailsMap = typeDetails.stream()
				.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));

		List<DataModel> submissionDatas = iDbFetchDataHandler.fetchDataFromDb(form, "dataReview", null, new Date(),
				new Date(), paramKeyValMap, null, user);

		List<DataObject> dataObjects = new ArrayList<>();

		for (DataModel submissionData : submissionDatas) {

			DataObject dataObject = new DataObject();
			dataObject.setFormId(submissionData.getFormId());

			System.out.println(submissionData.getId());
			if (submissionData.isRejected()) {
				dataObject.setTime(new Timestamp(submissionData.getExtraKeys().get("rejectedDate") == null ? null
						: ((Date) submissionData.getExtraKeys().get("rejectedDate")).getTime()));
			} else {
				dataObject.setTime(new Timestamp(((Date) submissionData.getExtraKeys().get("syncDate")).getTime()));
			}
			dataObject.setUsername(submissionData.getUserName());
			dataObject.setExtraKeys(submissionData.getExtraKeys());
			dataObject.setCreatedDate(sdfDateTimeWithSeconds.format(submissionData.getCreatedDate()));
			dataObject.setUpdatedDate(sdfDateTimeWithSeconds.format(submissionData.getUpdatedDate()));
			dataObject.setUniqueId(submissionData.getUniqueId());
			dataObject.setRejected(submissionData.isRejected());
			dataObject.setUniqueName(submissionData.getUniqueName());
			dataObject.setRejectedDate(submissionData.getRejectedDate() == null ? null
					: sdfDateTimeWithSeconds.format(submissionData.getRejectedDate()));
			Map<String, Object> extraKeys = dataObject.getExtraKeys();
			if (submissionData.isRejected()) {
				extraKeys.put("isRejectable", false);
			} else {
				extraKeys.put("isRejectable", true);
			}

			List<Question> questionList = questionRepository
					.findAllByFormIdAndFormVersionAndActiveTrueOrderByQuestionOrderAsc(form.getFormId(),
							submissionData.getFormVersion());

			for (Question question : questionList) {
				iDbReviewQueryHandler.setReviewHeaders(dataObject, question, typeDetailsMap, submissionData,
						"dataReview");
			}

			dataObjects.add(dataObject);
		}

		// setRejectedData(dataObjects);

		/**
		 * SUBMITTED TAB DATA-- SORTING IS BASED ON syncDate
		 * 
		 * REJECTED TAB DATA -- SORTING IS BASED ON rejectionDate
		 */
		List<DataObject> submittedTabData = new ArrayList<>();
		List<DataObject> rejectedTabData = new ArrayList<>();

		List<DataObject> datasz = new ArrayList<>();

		submittedTabData = dataObjects.stream().filter(v -> v.getRejected() == false).collect(Collectors.toList());

		rejectedTabData = dataObjects.stream().filter(v -> v.getRejected() == true).collect(Collectors.toList());

		Collections.sort(submittedTabData, comparing(DataObject::getTime).reversed());
		Collections.sort(rejectedTabData, comparing(DataObject::getTime).reversed());

		datasz.addAll(submittedTabData);
		datasz.addAll(rejectedTabData);

		return datasz;
	}

	@Override
	public Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(Integer formId, UserModel user,
			String submissionId, Map<String, Object> paramKeyValMap, HttpSession session) {

		DataModel submissionData = iDbFetchDataHandler.getSubmittedData(submissionId, formId);

		EnginesForm form = engineFormRepository.findByFormId(formId);

		List<TypeDetail> typeDetails = typeDetailRepository.findByFormId(form.getFormId());

		Map<Integer, TypeDetail> typeDetailsMap = typeDetails.stream()
				.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));

		Map<String, List<Map<String, List<QuestionModel>>>> mapOfSectionSubsectionListOfQuestionModel = new LinkedHashMap<>();

		List<QuestionModel> listOfQuestionModel = new LinkedList<>();

		Map<String, Map<String, List<QuestionModel>>> sectionMap = new LinkedHashMap<String, Map<String, List<QuestionModel>>>();
		Map<String, List<QuestionModel>> subsectionMap = null;

		/**
		 * for accordion
		 */

		QuestionModel questionModel = null;

		List<Question> questionList = questionRepository
				.findAllByFormIdAndFormVersionAndActiveTrueOrderByQuestionOrderAsc(form.getFormId(),
						submissionData.getFormVersion());

		Map<String, Question> questionMap = questionList.stream()
				.collect(Collectors.toMap(Question::getColumnName, question -> question));

		for (Question question : questionList) {

			questionModel = null;
			switch (question.getControllerType()) {
			case "Date Widget":
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
					questionModel = engineUtils.prepareQuestionModel(question);
					if (submissionData.getData().get(question.getColumnName()) instanceof Date) {
						questionModel
								.setValue(ymdDateFormat.format(submissionData.getData().get(question.getColumnName())));
					} else {
						if (String.class.cast(submissionData.getData().get(question.getColumnName())) != null) {
							String dt = formsServiceImpl.getDateFromString(
									String.class.cast(submissionData.getData().get(question.getColumnName())));
							questionModel.setValue(dt);
						} else
							questionModel.setValue(null);
					}

				}
				break;
			case "Time Widget":
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
					questionModel = engineUtils.prepareQuestionModel(question);
					questionModel.setValue(String.class.cast(submissionData.getData().get(question.getColumnName())));
				}
				break;
				
			case "autoCompleteMulti":
				
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
					questionModel = engineUtils.prepareQuestionModel(question);

					questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
							null, user, paramKeyValMap, session);
					List<Map<String,Object>> autoCompleteMultiValues = (List<Map<String, Object>>) submissionData.getData().get(question.getColumnName());
					String mutiValue = autoCompleteMultiValues.stream()
		                       .map(v->v.get("value").toString())
		                       .collect(Collectors.joining(", "));
					
					questionModel.setValue(autoCompleteMultiValues != null ? mutiValue : null);
				}
				break;
			case "autoCompleteTextView":
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
				questionModel = engineUtils.prepareQuestionModel(question);

				questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
						null, user, paramKeyValMap, session);
				Map<String,Object> value = (Map<String, Object>) submissionData.getData().get(question.getColumnName());
				questionModel.setValue(value != null ? value.get("value") : null);
			}
				break;

			case "checkbox": {
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					questionModel = engineUtils.prepareQuestionModel(question);

					// setting model
					if (submissionData != null) {
						questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
								String.class.cast(submissionData.getData().get(question.getColumnName())), user,
								paramKeyValMap, session);
					} else {
						questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
								null, user, paramKeyValMap, session);
					}
				}
			}
				break;
			case "textbox":
			case "textarea":
			case "geolocation":
			case "Month Widget": {

				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					questionModel = engineUtils.prepareQuestionModel(question);
					switch (question.getFieldType()) {

					case "singledecimal":
					case "doubledecimal":
					case "threedecimal":
						questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
								? String.valueOf(submissionData.getData().get(question.getColumnName()).toString())
								: null);
						break;

					case "tel":
						questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
								? Long.parseLong(submissionData.getData().get(question.getColumnName()).toString())
								: null);

						break;
					default:
						questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
								? String.valueOf(submissionData.getData().get(question.getColumnName()).toString())
								: null);
						break;
					}

				}
			}
				break;

			case "dropdown":
			case "segment": 
			{
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
				questionModel = engineUtils.prepareQuestionModel(question);
				questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
						null, user, paramKeyValMap, session);
				switch (question.getFieldType()) {

				case "checkbox":
					if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					
						// setting model
						if (submissionData != null) {
							if (submissionData.getData().get(question.getColumnName()) != null
									&& submissionData.getData().get(question.getColumnName()) instanceof ArrayList) {

								String values = ((List<Integer>) submissionData.getData().get(question.getColumnName()))
										.stream().map(e -> e.toString()).collect(Collectors.joining(","));
								questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap,
										question, values, user, paramKeyValMap, session);

								Map<Integer, String> optionMap = questionModel.getOptions().stream()
										.collect(Collectors.toMap(OptionModel::getKey, OptionModel::getValue));

								StringBuilder selections = new StringBuilder();
								for (Integer optionkey : ((List<Integer>) submissionData.getData()
										.get(question.getColumnName()))) {

									selections.append(optionMap.get(optionkey));
									selections.append(",");

								}
								questionModel.setValue(
										selections.toString().substring(0, selections.toString().length() - 1));
								questionModel.setOptions(new ArrayList<>());

							} else if (submissionData.getData().get(question.getColumnName()) != null
									&& submissionData.getData().get(question.getColumnName()) instanceof String) {
								questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap,
										question,
										String.class.cast(submissionData.getData().get(question.getColumnName())), user,
										paramKeyValMap, session);
							}

						} else {
							questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
									null, user, paramKeyValMap, session);

						}
					
					}
					break;
				default:
					if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
						
						if(submissionData.getData().get(question.getColumnName())!=null) {
							StringBuilder selections = new StringBuilder();
							for (OptionModel option : questionModel.getOptions()) {
								if (option.getKey() == Integer
										.parseInt(submissionData.getData().get(question.getColumnName()).toString())) {
									selections.append(option.getValue()).append(",");
								}
							}
							questionModel.setValue(selections.toString().substring(0, selections.toString().length() - 1));
							questionModel.setOptions(new ArrayList<>());
						}
					
					}
				}
				}
			}
				break;
			case "table":
			case "tableWithRowWiseArithmetic": {
				questionModel = engineUtils.prepareQuestionModel(question);
				/**
				 * from table question id and cell parent id getting all matched cells here
				 */
				List<Question> tableCells = questionList.stream()
						.filter(q -> q.getParentColumnName().equals(question.getColumnName()))
						.collect(Collectors.toList());

				Map<String, List<Question>> groupWiseQuestionsMap = new LinkedHashMap<>();

				tableCells.forEach(cell -> {

					if (groupWiseQuestionsMap.get(cell.getQuestion().split("@@split@@")[0].trim()) == null) {
						List<Question> questionPerGroup = new ArrayList<>();
						questionPerGroup.add(cell);
						groupWiseQuestionsMap.put(cell.getQuestion().split("@@split@@")[0].trim(), questionPerGroup);
					} else {
						List<Question> questionPerGroup = groupWiseQuestionsMap
								.get(cell.getQuestion().split("@@split@@")[0].trim());
						questionPerGroup.add(cell);
						groupWiseQuestionsMap.put(cell.getQuestion().split("@@split@@")[0].trim(), questionPerGroup);
					}

				});

				List<Map<String, Object>> array = new LinkedList<>();
				Integer index = 0;
				for (Map.Entry<String, List<Question>> map : groupWiseQuestionsMap.entrySet()) {
					List<Question> qs = map.getValue();
					
					Map<String, Object> jsonMap = new LinkedHashMap<String, Object>();
					jsonMap.put(question.getQuestion(), map.getKey());

					for (Question qdomain : qs) {
						QuestionModel qModel = engineUtils.prepareQuestionModel(qdomain);
						
						if (qModel.getControlType().equals("dropdown") || qModel.getControlType().equals("segment")) {
							qModel = engineUtils.setTypeDetailsAsOptions(qModel, typeDetailsMap, qdomain,
									null, user, paramKeyValMap, session);
							Map<Integer, String> toptionMap = qModel.getOptions().stream()
									.collect(Collectors.toMap(OptionModel::getKey, OptionModel::getValue));
							
							
							
							Integer values = (List<Map<String, Integer>>) submissionData.getData()
									.get(question.getColumnName()) != null
											? (((List<Map<String, Integer>>) submissionData.getData()
													.get(question.getColumnName())).get(index)
															.get(qdomain.getColumnName()))
											: null;
							if (values != null) {
								qModel.setValue(toptionMap.get(values));
								qModel.setOptions(new ArrayList<>());
							}
						}
							else if (qModel.getControlType().equals("textarea")) {
								
								String textValue = (List<Map<String, String>>) submissionData.getData()
										.get(question.getColumnName()) != null
												? (((List<Map<String, String>>) submissionData.getData()
														.get(question.getColumnName())).get(index)
																.get(qdomain.getColumnName()))
												: null;
												qModel.setValue(textValue);
						} else {
							qModel.setValue(submissionData == null ? null
									: (List<Map<String, Integer>>) submissionData.getData()
											.get(question.getColumnName()) != null
													? (((List<Map<String, Integer>>) submissionData.getData()
															.get(question.getColumnName())).get(index)
																	.get(qdomain.getColumnName()))
													: null);

						}
						jsonMap.put(qdomain.getQuestion().split("@@split@@")[1].trim(), qModel);
					}
					index++;
					array.add(jsonMap);
				}

				questionModel.setTableModel(array);
			}
				break;

			case BEGIN_REPEAT: {
				questionModel = prepareBeginRepeatModelWithData(question, questionList, submissionData, questionMap,
						typeDetailsMap, paramKeyValMap, session, user);

			}

				break;

			case "camera": {
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					questionModel = engineUtils.prepareQuestionModel(question);
					questionModel = iCameraDataHandler.readExternal(questionModel, submissionData, paramKeyValMap);
					
					
				}
			}
				break;

			case "file": {
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

					questionModel = engineUtils.prepareQuestionModel(question);
					questionModel = iCameraDataHandler.readExternal(questionModel, submissionData, paramKeyValMap);
					
					
					
				}
			}
				break;

//			case "autoCompleteTextView":
//			case "autoCompleteMulti":{
//				
//				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
//					questionModel = engineUtils.prepareQuestionModel(question);
//					questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
//							null, user, paramKeyValMap, session);
//
//					questionModel
//					.setValue(submissionData.getData().get(question.getColumnName()) != null
//							? submissionData.getData().get(question.getColumnName())
//							: null);
//				}
//			}
//			
//			break;

			}

			if (sectionMap.containsKey(question.getSection())) {

				if (subsectionMap.containsKey(question.getSubsection())) {

					/**
					 * checking the type of accordian here ie. RepeatSubSection()==no means not an
					 * accordian, and yes means accordian
					 */
					List<QuestionModel> list = (List<QuestionModel>) subsectionMap.get(question.getSubsection());
					if (questionModel != null)
						list.add(questionModel);

				} else {
					listOfQuestionModel = new LinkedList<>();
					if (questionModel != null)
						listOfQuestionModel.add(questionModel);
					subsectionMap.put(question.getSubsection(), listOfQuestionModel);
				}

			} else {
				subsectionMap = new LinkedHashMap<>();
				listOfQuestionModel = new ArrayList<>();
				if (questionModel != null)
					listOfQuestionModel.add(questionModel);
				subsectionMap.put(question.getSubsection(), listOfQuestionModel);

				sectionMap.put(question.getSection(), subsectionMap);
			}
		}

		/**
		 * adding list of subsection against a section.
		 */

		for (Map.Entry<String, Map<String, List<QuestionModel>>> entry : sectionMap.entrySet()) {

			if (mapOfSectionSubsectionListOfQuestionModel.containsKey(entry.getKey())) {
				mapOfSectionSubsectionListOfQuestionModel.get(entry.getKey()).add(entry.getValue());
			} else {
				mapOfSectionSubsectionListOfQuestionModel.put(entry.getKey(), Arrays.asList(entry.getValue()));
			}
		}
		return mapOfSectionSubsectionListOfQuestionModel;
	}

	@Override
	public List<EnginesForm> getAllEnginesForms(OAuth2Authentication oauths) {

		List<EnginesForm> formsz = new ArrayList<>();

		OAuth2Authentication oauth = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();

		UserModel userModel = tokenInfoExtracter.getUserModelInfo(oauth);

		String designation = userModel.getRoleIds().iterator().next();

		/**
		 * if loggedin user is admin than get all forms
		 */
		Designation desgs = designationRepository.findBySlugIdIn(userModel.getDesgSlugIds()).get(0);

		if (desgs.getCode().equals("ADMIN")) {
			List<EnginesForm> forms = enginesFormRepository.findAll();
			formsz.addAll(forms);
		} else {
			List<DesignationPartnerFormMapping> designationPartnerFormMapping = designationPartnerFormMappingRepository
					.findBypartnerIdInAndAccessTypeAndDesignation(userModel.getPartnerId(), AccessType.DATA_ENTRY,
							designationRepository.findById(designation));

			for (DesignationPartnerFormMapping dpfm : designationPartnerFormMapping) {
				List<Integer> formIds = dpfm.getFormId();

				List<EnginesForm> forms = enginesFormRepository.findByFormIdIn(formIds);
				formsz.addAll(forms);
			}
		}

		return formsz;

	}

	public QuestionModel prepareBeginRepeatModelWithData(Question question, List<Question> questionList,
			DataModel submissionData, Map<String, Question> questionMap, Map<Integer, TypeDetail> typeDetailsMap,
			Map<String, Object> paramKeyValMap, HttpSession session, Object user) {

		QuestionModel questionModel = engineUtils.prepareQuestionModel(question);

		List<Question> beginRepeatQuestions = questionList.stream()
				.filter(qq -> qq.getParentColumnName().equals(question.getColumnName())).collect(Collectors.toList());

		List<QuestionModel> questionsInRow = new ArrayList<>();

		List<List<QuestionModel>> beginRepeatModel = new ArrayList<>();
		Map<Integer, List<QuestionModel>> rowInTable = new HashMap<>();

		Map<String, Object> data = submissionData.getData();

		if (data.get(question.getColumnName()) == null) {
			throw new NullPointerException("No data available for the begin repeat key :" + question.getColumnName());
		}
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> beginRepeatData = (List<Map<String, Object>>) data.get(question.getColumnName());

		for (int qIndex = 0; qIndex < beginRepeatQuestions.size(); qIndex++) {
			Question questionInRow = beginRepeatQuestions.get(qIndex);

			switch (questionInRow.getControllerType()) {
			default:

//				if (beginRepeatData.get(0).get(questionInRow.getColumnName()) == null) {
//					// in case the question is not found in data, due to fact
//					// that the question was
//					// not there in blank form and was added later point of time
//					// into
//					// checklist/form, skip that question and continue to next
//					// question.
//					continue;
//				}
				int rowIndexOfTable = 0;
				String checkedValue = null;

				for (Map<String, Object> rowData : beginRepeatData) {
					checkedValue = null;
					QuestionModel qModel = engineUtils.prepareQuestionModel(questionInRow);
					qModel = engineUtils.setParentColumnNameOfOptionTypeForBeginRepeatWithData(qModel, question,
							questionMap, rowIndexOfTable, qIndex, rowInTable, beginRepeatQuestions);

					switch (questionInRow.getControllerType().trim()) {
					case "camera": {

						qModel = engineUtils.prepareQuestionModel(questionInRow);
						qModel = iCameraDataHandler.readExternal(qModel, submissionData, paramKeyValMap);
					}
						break;

					case "Date Widget": {
						if (String.class.cast(rowData.get(questionInRow.getColumnName())) != null) {
							String date = getDateFromString(
									String.class.cast(rowData.get(questionInRow.getColumnName())));
							qModel.setValue(rowData.get(questionInRow.getColumnName()) != null ? date : null);
						} else {
							qModel.setValue(rowData.get(questionInRow.getColumnName()) != null
									? String.class.cast(rowData.get(questionInRow.getColumnName()))
									: null);
						}

					}
						break;
					case "Time Widget":
						qModel.setValue(rowData.get(questionInRow.getColumnName()) != null
								? String.class.cast(rowData.get(questionInRow.getColumnName()))
								: null);
						break;
					case "dropdown":
					case "segment":
					case "autoCompleteTextView":
					case "autoCompleteMulti":
						// we know, only single option is selected,
						// so we fetch the type details from
						// typeDetail Map and set value
						switch (questionInRow.getFieldType().trim()) {
						case "checkbox":
							if ((rowData.get(questionInRow.getColumnName()) == null ? null
									: rowData.get(questionInRow.getColumnName()).equals("") ? null
											: rowData.get(questionInRow.getColumnName()).toString()) != null) {

								List<Integer> typeIds = rowData.get(questionInRow.getColumnName()) != null
										? (List<Integer>) rowData.get(questionInRow.getColumnName())
										: null;

								String fieldValue = typeIds.stream().map(e -> e.toString())
										.collect(Collectors.joining(","));
								checkedValue = fieldValue;

								qModel = engineUtils.setTypeDetailsAsOptions(qModel, typeDetailsMap, questionInRow,
										checkedValue, user, paramKeyValMap, session);

							
								if(typeIds!=null) {
									Map<Integer, String> optionMap = qModel.getOptions().stream()
											.collect(Collectors.toMap(OptionModel::getKey, OptionModel::getValue));

									StringBuilder selections = new StringBuilder();
									for (Integer optionkey : typeIds) {

										selections.append(optionMap.get(optionkey)).append(",");

									}
									qModel.setValue(selections.toString().substring(0, selections.toString().length() - 1));
									qModel.setOptions(new ArrayList<>());
								}
							
							}

							break;
						default:
							qModel = engineUtils.setTypeDetailsAsOptions(qModel, typeDetailsMap, questionInRow, null,
									user, paramKeyValMap, session);
							StringBuilder selections = new StringBuilder();
							for (OptionModel option : qModel.getOptions()) {
								if (option.getKey() == Integer.class.cast(rowData.get(questionInRow.getColumnName()))) {
									selections.append(option.getValue()).append(",");
								}
							}
							qModel.setValue(selections.toString().substring(0, selections.toString().length() - 1));
							qModel.setOptions(new ArrayList<>());
						}

						break;
					case "Month Widget":
					case "textbox":
					case "textarea":
						// We return the captured value as string

						switch (questionInRow.getFieldType()) {
						case "tel":
							qModel.setValue(rowData.get(questionInRow.getColumnName()) != null
									? Integer.parseInt(rowData.get(questionInRow.getColumnName()).toString())
									: null);
							break;
						default:
							qModel.setValue(rowData.get(questionInRow.getColumnName()) != null
									? String.class.cast(rowData.get(questionInRow.getColumnName()))
									: null);
							break;
						}
					}

					if (rowInTable.get(rowIndexOfTable) == null) {
						questionsInRow = new ArrayList<>();
						questionsInRow.add(qModel);
						rowInTable.put(rowIndexOfTable, questionsInRow);
					} else {
						questionsInRow = rowInTable.get(rowIndexOfTable);
						questionsInRow.add(qModel);
						rowInTable.put(rowIndexOfTable, questionsInRow);
					}
					rowIndexOfTable++;
				}
				break;
			}
		}
		if (rowInTable == null || rowInTable.isEmpty()) {
			beginRepeatModel.add(questionsInRow);
		} else {
			rowInTable.forEach((k, v) -> {
				beginRepeatModel.add(v);
			});
		}
		questionModel.setBeginRepeat(beginRepeatModel);
		return questionModel;
	}

	public String getDateFromString(String stringDate) {
		String dt = null;
		try {
			Date date = new SimpleDateFormat("dd-MM-yyyy").parse(stringDate);
			dt = new SimpleDateFormat("yyyy-MM-dd").format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dt;
	}

	@Override
	public Map<String, List<UpdateAreaModel>> getUpdatedAreas(String lastUpdatedDate) {
		Map<String,List<UpdateAreaModel>> areaModelMap = new HashMap<>();
		List<CollectionAudit> collectionAudit = null;
		try {
			
			MatchOperation match = Aggregation.match(Criteria.where("collectionName").is("area").and("updateDate").gte(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(lastUpdatedDate)));
			Aggregation resultQuery=Aggregation.newAggregation(match);
			collectionAudit = mongoTemplate.aggregate(resultQuery, CollectionAudit.class, CollectionAudit.class).getMappedResults();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Area> listOfAreas = new ArrayList<>();
          if(collectionAudit!=null && !collectionAudit.isEmpty()) {
        	  listOfAreas = areaRepository.findAllByAreaLevelAreaLevelIdIn(Arrays.asList(6,7)); 
          }
		 
		List<UpdateAreaModel> areaModelList= null;
		for (Area area : listOfAreas) {
			if(areaModelMap.containsKey(area.getAreaLevel().getAreaLevelName().toLowerCase())) {
				areaModelMap.get(area.getAreaLevel().getAreaLevelName().toLowerCase()).add(getAreaModel(area));
				
			}else {
				areaModelList= new ArrayList<>();
				areaModelList.add(getAreaModel(area));
				areaModelMap.put(area.getAreaLevel().getAreaLevelName().toLowerCase(),areaModelList);
			}
		
		}
		AreaLevel areaLevel = null;
		Area otherArea = null;
		//others add
		if(!listOfAreas.isEmpty()) {
		otherArea = areaRepository.findByAreaId(600003);
		if(otherArea != null) {
		areaLevel= areaLevelRepository.findByAreaLevelId(6);
		if(areaModelMap.containsKey(areaLevel.getAreaLevelName().toLowerCase())) {
		areaModelMap.get(areaLevel.getAreaLevelName().toLowerCase()).add(getAreaModel(otherArea));
		}
		}
		
		otherArea = areaRepository.findByAreaId(600004);
		if(otherArea != null) {
		areaLevel= areaLevelRepository.findByAreaLevelId(7);
		if(areaModelMap.containsKey(areaLevel.getAreaLevelName().toLowerCase())) {
		areaModelMap.get(areaLevel.getAreaLevelName().toLowerCase()).add(getAreaModel(otherArea));
		}
		}
	}
		
		return areaModelMap;
	}

	private UpdateAreaModel getAreaModel(Area area) {
		UpdateAreaModel areaModel= new UpdateAreaModel();
		areaModel.setAreaId(area.getAreaId());
		areaModel.setAreaName(area.getAreaName());
		areaModel.setParentAreaId(area.getParentAreaId());
		return areaModel;
	}
}
