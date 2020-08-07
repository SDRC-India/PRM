package org.sdrc.pmr.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.sdrc.pmr.model.EditSubmissionModel;
import org.sdrc.pmr.model.UserModel;
import org.sdrc.pmr.util.OAuth2Utility;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.engine.FormsServiceImpl;
import in.co.sdrc.sdrcdatacollector.handlers.IDbFetchDataHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.QuestionRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.util.EngineUtils;

/**
 * @author Sarita Panigrahi
 *
 */
@Service
public class EditSubmissionServiceImpl implements EditSubmissionService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TypeDetailRepository typeDetailRepository;

	@Autowired
	private IDbFetchDataHandler iDbFetchDataHandler;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private EngineFormRepository enginesFormRepository;

	@Autowired
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;
	
	@Autowired
	private EngineUtils engineUtils;

	@Autowired
	private FormsServiceImpl formsServiceImpl;

	private final String BEGIN_REPEAT = "beginrepeat";

	private DateFormat ymdDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private OAuth2Utility oAuth2Utility;

	@Override
	public ResponseEntity<List<EditSubmissionModel>> getSubmissionsToEdit(String mobileNo, Integer formId) {

//		new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())
		UserModel user = oAuth2Utility.getUserModel();
		
		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(user.getRoleIds()));
		
		MatchOperation match = null;
		
		if(desig.get(0).getName().equals("STATE LEVEL") || desig.get(0).getName().equals("ADMIN") ) {
			match = Aggregation.match(Criteria.where("formId").is(formId).and("isValid").is(true)
					.and("latest").is(true)
					.and("isAggregated").is(false)
					.and("checklistSubmissionStatus").is("COMPLETE")
					.and("data.undp_f1_q0").is(1)
					.and("data.undp_f1_mob").is(new Long(mobileNo)));
		}else {
			match = Aggregation.match(Criteria.where("formId").is(formId).and("isValid").is(true)
					.and("latest").is(true)
					.and("isAggregated").is(false)
					.and("checklistSubmissionStatus").is("COMPLETE")
					.and("data.undp_f1_q0").is(1)
					.and("data.undp_f1_mob").is(new Long(mobileNo))
					.and("data.undp_f1_q18").in(user.getAreaIds()));
		}
		

		SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "uniqueId").and(Sort.Direction.DESC, "syncDate");

//		GroupOperation group = Aggregation.group("_id").last("id").as("id").last("updatedDate").as("updatedDate")
//				.last("createdDate").as("createdDate").last("uniqueId").as("uniqueId").last("data.undp_f1_mob")
//				.as("mobileNo").last("data.undp_f1_q1").as("name");
		
		ProjectionOperation projectionOperation = Aggregation.project()
				.and("uniqueId").as("uniqueId")
				.and("data.undp_f1_mob").as("mobileNo")
				.and("data.undp_f1_q1").as("name")
				.and("updatedDate").dateAsFormattedString("%d-%m-%Y %H:%M:%S").as("updatedDate")
				.and("createdDate").dateAsFormattedString("%d-%m-%Y %H:%M:%S").as("createdDate");
		Aggregation resultQuery = Aggregation.newAggregation(match, sort, projectionOperation);	

		List<EditSubmissionModel> submissionModels = mongoTemplate
				.aggregate(resultQuery, "allChecklistFormData", EditSubmissionModel.class).getMappedResults();

		return new ResponseEntity<List<EditSubmissionModel>>(submissionModels, HttpStatus.OK);

	}

	@Override
	public Map<String, List<Map<String, List<QuestionModel>>>> getViewMoreDataForReview(Integer formId,
			String submissionId, Map<String, Object> paramKeyValMap, HttpSession session) {

		UserModel user = oAuth2Utility.getUserModel();

		DataModel submissionData = iDbFetchDataHandler.getSubmittedData(submissionId, formId);

		EnginesForm form = enginesFormRepository.findByFormId(formId);

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
				if(question.getColumnName().equals("undp_f1_q25_5")) {
					questionModel.setRelevance(null);
				}
				break;
			case "Time Widget":
				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
					questionModel = engineUtils.prepareQuestionModel(question);
					questionModel.setValue(String.class.cast(submissionData.getData().get(question.getColumnName())));
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
			case "Month Widget":
			case "textbox":
			case "textarea":
			case "geolocation":
			case "score-holder":
			case "heading":
			case "uuid":
			case "score-keeper":
			case "sub-score-keeper":
			case "mfile":
			case "checklist-score-keeper": {
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
			case "segment": {
				switch (question.getFieldType()) {

				case "checkbox":
					if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {

						questionModel = engineUtils.prepareQuestionModel(question);

						// setting model
						if (submissionData != null) {
							if (submissionData.getData().get(question.getColumnName()) != null
									&& submissionData.getData().get(question.getColumnName()) instanceof ArrayList) {

								String values = ((List<Integer>) submissionData.getData().get(question.getColumnName()))
										.stream().map(e -> e.toString()).collect(Collectors.joining(","));
								questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap,
										question, values, user, paramKeyValMap, session);

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

						questionModel.setValue(submissionData.getData().get(question.getColumnName()));
					}
					break;
				default:
					if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
						questionModel = engineUtils.prepareQuestionModel(question);
						questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question,
								null, user, paramKeyValMap, session);

						questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
								? Integer.parseInt(submissionData.getData().get(question.getColumnName()).toString())
								: null);
					}
				}

			}
				break;

			case "autoCompleteTextView":
			case "autoCompleteMulti": {

				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
					questionModel = engineUtils.prepareQuestionModel(question);
					questionModel = engineUtils.setTypeDetailsAsOptions(questionModel, typeDetailsMap, question, null,
							user, paramKeyValMap, session);

					questionModel.setValue(submissionData.getData().get(question.getColumnName()) != null
							? submissionData.getData().get(question.getColumnName())
							: null);
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
					;
					Map<String, Object> jsonMap = new LinkedHashMap<String, Object>();
					jsonMap.put(question.getQuestion(), map.getKey());

					for (Question qdomain : qs) {
						QuestionModel qModel = engineUtils.prepareQuestionModel(qdomain);

						qModel.setValue(submissionData == null ? null
								: (List<Map<String, Integer>>) submissionData.getData()
										.get(question.getColumnName()) != null
												? (((List<Map<String, Integer>>) submissionData.getData()
														.get(question.getColumnName())).get(index)
																.get(qdomain.getColumnName()))
												: null);
						jsonMap.put(qdomain.getQuestion().split("@@split@@")[1].trim(), qModel);
					}
					index++;
					array.add(jsonMap);
				}

				questionModel.setTableModel(array);
			}
				break;

			case BEGIN_REPEAT:

			{
				questionModel = engineUtils.prepareQuestionModel(question);
				List<Question> beginRepeatQuestions = questionList.stream()
						.filter(qq -> qq.getParentColumnName().equals(question.getColumnName()))
						.collect(Collectors.toList());

				List<QuestionModel> model = new ArrayList<>();
				List<List<QuestionModel>> beginRepeatModel = new ArrayList<>();
				int arrayIndex = 0;

				for (int index = 0; index < beginRepeatQuestions.size(); index++) {

					Question q = beginRepeatQuestions.get(index);

					// if submission data is null we have to design an array
					// of array of question.

					QuestionModel qModel = engineUtils.prepareQuestionModel(q);
					switch (q.getControllerType().trim()) {
					case "dropdown":
					case "segment":
						qModel = engineUtils.setTypeDetailsAsOptions(qModel, typeDetailsMap, q, null, user,
								paramKeyValMap, session);
						break;
					}
					qModel = engineUtils.setParentColumnNameOfOptionTypeForBeginRepeat(qModel, question, questionMap,
							arrayIndex, index, model, beginRepeatQuestions);

					model.add(qModel);
				}
				beginRepeatModel.add(model);
				questionModel.setBeginRepeat(beginRepeatModel);
			}
				break;

//			case "camera": {
//				if (question.getParentColumnName() == null || question.getParentColumnName().isEmpty()) {
//
//					questionModel = engineUtils.prepareQuestionModel(question);
//					questionModel = iCameraDataHandler.readExternal(questionModel, submissionData,paramKeyValMap);
//
//				}
//			}
//				break;
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
}
