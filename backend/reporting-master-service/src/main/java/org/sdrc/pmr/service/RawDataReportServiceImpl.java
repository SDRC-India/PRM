package org.sdrc.pmr.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.sdrc.pmr.collection.AllChecklistFormData;
import org.sdrc.pmr.collection.Area;
import org.sdrc.pmr.collection.DataValue;
import org.sdrc.pmr.collection.Indicator;
import org.sdrc.pmr.model.ReportChartModel;
import org.sdrc.pmr.model.UserModel;
import org.sdrc.pmr.repository.AreaRepository;
import org.sdrc.pmr.repository.IndicatorRepository;
import org.sdrc.pmr.util.ExcelStyleSheet;
import org.sdrc.pmr.util.OAuth2Utility;
import org.sdrc.pmr.util.ReportUtil;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators.Sum;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.Question;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.MessageModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.QuestionRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author subham
 *
 */
@Service
@Slf4j
public class RawDataReportServiceImpl implements RawDataReportService {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private TypeDetailRepository typeDetailRepository;
	
	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private AreaRepository areaRepository;
	
//	@Autowired
//	private DesignationPartnerFormMappingRepository designationPartnerFormMappingRepository;
	
	@Autowired
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private EngineFormRepository engineFormRepository;
	
	
	@Autowired
	private EngineFormRepository formRepository;
	
//	private SimpleDateFormat sdfDateTimeWithSeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private SimpleDateFormat sDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	@Autowired
	private OAuth2Utility oAuth2Utility; 
	
	@Autowired
	private ReportUtil reportUtil;
	
	/**
	 * Static factory method to create a Criteria using the provided key
	 * 
	 * @param key
	 * @return
	 */
	public static Criteria where(String key) {
		return new Criteria(key);
	}

	
	
	private String getExportDatasResult(List<AllChecklistFormData> datas, List<Question> questionList, Integer formId,
			Date startDate, Date EndDate, Principal principal) {
		
		EnginesForm form = formRepository.findByFormId(formId);

		return exportData(datas, questionList, formId,"data", form.getName().replace(" ", "_").toLowerCase()+"_Report", startDate, EndDate, form.getName(), principal);

	}
	
	private String exportData(List<AllChecklistFormData> list, List<Question> questionList, int formId, String sheetName, String fileName,
			Date startDate, Date endDate, String formName, Principal principal) {
	
		/*
		 * column name as a key
		 */
		Map<String, Question> questionMap = questionList.stream()
				.collect(Collectors.toMap(Question::getColumnName, question -> question));

		/*
		 * getting all the typeDetails by passing formId
		 */
		List<TypeDetail> typeDetailList = typeDetailRepository.findByFormId(formId);

		/*
		 * KEY-SlugId value-name
		 */
		Map<Integer, TypeDetail> typeDetailMap = typeDetailList.stream()
				.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));

		/**
		 * get all Area(only areaid and areaname)
		 */
		List<Area> areaList = areaRepository.findAreaIdAndAreaName();

		Map<Integer, String> areaMap = areaList.stream().collect(Collectors.toMap(Area::getAreaId, Area::getAreaName));
		
		try {

			SXSSFWorkbook workbook = null;
			SXSSFSheet sheet = null;
			Row row;
			Cell cell;

			/*
			 * creating a questions heading
			 */
			workbook = setQuestions(workbook, sheet, questionList, sheetName, startDate, endDate, formName);

			/*
			 * get style for odd cell
			 */
			CellStyle colStyleOdd = ExcelStyleSheet.getStyleForOddCell(workbook,false);
			/*
			 * get style for even cell
			 */
			CellStyle colStyleEven = ExcelStyleSheet.getStyleForEvenCell(workbook,false);
			/*
			 * Iterating to set submission values
			 */
			Integer rowIndex = 4;
			Integer columnIndex = 0;
			Integer mapSize = null;

			/*
			 * begin-repeat variables
			 */
			Row beginRepeatRow = null;
			Cell beginRepeatcell = null;
			Integer beginRepeatRowCount = 1;
			Integer beginRepeatcellCount = 0;
			for (int i = 0; i < list.size(); i++) {

				sheet = workbook.getSheet(sheetName);
				row = sheet.createRow(rowIndex);
				
				/* 
				 * setting serial number value
				 */
				cell = row.createCell(columnIndex);
				cell.setCellValue(i + 1);
				cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
				columnIndex++;

				AllChecklistFormData allChecklistFormData = null;

				/*
				 * key is column name here,object is submitted value
				 */
				Map<String, Object> submissionDataMap = null;

				/*
				 * checking the type of List
				 */
				if (list.get(i) instanceof AllChecklistFormData) {
					allChecklistFormData = (AllChecklistFormData) list.get(i);
					submissionDataMap = allChecklistFormData.getData();
					/*
					 * setting user name
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(allChecklistFormData.getUserName());
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;
					/*
					 * setting submission date
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(sDateFormat.format(allChecklistFormData.getSyncDate()));
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting valid record status
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(allChecklistFormData.getIsValid() == true ? "Valid" : "Invalid");
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting submission status
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(allChecklistFormData.getChecklistSubmissionStatus().toString());
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting rejection status-
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(allChecklistFormData.isRejected() == false ? "Approved" : "Rejected");
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting aggregation status-
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(allChecklistFormData.getIsAggregated() == true ? "Aggregated" : "Not aggregated");
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;

					/**
					 * setting unique id
					 */
					cell = row.createCell(columnIndex);
					cell.setCellValue(allChecklistFormData.getUniqueId());
					cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
					columnIndex++;
				}

				for (int j = 0; j < questionList.size(); j++) {

//					System.out.println("row count ===========" + j);
					String fieldValue = "";
					String colName = null;

					Question question = questionList.get(j);

					switch (question.getControllerType()) {

					case "dropdown":
					case "segment": {
						/*
						 * get sheet
						 */
						sheet = workbook.getSheet(sheetName);
						/*
						 * get column name
						 */
						colName = question.getColumnName();

//						System.out.println("column name +++++++++=== "+colName);
						/*
						 * with column name against find value which is SlugId
						 * of TypeDetail documnet
						 */
						List<Integer> values = null;

						if (question.getFieldType().equals("checkbox")) {
							values = submissionDataMap.get(colName) != null
									? (List<Integer>) (submissionDataMap.get(colName)) : null;
						} else {
//							System.out.println(colName);
							Integer v = submissionDataMap.get(colName) != null ? (int) (submissionDataMap.get(colName))
									: null;
							if (v != null) {
								values = new ArrayList<>();
								values.add(v);
							}
						}

						if (values != null && !values.isEmpty()) {
							/*
							 * find typedetailname against this value
							 */
							String temp = null;
							for (Integer value : values) {

								if (question.getTableName() == null) {
									/*
									 * if there is no score present in
									 * typeDetails
									 */
//									System.out.println("value======="+value);
									if (typeDetailMap.get(value).getScore() == null)
										temp = typeDetailMap.get(value).getName();
									else// if score is present
									{
										temp = typeDetailMap.get(value).getName().concat(" - score = ")
												.concat(typeDetailMap.get(value).getScore());
									}
								}

								else {
									/*
									 * find areaname against this value
									 */
									// area_group:F1Q2@ANDfetch_tables:area$$arealevel=district
									switch (question.getTableName().split("\\$\\$")[0].trim()) {

									case "area": {

										temp = areaMap.get(value);
									}
										break;
										
									}
								}

								if (fieldValue.equals("")) {
									fieldValue = temp;
								} else
									fieldValue = fieldValue + "," + temp;
							}

						}

						// System.out.println(question.getQuestion()+":"+"COLUMN-NAME"+colName+":"+fieldValue
						// +"colcount ===="+columnIndex);

						cell = row.createCell(columnIndex);

//						System.out.println("fieldValue =====" + fieldValue + "column index === " + cell.getColumnIndex());

						cell.setCellValue(fieldValue != null ? fieldValue : "");
						cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
						columnIndex++;
						fieldValue = "";
					}
						break;

					case "beginrepeat": {
						String brSheeetName = null;
						brSheeetName = question.getQuestion();
						/*
						 * check the size of brSheeetName is size>=28 than
						 * reduce the sheetName
						 */
						if (brSheeetName.length() >= 28) {
							brSheeetName = brSheeetName.substring(0, 17);
						}
						/*
						 * it returns rowValue to be used
						 */
						sheet = workbook.getSheet(brSheeetName);
						/*
						 * get last row num creted in the sheet
						 */
						int rowValue = sheet.getLastRowNum();
						/*
						 * assign to beginRepeatRowCount to create row
						 */
						beginRepeatRowCount = rowValue + 1;
						/*
						 * get column name of question
						 */
						colName = question.getColumnName();
						/*
						 * beginRepeat type
						 */
						List<Map<String, Object>> beginRepeatMap = (List<Map<String, Object>>) submissionDataMap.get(colName);

						/*
						 * get size of map
						 */
						Map<String, Object> map2 = beginRepeatMap.get(0);
						mapSize = map2.size();
						int k = 1;
						/*
						 * iterating list of submission of beginrepeat, list
						 */
						for (Map<String, Object> map : beginRepeatMap) {

							/*
							 * creating row
							 */
							beginRepeatRow = sheet.createRow(beginRepeatRowCount);
							/*
							 * creating cell
							 */
							beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
							/*
							 * setting reference-id column value
							 */
							beginRepeatcell.setCellValue(i + 1);
							/*
							 * setting style
							 */
							beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							beginRepeatcellCount++;
							
							/**
							 * unique id value setting
							 */
							beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
							beginRepeatcell.setCellValue(allChecklistFormData.getUniqueId());
							beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							beginRepeatcellCount++;
							/*
							 * iterating Map of beginrepeat
							 */
							for (Map.Entry<String, Object> entry : map.entrySet()) {

								if (k == 1)
									j++;// for question-loop it should increment
										// with size 1 only as value is multiple
										// but qstn is one

								switch (questionMap.get(entry.getKey()).getControllerType()) {

								case "dropdown":
								case "segment": {

									/*
									 * key == column name with column name
									 * against find value which is SlugId of
									 * TypeDetail documnet
									 */

									List<Integer> values = null;
									if (questionMap.get(entry.getKey()).getFieldType().equals("checkbox")) {
										values = entry.getValue() != null ? (List<Integer>) entry.getValue() : null;

									} else {
										Integer v = entry.getValue() != null ? (int) entry.getValue() : null;
										if (v != null) {
											values = new ArrayList<>();
											values.add(v);
										}

									}

									if (values != null && !values.isEmpty()) {
										String tempValue = null;
										/*
										 * find typedetailname against this
										 * value
										 */
										for (Integer value : values) {

											if (questionMap.get(entry.getKey()).getTableName() == null) {
												System.out.println("key == "+entry.getKey()+" value = "+value);
												tempValue = typeDetailMap.get(value).getName();
											}

											else {
												/*
												 * find areaname against this
												 * value
												 */

												switch (questionMap.get(entry.getKey()).getTableName()
														.split("\\$\\$")[0].trim()) {

												case "area": {

													tempValue = areaMap.get(value);
												}
													break;

												}
											}

											if (fieldValue.equals("")) {
												fieldValue = tempValue;
											} else
												fieldValue = fieldValue + "," + tempValue;

										}

									}

									/*
									 * write in excel cell
									 */
									beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
									beginRepeatcell.setCellValue(fieldValue != null ? fieldValue : "");
									beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									beginRepeatcellCount++;

								}
									break;

								default: {

//									System.out.println("fieldValue =====" + fieldValue + "column index === "
//											+ cell.getColumnIndex());
									try {
										fieldValue = entry.getValue() != null ? (String) entry.getValue() : null;
									} catch (ClassCastException e) {
										fieldValue = entry.getValue() != null
												? String.valueOf((Integer) entry.getValue()) : null;
									}

									/*
									 * write in excel cell
									 */
									beginRepeatcell = beginRepeatRow.createCell(beginRepeatcellCount);
									beginRepeatcell.setCellValue(fieldValue != null ? fieldValue : "");
									beginRepeatcell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									beginRepeatcellCount++;
								}

								}// end of switch

							} // end of map - innerloop

							if (beginRepeatMap.size() > 1) {
								beginRepeatRowCount++;
								beginRepeatcellCount = 0;
							}
							k++;
						}

						beginRepeatcellCount = 0;
					}
						break;

					case "tableWithRowWiseArithmetic": {
						/*
						 * getting sheet
						 */
						sheet = workbook.getSheet(sheetName);
						/*
						 * column name of question
						 */
						colName = question.getColumnName();
						/*
						 * tableWithRowWiseArithmetic type
						 */
						List<Map<String, Object>> beginRepeatMap = (List<Map<String, Object>>) submissionDataMap.get(colName);

						for (Map<String, Object> map : beginRepeatMap) {

							for (Map.Entry<String, Object> entry : map.entrySet()) {

								j++;// for question-loop

								switch (questionMap.get(entry.getKey()).getControllerType()) {

								case "dropdown":
								case "segment": {

									// System.out.println("row num = "+j);
									/*
									 * key == column name with column name
									 * against find value which is SlugId of
									 * TypeDetail documnet
									 */
									List<Integer> values = null;
									if (questionMap.get(entry.getKey()).getFieldType().equals("checkbox")) {

										values = entry.getValue() != null ? (List<Integer>) entry.getValue() : null;

									} else {
										Integer v = entry.getValue() != null ? (int) entry.getValue() : null;

										if (v != null) {
											values = new ArrayList<>();
											values.add(v);
										}

									}

									if (values != null && !values.isEmpty()) {
										/*
										 * find typedetailname against this
										 * value
										 */
										for (Integer value : values) {

											String tempValue = null;

											if (questionMap.get(entry.getKey()).getTableName() == null)
												tempValue = typeDetailMap.get(value).getName();
											else {
												/*
												 * find areaname against this
												 * value
												 */
												switch (questionMap.get(entry.getKey()).getTableName()
														.split("\\$\\$")[0].trim()) {

												case "area": {

													tempValue = areaMap.get(value);
												}
													break;

												}
											}

											if (fieldValue.equals("")) {
												fieldValue = tempValue;
											} else
												fieldValue = fieldValue + "," + tempValue;
										}

									}

									// System.out.println(question.getQuestion()+":"+"COLUMN-NAME"+colName+":"+fieldValue
									// +"colcount ===="+columnIndex);
									/*
									 * write in excel cell
									 */
									cell = row.createCell(columnIndex);
									cell.setCellValue(fieldValue != null ? fieldValue : "");
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									columnIndex++;

								}
									break;

								default: {

									if(questionMap.get(entry.getKey()).getFieldType().equals("tel")){
										Integer value = entry.getValue() != null ? (Integer) entry.getValue() : null;
										fieldValue=String.valueOf(value);
									}else{
										System.out.println("key "+entry.getKey()+"value"+entry.getValue());
										fieldValue = entry.getValue() != null ? String.valueOf(entry.getValue()) : null;
									}
									/**
									 * write in excel cell
									 */
//									 System.out.println(question.getQuestion()+":"+"COLUMN-NAME"+colName+":"+fieldValue
//									 +"colcount ===="+columnIndex);
									
									cell = row.createCell(columnIndex);
									
									if(fieldValue==null || fieldValue.equals("null")){
										cell.setCellValue("");
									}else{
										cell.setCellValue(fieldValue);
									}
//									cell.setCellValue(fieldValue != null ? fieldValue : "");
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
									columnIndex++;
								}

							}// end of switch

							} // end of map - innerloop
						}
					}
						break;

					case "autoCompleteTextView":{
						sheet = workbook.getSheet(sheetName);
						colName = question.getColumnName();
						if(submissionDataMap.get(colName)!=null){
							Map<String,Object> autoCompleteMap = (Map<String, Object>) submissionDataMap.get(colName);
							fieldValue = (String) autoCompleteMap.get("value");
							cell=row.createCell(columnIndex);
							cell.setCellValue(fieldValue);
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							fieldValue = "";
							columnIndex++;
						}else{
							cell=row.createCell(columnIndex);
							cell.setCellValue("");
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							fieldValue = "";
							columnIndex++;
						}
						
					}
					break;
					
					case "autoCompleteMulti":{
						sheet = workbook.getSheet(sheetName);
						colName = question.getColumnName();
						if(submissionDataMap.get(colName)!=null){
							List<Map<String,Object>> autoCompleteMap = (List<Map<String, Object>>) submissionDataMap.get(colName);
							
							String val = null;
							for(Map<String,Object> map : autoCompleteMap){
								
								if(val==null){
									val=(String) map.get("value");
									fieldValue = val;
								}else{
									val=val.concat(",").concat((String) map.get("value"));
									fieldValue = val;
								}
								
							}
							cell=row.createCell(columnIndex);
							cell.setCellValue(fieldValue);
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							fieldValue = "";
							columnIndex++;
						}else{
							cell=row.createCell(columnIndex);
							cell.setCellValue("");
							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							fieldValue = "";
							columnIndex++;
						}
						
					}
					break;
					
					case "camera": {
						
						sheet = workbook.getSheet(sheetName);
						CreationHelper createHelper = workbook.getCreationHelper();

						String attachFileName = "";

						Map<String, List<FormAttachmentsModel>> attachments = allChecklistFormData.getAttachments();

							if (attachments != null && attachments.get(question.getColumnName())!=null) {
								for (FormAttachmentsModel model : attachments.get(question.getColumnName())) {
									if (attachFileName.equals(""))
										attachFileName = model.getFilePath();
									else
										attachFileName = attachFileName.concat(",").concat(model.getFilePath());
								}
								
								List<String> commaSeperatedFilePath = Stream.of(attachFileName.split(",")).map(String::trim)
										.collect(Collectors.toList());

								for (int count = 1; count<=commaSeperatedFilePath.size(); count++) {

									cell = row.createCell(columnIndex);
									String linkAddress = configurableEnvironment.getProperty("image.download.path");
									cell.setCellValue("download image-" + count);
									linkAddress = linkAddress.concat("?path=" + Base64.getUrlEncoder().encodeToString(commaSeperatedFilePath.get(count-1).getBytes()));
									XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
									link.setAddress(linkAddress);
									cell.setHyperlink(link);
									cell.setCellStyle(i % 2 == 0 ? ExcelStyleSheet.getStyleForEvenHyperLink(workbook) :  ExcelStyleSheet.getStyleForOddHyperLink(workbook));
									fieldValue = "";
									columnIndex++;

								}

							}else{
								cell = row.createCell(columnIndex);
								cell.setCellValue("");
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
								columnIndex++;
							}
						
					}
						break;
						
						
					case "file": {
						
						sheet = workbook.getSheet(sheetName);
						String attachFileName = "";

						Map<String, List<FormAttachmentsModel>> attachments = allChecklistFormData.getAttachments();

							if (attachments != null && attachments.get(question.getColumnName())!=null) {
								for (FormAttachmentsModel model : attachments.get(question.getColumnName())) {
									if (attachFileName.equals(""))
										attachFileName = model.getFilePath();
									else
										attachFileName = attachFileName.concat(",").concat(model.getFilePath());
								}
								
								List<String> commaSeperatedFilePath = Stream.of(attachFileName.split(",")).map(String::trim)
										.collect(Collectors.toList());

								String value=null;
								for (int count = 1; count<=commaSeperatedFilePath.size(); count++) {

									cell = row.createCell(columnIndex);
									String linkAddress = configurableEnvironment.getProperty("image.download.path");
									if(value==null){
										value=linkAddress.concat("?path=" + Base64.getUrlEncoder().encodeToString(commaSeperatedFilePath.get(count-1).getBytes()));
										fieldValue=value;
									}else{
										value=value.concat(",").concat("  ").concat(linkAddress.concat("?path=" + Base64.getUrlEncoder().encodeToString(commaSeperatedFilePath.get(count-1).getBytes())));
										fieldValue=value;
									}
								}

								sheet.setColumnWidth(columnIndex, 12000);
								cell.setCellValue(fieldValue);
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
								fieldValue = "";
								columnIndex++;
								
							}else{
								cell = row.createCell(columnIndex);
								cell.setCellValue("");
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
								columnIndex++;
							
							}
							
					}
					break;
					

					default: {

						sheet = workbook.getSheet(sheetName);

						colName = question.getColumnName();
						/*
						 * fieldValue- type int or string and writing in excel
						 */
						cell = row.createCell(columnIndex);

						if (question.getFieldType().equals("tel")) {

							if (submissionDataMap.get(colName) == null
									|| submissionDataMap.get(colName).toString().trim().equals("")) {
								cell.setCellType(CellType.STRING);
								cell.setCellValue("");
								cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);

							} else {

								fieldValue = submissionDataMap.get(colName).toString();
								/*
								 * check length of value if it is >9 make it
								 * string and write it in excel else make it
								 * Long type
								 */
								if (fieldValue.length() > 9) {
									cell.setCellType(CellType.STRING);
									cell.setCellValue(fieldValue);
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
								} else {
									cell.setCellValue((Long.valueOf(fieldValue)));
									cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
									fieldValue = "";
								}

							}

						} else if (question.getFieldType().equals("singledecimal")
								|| question.getFieldType().equals("doubledecimal")
								|| question.getFieldType().equals("threedecimal")) {

							if (submissionDataMap.get(colName) == null
									|| submissionDataMap.get(colName).toString().trim().equals("")) {
								cell.setCellType(CellType.STRING);
								cell.setCellValue("");
							} else {

								cell.setCellValue(Double.valueOf((String) submissionDataMap.get(colName)));
							}

							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);

						} else {

							if (submissionDataMap.get(colName) == null
									|| submissionDataMap.get(colName).toString().trim().equals("")) {
								cell.setCellType(CellType.STRING);
								cell.setCellValue("");

							} else {
								
								fieldValue = (String) submissionDataMap.get(colName);
								if (formId == 5 && fieldValue.length() > 30) {

									sheet.setColumnWidth(cell.getColumnIndex(), 12000);
								}
								cell.setCellValue(fieldValue);
							}

							cell.setCellStyle(i % 2 == 0 ? colStyleEven : colStyleOdd);
							fieldValue = "";
						}
						columnIndex++;
					}
					}
				}
				rowIndex++;
				columnIndex = 0;
			}

			String dir = configurableEnvironment.getProperty("report.path");

			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();

			String name = fileName + "_" + principal.getName() + "_"
					+ new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xlsx";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();
			return path;

		} catch (Exception e) {
			log.error("error while generating report with payload formId : {}, startdate {}, enddate{} : " + formId,
					startDate, endDate, e);
			throw new RuntimeException(e);
		}

	}


	/**
	 * setting all the questions in excel first row and making different sheets
	 * for begin repeat type
	 * 
	 * @param workbook
	 * @param sheet
	 * @param questionList
	 * @param sheetName
	 * @return
	 */
	private SXSSFWorkbook setQuestions(SXSSFWorkbook workbook, SXSSFSheet sheet, List<Question> questionList,
			String sheetName, Date startDate, Date endDate, String formName) {

		/**
		 * make parent-id as a key, for begin-repeat
		 */
		List<Question> qList = new ArrayList<>();

		Map<String, List<Question>> questionMapBr = new LinkedHashMap<>();
		for (Question q : questionList) {

			if (questionMapBr.containsKey(q.getParentColumnName())) {
				questionMapBr.get(q.getParentColumnName()).add(q);
			} else {
				qList = new ArrayList<>();
				qList.add(q);
				questionMapBr.put(q.getParentColumnName(), qList);
			}

		}

		int mergeRange = 0;
		workbook = new SXSSFWorkbook();
		sheet = workbook. createSheet(sheetName);
		Row row;
		Cell cell;
		/*
		 * style for headers
		 */
		// CellStyle headerStyle =
		// ExcelStyleSheet.getStyleForColorHeader(workbook);
//		CellStyle styleForHeading = ExcelStyleSheet.getStyleForHeading(workbook);

		try {

			/*
			 * SINO
			 */
			row = sheet.createRow(3);
			row.setHeight((short) 1500);
			cell = row.createCell(0);
			sheet.setColumnWidth(cell.getColumnIndex(), 1700);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Sl. No.");
			mergeRange++;

			/*
			 * user name
			 */
			cell = row.createCell(1);
			sheet.setColumnWidth(cell.getColumnIndex(), 3500);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("User Name");
			mergeRange++;
			/*
			 * submission date
			 */
			cell = row.createCell(2);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Submission Date");
			mergeRange++;
			
			/*
			 * submission date
			 */
			cell = row.createCell(3);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Valid/Invalid Record");
			mergeRange++;
			
			/**
			 * submission status---on/late submission
			 */
			cell = row.createCell(4);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Submission Status");
			mergeRange++;
			
			/**
			 * rejection status-true or false
			 */
			cell = row.createCell(5);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Rejected/Approved Status");
			mergeRange++;
			
			/**
			 * aggregation status -- true or false
			 */
			cell = row.createCell(6);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Aggregation Status");
			mergeRange++;
			
			/**
			 * unique-id value
			 */
			cell = row.createCell(7);
			sheet.setColumnWidth(cell.getColumnIndex(), 4000);
			sheet.setHorizontallyCenter(true);
//			cell.setCellStyle(styleForHeading);
			cell.setCellValue("Unique-Id");
			mergeRange++;


			/*
			 * Setting all the questions
			 */
			int columnIndex = 8;
			int brColumnIndex = 1;
			String qstnValue = null;
			for (int i = 0; i < questionList.size(); i++) {

				Question question = questionList.get(i);

				switch (question.getControllerType()) {

				case "beginrepeat": {

					String brSheeetName = null;
					brSheeetName = question.getQuestion();
					/*
					 * check the size of brSheeetName is size>=28 than reduce
					 * the sheetName
					 */
					if (brSheeetName.length() >= 28) {
						brSheeetName = brSheeetName.substring(0, 17);
					}
					brColumnIndex = 2;
					
					sheet = workbook.createSheet(brSheeetName);
					Row row1 = sheet.createRow(0);
					
					Cell cell1 = row1.createCell(0);
					sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
					sheet.setHorizontallyCenter(true);
//					cell1.setCellStyle(styleForHeading);
					cell1.setCellValue("Reference_Id");

					cell1 = row1.createCell(1);
					sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
					sheet.setHorizontallyCenter(true);
//					cell1.setCellStyle(styleForHeading);
					cell1.setCellValue("Unique Id");
					
					List<Question> qHeaderlist = questionMapBr.get(question.getColumnName());

					for (Question que : qHeaderlist) {

						cell1 = row1.createCell(brColumnIndex);
//						cell1.setCellStyle(styleForHeading);
						cell1.setCellValue(que.getQuestion());
						sheet.setColumnWidth(cell1.getColumnIndex(), 4000);

						brColumnIndex++;
						i++;
					}

				}
					break;

				case "tableWithRowWiseArithmetic": {

					sheet = workbook.getSheet(sheetName);
					qstnValue = question.getQuestion();

					List<Question> qHeaderlist = questionMapBr.get(question.getColumnName());

					for (Question que : qHeaderlist) {

						cell = row.createCell(columnIndex);
//						cell.setCellStyle(styleForHeading);
						System.out.println("table "+que.getQuestion());
						
						cell.setCellValue(qstnValue.concat("-") + que.getQuestion().replaceAll("@@split@@", "-"));
						sheet.setColumnWidth(cell.getColumnIndex(), 9500);
						columnIndex++;
						i++;
						mergeRange++;
					}

				}
					break;


				default: {

					sheet = workbook.getSheet(sheetName);
					cell = row.createCell(columnIndex);
//					cell.setCellStyle(styleForHeading);
					
					System.out.println(question.getQuestion());
					cell.setCellValue(question.getQuestion());
					sheet.setColumnWidth(cell.getColumnIndex(), 4000);
					columnIndex++;
					mergeRange++;
				}

				}

			}
		} catch (Exception e) {

			log.error("error : ", e);
			throw new RuntimeException(e);
		}

		sheet = workbook.getSheet(sheetName);
		/*
		 * heading for raw data
		 */
		row = sheet.createRow(0);
		cell = row.createCell(0);
//		cell.setCellStyle(styleForHeading);
		cell.setCellValue(
				configurableEnvironment.getProperty("project.name") + " : Submission Report " + "(" + formName + ")");
		sheet = ExcelStyleSheet.doMerge(0, 0, 0, mergeRange - 1, sheet);
		/*
		 * report generation date
		 */
		row = sheet.createRow(1);
		cell = row.createCell(0);
//		cell.setCellStyle(styleForHeading);
		cell.setCellValue("Date of Report Generation : " + new SimpleDateFormat("dd-MM-yyy").format(new Date()));
		sheet = ExcelStyleSheet.doMerge(1, 1, 0, mergeRange - 1, sheet);

		/*
		 * from date and to date
		 */
		row = sheet.createRow(2);
		cell = row.createCell(0);
//		cell.setCellStyle(styleForHeading);
		cell.setCellValue("From Date : " + new SimpleDateFormat("dd-MM-yyy").format(startDate) + "  To Date : "
				+ new SimpleDateFormat("dd-MM-yyy").format(endDate));
		sheet = ExcelStyleSheet.doMerge(2, 2, 0, mergeRange - 1, sheet);

		// workbook.getSheet(sheetName).createFreezePane(0, 3);

		return workbook;
	}


	/**
	 * setting the message model here when list is empty
	 * 
	 * @return
	 */
	private MessageModel setMessageModelDataNotFound() {

		MessageModel model = new MessageModel();
		model.setMessage(configurableEnvironment.getProperty("no.data.found"));
		model.setStatusCode(204);
		return model;
	}


	@Override
	public String getRawDataReport(Integer formId,String startDate,String endDate, OAuth2Authentication oauth) throws Exception {
		try {

			Date startdateDate=null;
			Date enddateDate = null;
			String cellStartDate=null;
			String cellEndDate = null;
			if(startDate==null) {
				startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse("17-05-2020 00:00:00:000");
				cellStartDate="01-05-2020 00:00:00";
			}else {
				startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(startDate+" 00:00:00:000");
				cellStartDate = startDate+" 00:00:00";
			}
			
			if(endDate==null) {
				enddateDate=new Date();
				cellEndDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS").format(enddateDate);
			}else {
				enddateDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(endDate+" 23:59:59:999");
				cellEndDate =endDate+" 23:59:59";
			}
			
			
			UserModel principal = oAuth2Utility.getUserModel();
			
			List<AllChecklistFormData> fetchedSubmission = null;
			MatchOperation match = null;
			Aggregation resultQuery = null;
			if (principal.getDesgnName().equals("STATE LEVEL") || principal.getDesgnName().equals("ADMIN")) {
				
				match = Aggregation.match(
						Criteria.where("formId").is(formId).and("isValid").is(true).and("latest").is(true).and("syncDate").gte(startdateDate).lte(enddateDate));
				resultQuery = Aggregation.newAggregation(match);
				fetchedSubmission = mongoTemplate
						.aggregate(resultQuery, AllChecklistFormData.class, AllChecklistFormData.class)
						.getMappedResults();
			} else {
				if (principal.getAreaIds() != null) {
					match = Aggregation.match(
							Criteria.where("formId").is(formId).and("isValid").is(true).and("latest").is(true).and("data.undp_f1_q18").in(principal.getAreaIds()).and("syncDate").gte(startdateDate).lte(enddateDate));
					resultQuery = Aggregation.newAggregation(match);
					fetchedSubmission = mongoTemplate
							.aggregate(resultQuery, AllChecklistFormData.class, AllChecklistFormData.class)
							.getMappedResults();
				}
			}
			if(fetchedSubmission.isEmpty()) {
				return null;
			}
			List<Question> listOfFetchedQuestion = questionRepository.findAllByFormIdOrderByQuestionOrderAsc(formId);
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			SXSSFSheet sheet = workbook.createSheet("Covid19RMRS");
			CellStyle colStyleHeader = ExcelStyleSheet.getStyleForHeader(workbook,false);
			
			SXSSFCell cell1 = null;
			
			SXSSFRow row = null;
			int rowNum = 0;
			int colNum=0;
			
			row = sheet.createRow(rowNum);
			cell1=row.createCell(colNum);
			cell1.setCellValue("Start Date");
			sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
			cell1.setCellStyle(colStyleHeader);
			row.createCell(++colNum).setCellValue(cellStartDate);
			colNum=0;
			row = sheet.createRow(++rowNum);
			cell1=row.createCell(colNum);
			cell1.setCellValue("End Date");
			sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
			cell1.setCellStyle(colStyleHeader);
			row.createCell(++colNum).setCellValue(cellEndDate);
			
			
			 rowNum = 2;
			int questionCellCount = 0;
			SXSSFRow questionrow = sheet.createRow(rowNum);
			SXSSFRow hiddenRow = sheet.createRow(++rowNum);
			hiddenRow.setZeroHeight(true);
			
			 cell1 = questionrow.createCell(questionCellCount);//
			 cell1.setCellValue("Sl.No.");
			 sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
			 cell1.setCellStyle(colStyleHeader);
			++questionCellCount;
			 cell1 = questionrow.createCell(questionCellCount);//
			 cell1.setCellValue("Submitted By");
			 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
			 cell1.setCellStyle(colStyleHeader);
			++questionCellCount;
			 cell1 = questionrow.createCell(questionCellCount);//
			 cell1.setCellValue("Submitted Date");
			 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
			 cell1.setCellStyle(colStyleHeader);
			++questionCellCount;
			for (Question questions : listOfFetchedQuestion) {
				if (!questions.getControllerType().equals("heading")
						&& !questions.getControllerType().equals("beginrepeat")
						&& !questions.getRepeatSubSection().equalsIgnoreCase("yes")) {
					String hideCellValue = gethiddenCenValue(questions);
						 cell1 = questionrow.createCell(questionCellCount);//
						 cell1.setCellValue(questions.getQuestion());
						 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
						 cell1.setCellStyle(colStyleHeader);
						hiddenRow.createCell(questionCellCount).setCellValue(hideCellValue);
					questionCellCount++;
				}

			}
			List<Area> areaList = areaRepository.findAll();
			Map<Integer, String> areaMap = areaList.stream()
					.collect(Collectors.toMap(Area::getAreaId, Area::getAreaName,(area1, area2) -> area1));
			if (fetchedSubmission != null) {
				List<TypeDetail> typeDetailList = typeDetailRepository.findByFormId(formId);
				Map<Integer, TypeDetail> typeDetailMap = typeDetailList.stream()
						.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));
				//typeDetailMap=getDeletedTypeDetailId(typeDetailMap);
				Row dataRow = null;
				Row keyRow = sheet.getRow(3);
				int slno=1;
				for (AllChecklistFormData submissionData : fetchedSubmission) {
					dataRow = sheet.createRow(++rowNum);
					int cellCount = 0;
					dataRow.createCell(cellCount).setCellValue(slno);
					dataRow.createCell(++cellCount).setCellValue(submissionData.getUserName());
					dataRow.createCell(++cellCount).setCellValue(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(submissionData.getSyncDate()));
					++cellCount;
					
					for (Cell cell : keyRow) {

						if (cell.getStringCellValue().split("#")[1].equals("text")
								|| cell.getStringCellValue().split("#")[1].equals("tel")
								|| cell.getStringCellValue().split("#")[1].equals("date")) {
							dataRow.createCell(cellCount).setCellValue(
									(submissionData.getData().get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
													.equals("null"))
															? ""
															: submissionData.getData()
																	.get(cell.getStringCellValue().split("#")[0])
																	.toString());
						} else if (cell.getStringCellValue().split("#")[1].equals("area")) {
							if (submissionData.getData()
									.get(cell.getStringCellValue().split("#")[0]) instanceof List<?>) {
								StringBuilder areaName = new StringBuilder("");
								List<Integer> areaIds = (List<Integer>) submissionData.getData()
										.get(cell.getStringCellValue().split("#")[0]);
								for (Integer areaId : areaIds) {
									areaName.append(areaMap.get(areaId) + ", ");
								}
								dataRow.createCell(cellCount).setCellValue(
										(submissionData.getData().get(cell.getStringCellValue().split("#")[0]) == null
												|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
														.equals("null")) ? ""
																: Pattern.compile(", $").matcher(areaName)
																		.replaceAll(""));
							} else {

								if (cell.getStringCellValue().split("#")[2].equals("autoCompleteTextView")) {
									dataRow.createCell(cellCount).setCellValue((submissionData.getData()
											.get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
													.equals("null"))
															? ""
															: areaMap.get(Integer.parseInt(submissionData.getData()
																	.get(cell.getStringCellValue().split("#")[0])
																	.toString())));
								} else {
									dataRow.createCell(cellCount).setCellValue((submissionData.getData()
											.get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
													.equals("null"))
															? ""
															: areaMap.get(Integer.parseInt(submissionData.getData()
																	.get(cell.getStringCellValue().split("#")[0])
																	.toString())));
								}
							}
						} else if (cell.getStringCellValue().split("#")[1].equals("option")) {
							dataRow.createCell(cellCount).setCellValue(
									(submissionData.getData().get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData
													.getData().get(cell.getStringCellValue().split("#")[0]).equals(
															"null")) ? ""
																	: typeDetailMap
																			.get(Integer.parseInt(submissionData
																					.getData()
																					.get(cell.getStringCellValue()
																							.split("#")[0])
																					.toString()))
																			.getName());
						} else if (cell.getStringCellValue().split("#")[1].equals("checkbox")) {
							List<Integer> multiValue = (List<Integer>) submissionData.getData()
									.get(cell.getStringCellValue().split("#")[0]);
							if (multiValue == null) {
								dataRow.createCell(cellCount).setCellValue("");
							} else {
								StringBuilder skillName = new StringBuilder("");
								List<Integer> skillId = multiValue;
								for (Integer skill : skillId) {
									skillName.append(typeDetailMap.get(skill).getName() + ", ");
								}
									dataRow.createCell(cellCount).setCellValue(Pattern.compile(", $").matcher(skillName)
											.replaceAll(""));
							}
						}
						++cellCount;
					}
					++slno;
				}
			}

//			removeRow(sheet, 4);
			String dir = configurableEnvironment.getProperty("report.path");
			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();
			String formName = engineFormRepository.findByFormId(formId).getName();
			/**/

			String name = "Covid19_" + formName + "_" + areaMap.get(principal.getAreaIds().get(0)) + "_"
					+ new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xlsx";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();
//			return new ResponseEntity<String>(path, HttpStatus.OK);
			return path;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Action : While Generate rawdata report user with payload {},{}",SecurityContextHolder.getContext().getAuthentication().getName(), e);
			throw new RuntimeException(e);
		}

	}
	
	private Map<Integer, TypeDetail> getDeletedTypeDetailId(Map<Integer, TypeDetail> typeDetailMap) {
		String[] deletedTypeDetailMapping = configurableEnvironment.getProperty("tydetail.delete.id").split(",");
		for (String deletedTypeDetail : deletedTypeDetailMapping) {
			String[] idividualMapping = deletedTypeDetail.split("#");
			typeDetailMap.put(Integer.parseInt(idividualMapping[0]), typeDetailMap.get(Integer.parseInt(idividualMapping[1])));
		}
		return typeDetailMap;
	}



	private void removeRow(XSSFSheet sheet, int rowIndex) {
	    int lastRowNum = sheet.getLastRowNum();
	    if (rowIndex >= 0 && rowIndex < lastRowNum) {
	        sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
	    }
	    if (rowIndex == lastRowNum) {
	        Row removingRow = sheet.getRow(rowIndex);
	        if (removingRow != null) {
	            sheet.removeRow(removingRow);
	        }
	    }
	}


private String gethiddenCenValue(Question questions) {
	String value = null;
	
	if(questions.getTableName()!=null) {
		value = questions.getColumnName()+"#"+questions.getTableName().split("\\$\\$")[0].trim()+"#"+questions.getControllerType();
	}else {
		value = questions.getColumnName()+"#"+questions.getFieldType();
	}
	
		return value;
	}

@Override
public String getAreaAndUserWiseRawDataReport(Integer reportType,String startDate,String endDate,OAuth2Authentication oauth) {
	String reportName=null;
	if(reportType==1) {
		reportName="Covid19_AreaWise_Report_";
	}else {
		reportName="Covid19_UserWise_Report_";
	}

	try {
		Date startdateDate=null;
		Date enddateDate = null;
		String cellStartDate=null;
		String cellEndDate = null;
		if(startDate==null) {
			startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse("17-05-2020 00:00:00:000");
			cellStartDate="01-05-2020 00:00:00";
		}else {
			startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(startDate+" 00:00:00:000");
			cellStartDate = startDate+" 00:00:00";
		}
		
		if(endDate==null) {
			enddateDate=new Date();
			cellEndDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS").format(enddateDate);
		}else {
			enddateDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(endDate+" 23:59:59:999");
			cellEndDate =endDate+" 23:59:59";
		}
	
		UserModel principal = oAuth2Utility.getUserModel();
//		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		CellStyle colStyleHeader = ExcelStyleSheet.getStyleForHeader(workbook,false);
	List<TypeDetail> listOfTypeDetails = typeDetailRepository.findByType(typeRepository.findByTypeName("placeOfEntry Type"));
	SXSSFSheet sheet =null;
	Row row = null;
	Cell col = null;
	int rowNum = 0;
	int colNum=0;
	Cell cell1=null;
	List<Map> reportMap = new ArrayList<>();
	if(reportType==1) {
		
				if (principal.getDesgnName().equals("STATE LEVEL") || principal.getDesgnName().equals("ADMIN")) {
					reportMap.addAll(getDistrictMap("undp_f1_q17", startdateDate, enddateDate, null, null));// District
					reportMap.addAll(getDistrictMap("undp_f1_q18", startdateDate, enddateDate, null, null));// District

				} else {
					if (principal.getAreaIds() != null) {
						reportMap.addAll(getDistrictMap("undp_f1_q18", startdateDate, enddateDate, "undp_f1_q18",
								principal.getAreaIds()));// District
						reportMap.addAll(getAreaMap(80, "undp_f1_tahasil", startdateDate, enddateDate, "undp_f1_q18",
								principal.getAreaIds()));// TAHASIL
						reportMap.addAll(getAreaMap(80, "undp_f1_q19", startdateDate, enddateDate, "undp_f1_q18",
								principal.getAreaIds()));// BLOCK
						reportMap.addAll(getAreaMap(81, "undp_f1_q20_w1", startdateDate, enddateDate, "undp_f1_q18",
								principal.getAreaIds()));// ULBs
					}
				}
		
		if(reportMap.isEmpty()) {
			return null;
		}
		
		sheet = workbook.createSheet("AreaWiseSubmission");
		
		row = sheet.createRow(rowNum);
		cell1=row.createCell(colNum);
		cell1.setCellValue("Start Date");
		sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
		cell1.setCellStyle(colStyleHeader);
		row.createCell(++colNum).setCellValue(cellStartDate);
		colNum=0;
		row = sheet.createRow(++rowNum);
		cell1=row.createCell(colNum);
		cell1.setCellValue("End Date");
		sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
		cell1.setCellStyle(colStyleHeader);
		row.createCell(++colNum).setCellValue(cellEndDate);
		
		colNum=0;
		row = sheet.createRow(++rowNum);
		cell1=row.createCell(colNum);
		cell1.setCellValue("Sl.No.");
		sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
		cell1.setCellStyle(colStyleHeader);
		cell1=row.createCell(++colNum);		
		cell1.setCellValue("Area Level");
		sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
		cell1.setCellStyle(colStyleHeader);
		cell1=row.createCell(++colNum);
		cell1.setCellValue("Area Name");
		sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
		cell1.setCellStyle(colStyleHeader);
		
		for (TypeDetail typeDetail : listOfTypeDetails) {
			cell1=row.createCell(++colNum);
			cell1.setCellValue(typeDetail.getName());
			sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
			cell1.setCellStyle(colStyleHeader);
		}
		cell1=row.createCell(++colNum);
		cell1.setCellValue("Total Count");
		sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
		cell1.setCellStyle(colStyleHeader);
		
		Row keyRow = sheet.getRow(rowNum);
	
		
		Set<String> areaIdSet = new HashSet<>();
		int slno=1;
		for (Map map : reportMap) {
			
			Map<String,Object> value = map;
			if(!areaIdSet.contains(value.get("_id").toString())) {
				areaIdSet.add(value.get("_id").toString());
		int colCount=0;
		row = sheet.createRow(++rowNum);
		row.createCell(colCount).setCellValue(slno);++colCount;
		if(value.get("areaLevel")==null) {
			if(value.get("_id").toString().equals("600005")) {
				row.createCell(colCount).setCellValue("TAHASIL");++colCount;
			}else if(value.get("_id").toString().equals("600001")) {
				row.createCell(colCount).setCellValue("BLOCK");++colCount;
			}else if(value.get("_id").toString().equals("600002")) {
				row.createCell(colCount).setCellValue("ULB");++colCount;
			}
		}else {
		row.createCell(colCount).setCellValue(value.get("areaLevel").toString());++colCount;
		}
		row.createCell(colCount).setCellValue(value.get("area").toString());++colCount;
					for (int i = 0; i <= listOfTypeDetails.size() - 1; i++) {
						if (keyRow.getCell(colCount).getStringCellValue().contains("Transit")) {

							row.createCell(colCount).setCellValue((int)value.get("Transit"));
						} else if (keyRow.getCell(colCount).getStringCellValue().contains("Individual")) {
							row.createCell(colCount).setCellValue((int)value.get("Individual"));
						} else if (keyRow.getCell(colCount).getStringCellValue().contains("Shelter")) {
							row.createCell(colCount).setCellValue((int)value.get("Shelter"));
						}
						++colCount;
					}
					row.createCell(colCount).setCellValue((int)value.get("Transit")+(int)value.get("Individual")+(int)value.get("Shelter"));
			slno++;
		}
		}
				
				
		
	}else if(reportType==2) {
		reportMap=new ArrayList<>();
		reportMap.addAll(getUserWiseReport(startdateDate,enddateDate,null,null));
		
		if(reportMap.isEmpty()) {
			return null;
		}
		
		List<Area> areaList = areaRepository.findAllByAreaLevelAreaLevelIdIn(Arrays.asList(1,2));
		Map<Integer, String> areaMap = areaList.stream()
				.collect(Collectors.toMap(Area::getAreaId, Area::getAreaName,(area1, area2) -> area1));
		
		Map<String,Integer> userMap = oAuth2Utility.userWiseArea();
		sheet = workbook.createSheet("UserWiseSubmission");
		
		row = sheet.createRow(rowNum);
		cell1=row.createCell(colNum);
		cell1.setCellValue("Start Date");
		sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
		cell1.setCellStyle(colStyleHeader);
		row.createCell(++colNum).setCellValue(cellStartDate);
		colNum=0;
		row = sheet.createRow(++rowNum);
		cell1=row.createCell(colNum);
		cell1.setCellValue("End Date");
		sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
		cell1.setCellStyle(colStyleHeader);
		row.createCell(++colNum).setCellValue(cellEndDate);
		
		colNum=0;
		row = sheet.createRow(++rowNum);
		cell1=row.createCell(colNum);
		cell1.setCellValue("Sl.No.");
		sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
		cell1.setCellStyle(colStyleHeader);
		cell1=row.createCell(++colNum);
		cell1.setCellValue("Area Name");
		sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
		cell1.setCellStyle(colStyleHeader);
		cell1=row.createCell(++colNum);
		cell1.setCellValue("User Name");
		sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
		cell1.setCellStyle(colStyleHeader);
		
		for (TypeDetail typeDetail : listOfTypeDetails) {
			cell1=row.createCell(++colNum);
			cell1.setCellValue(typeDetail.getName());
			sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
			cell1.setCellStyle(colStyleHeader);
		}
		cell1=row.createCell(++colNum);
		cell1.setCellValue("Total Count");
		sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
		cell1.setCellStyle(colStyleHeader);
		
		Row keyRow = sheet.getRow(rowNum);
		int slno=1;
		
		if (principal.getDesgnName().equals("STATE LEVEL") || principal.getDesgnName().equals("ADMIN")) {
			
			for (Map map : reportMap) {
				Map<String,Object> value = map;
			int colCount=0;
			row = sheet.createRow(++rowNum);
			row.createCell(colCount).setCellValue(slno);++colCount;
			row.createCell(colCount).setCellValue(areaMap.get(userMap.get(value.get("_id").toString())));++colCount;
			row.createCell(colCount).setCellValue(value.get("_id").toString());++colCount;
						for (int i = 0; i <= listOfTypeDetails.size() - 1; i++) {
							if (keyRow.getCell(colCount).getStringCellValue().contains("Transit")) {

								row.createCell(colCount).setCellValue((int)value.get("Transit"));
							} else if (keyRow.getCell(colCount).getStringCellValue().contains("Individual")) {
								row.createCell(colCount).setCellValue((int)value.get("Individual"));
							} else if (keyRow.getCell(colCount).getStringCellValue().contains("Shelter")) {
								row.createCell(colCount).setCellValue((int)value.get("Shelter"));
							}
							++colCount;
						}
						row.createCell(colCount).setCellValue((int)value.get("Transit")+(int)value.get("Individual")+(int)value.get("Shelter"));
				slno++;
			}
			
		} else {
			if (principal.getAreaIds() != null) {
				Integer districtAreaId = null;
				if(principal.getAreaIds().size()>1) {
					districtAreaId=principal.getAreaIds().get(1);
				}else {
					districtAreaId=principal.getAreaIds().get(0);
				}
				for (Map map : reportMap) {
					Map<String,Object> value = map;
					if(districtAreaId.toString().equals(userMap.get(value.get("_id").toString()).toString())) {
				int colCount=0;
				row = sheet.createRow(++rowNum);
				row.createCell(colCount).setCellValue(slno);++colCount;
				row.createCell(colCount).setCellValue(areaMap.get(userMap.get(value.get("_id").toString())));++colCount;
				row.createCell(colCount).setCellValue(value.get("_id").toString());++colCount;
							for (int i = 0; i <= listOfTypeDetails.size() - 1; i++) {
								if (keyRow.getCell(colCount).getStringCellValue().contains("Transit")) {

									row.createCell(colCount).setCellValue((int)value.get("Transit"));
								} else if (keyRow.getCell(colCount).getStringCellValue().contains("Individual")) {
									row.createCell(colCount).setCellValue((int)value.get("Individual"));
								} else if (keyRow.getCell(colCount).getStringCellValue().contains("Shelter")) {
									row.createCell(colCount).setCellValue((int)value.get("Shelter"));
								}
								++colCount;
							}
							row.createCell(colCount).setCellValue((int)value.get("Transit")+(int)value.get("Individual")+(int)value.get("Shelter"));
					slno++;
					}
				}
				
			}
		}
	}
	
	String dir = configurableEnvironment.getProperty("report.path");
	File file = new File(dir);
	if (!file.exists())
		file.mkdirs();
	String name = reportName + new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xlsx";
	String path = dir + "" + name;

	FileOutputStream fos = new FileOutputStream(new File(path));
	workbook.write(fos);
	fos.close();
	workbook.close();
	return path;
	} catch (Exception e) {
		e.printStackTrace();
		log.error("Action : While Generate "+reportName+" report user with payload {},{}",SecurityContextHolder.getContext().getAuthentication().getName(), e);
		throw new RuntimeException(e);
	}
}



	private List<Map> getUserWiseReport(Date startdateDate, Date enddateDate, String districtCol, List<Integer> districtId) {

		MatchOperation match = null;
				if(districtCol==null && startdateDate!=null && enddateDate!=null ) {
					match = Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1).and("syncDate").gte(startdateDate).lt(enddateDate));
				}else if(districtCol!=null && startdateDate!=null && enddateDate!=null ) {
					match = Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1).and("data."+districtCol).in(districtId).and("syncDate").gte(startdateDate).lt(enddateDate));
				}else if(districtCol==null && startdateDate==null && enddateDate==null ) {
					match = Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1));
				}
		//ProjectionOperation dataProject = Aggregation.project("data");
		
		ProjectionOperation projection = Aggregation.project().and("userName").as("user")
				.and(when(where("data.undp_f1_q0").is(1)).then(1).otherwise(0)).as("Shelter")
				.and(when(where("data.undp_f1_q0").is(2)).then(1).otherwise(0)).as("Transit")
				.and(when(where("data.undp_f1_q0").is(3)).then(1).otherwise(0)).as("Individual");

		GroupOperation group = Aggregation.group("user").sum("Shelter").as("Shelter").sum("Transit")
				.as("Transit").sum("Individual").as("Individual");

ProjectionOperation projectParentArea = Aggregation.project().and("user").as("user")
.and("Shelter").as("Shelter")
.and("Transit").as("Transit").and("Individual").as("Individual");

Aggregation resultQuery=Aggregation.newAggregation(match, projection, group, projectParentArea);

List<Map> reportMap = mongoTemplate.aggregate(resultQuery, AllChecklistFormData.class, Map.class).getMappedResults();
return reportMap;
}



	private List<Map> getAreaMap(Integer matchValue,String projectionValue,Date startdateDate,Date enddateDate, String districtCol, List<Integer> districtid) {
		MatchOperation match = null;
		if(districtCol==null) {
			match =	Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1).and("data.undp_f1_q18_1").is(matchValue).and("syncDate").gte(startdateDate).lt(enddateDate));
		}else {
			match =	Aggregation.match(Criteria.where("isValid").is(true).and("latest").is(true).and("formId").is(1).and("data."+districtCol).in(districtid).and("data.undp_f1_q18_1").is(matchValue).and("syncDate").gte(startdateDate).lt(enddateDate));
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

	private List<Map> getDistrictMap(String projectionValue,Date startdateDate,Date enddateDate, String districtCol, List<Integer> districtid) {
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


	@Override
	public Map<Integer, ReportChartModel> getDataForCard(OAuth2Authentication oauth) {
		
		UserModel principal = oAuth2Utility.getUserModel();
//		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		List<Map> reportMap = new ArrayList<>();
		ReportChartModel reportChartModel = null;
		Map<Integer, ReportChartModel> reportchartModelMap  = new HashMap<>();
		Map<String,Object> value = null;
		Map<String,Object> uservalue = null;
		int totalUserCount = oAuth2Utility.userCount();
		if (principal.getDesgnName().equals("STATE LEVEL") || principal.getDesgnName().equals("ADMIN")) {
			
			
			reportMap.addAll(reportUtil.getDistrictMap("undp_f1_q17",null,null,null,null));//District
			value = reportMap.get(0);
			reportChartModel = new ReportChartModel();
			reportChartModel.setIndividualCount((int)value.get("Individual"));
			reportChartModel.setShelterCount((int)value.get("Shelter"));
			reportChartModel.setTransitCountCount((int)value.get("Transit"));
			reportChartModel.setTotalDataCount((int)value.get("Transit")+(int)value.get("Individual")+(int)value.get("Shelter"));
			reportChartModel.setTotaluserCount(totalUserCount);
			reportchartModelMap.put(1, reportChartModel);
			
			
			//userWise
			reportMap = new ArrayList<>();
			reportMap.addAll(getUserWiseReport(null,null,null,null));
			int shelterCount=0;
			int transitCount=0;
			int indivisualCount=0;
		Set<String> userSet = new HashSet<>();
			for (Map map : reportMap) {
				uservalue = map;
				shelterCount += (int)uservalue.get("Shelter");
				transitCount += (int)uservalue.get("Transit");
				indivisualCount += (int)uservalue.get("Individual");
				userSet.add(uservalue.get("_id").toString());
			}
			reportChartModel = new ReportChartModel();
			reportChartModel.setIndividualCount(shelterCount);
			reportChartModel.setShelterCount(transitCount);
			reportChartModel.setTransitCountCount(indivisualCount);
			reportChartModel.setTotalDataCount(shelterCount+transitCount+indivisualCount);
			reportChartModel.setTotalActiveuserCount(userSet.size());
			reportChartModel.setTotaluserCount(totalUserCount);
			reportchartModelMap.put(2, reportChartModel);
			
			
			
		} else {
			if (principal.getAreaIds() != null) {
				
				reportMap.addAll(reportUtil.getDistrictMap("undp_f1_q18",null,null,"undp_f1_q18",principal.getAreaIds()));//District
				value = reportMap.get(0);
				reportChartModel = new ReportChartModel();
				reportChartModel.setIndividualCount((int)value.get("Individual"));
				reportChartModel.setShelterCount((int)value.get("Shelter"));
				reportChartModel.setTransitCountCount((int)value.get("Transit"));
				reportChartModel.setTotalDataCount((int)value.get("Transit")+(int)value.get("Individual")+(int)value.get("Shelter"));
				reportChartModel.setTotaluserCount(totalUserCount);
				reportchartModelMap.put(1, reportChartModel);
				
				
				//userWise
				reportMap = new ArrayList<>();
				reportMap.addAll(getUserWiseReport(null,null,null,null));
				int shelterCount=0;
				int transitCount=0;
				int indivisualCount=0;
			Set<String> userSet = new HashSet<>();
			
			Integer districtAreaId = null;
			if(principal.getAreaIds().size()>1) {
				districtAreaId=principal.getAreaIds().get(1);
			}else {
				districtAreaId=principal.getAreaIds().get(0);
			}
			Map<String,Integer> userMap = oAuth2Utility.userWiseArea();
				for (Map map : reportMap) {
					uservalue = map;
					if(districtAreaId.toString().equals(userMap.get(uservalue.get("_id").toString()).toString())) {
					shelterCount += (int)uservalue.get("Shelter");
					transitCount += (int)uservalue.get("Transit");
					indivisualCount += (int)uservalue.get("Individual");
					userSet.add(uservalue.get("_id").toString());
					}
				}
				reportChartModel = new ReportChartModel();
				reportChartModel.setIndividualCount(shelterCount);
				reportChartModel.setShelterCount(transitCount);
				reportChartModel.setTransitCountCount(indivisualCount);
				reportChartModel.setTotalDataCount(shelterCount+transitCount+indivisualCount);
				reportChartModel.setTotalActiveuserCount(userSet.size());
				reportChartModel.setTotaluserCount(totalUserCount);
				reportchartModelMap.put(2, reportChartModel);
				
			}
		}
		return reportchartModelMap;
	}



	@Override
	public String getRawDataReportId(Integer formId, String startDate, String endDate, OAuth2Authentication oauth) {
		try {

			Date startdateDate=null;
			Date enddateDate = null;
			String cellStartDate=null;
			String cellEndDate = null;
			if(startDate==null) {
				startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse("17-05-2020 00:00:00:000");
				cellStartDate="01-05-2020 00:00:00";
			}else {
				startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(startDate+" 00:00:00:000");
				cellStartDate = startDate+" 00:00:00";
			}
			
			if(endDate==null) {
				enddateDate=new Date();
				cellEndDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS").format(enddateDate);
			}else {
				enddateDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(endDate+" 23:59:59:999");
				cellEndDate =endDate+" 23:59:59";
			}
			
			
			UserModel principal = oAuth2Utility.getUserModel();
//			List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
			
			List<AllChecklistFormData> fetchedSubmission = null;
			MatchOperation match = null;
			Aggregation resultQuery = null;
			if (principal.getDesgnName().equals("STATE LEVEL") || principal.getDesgnName().equals("ADMIN")) {
				
				match = Aggregation.match(
						Criteria.where("formId").is(formId).and("isValid").is(true).and("latest").is(true).and("syncDate").gte(startdateDate).lte(enddateDate));
				resultQuery = Aggregation.newAggregation(match);
				fetchedSubmission = mongoTemplate
						.aggregate(resultQuery, AllChecklistFormData.class, AllChecklistFormData.class)
						.getMappedResults();
			} 
			else {
				if (principal.getAreaIds() != null) {
					match = Aggregation.match(
							Criteria.where("formId").is(formId).and("isValid").is(true).and("latest").is(true).and("data.undp_f1_q18").in(principal.getAreaIds()).and("syncDate").gte(startdateDate).lte(enddateDate));
					resultQuery = Aggregation.newAggregation(match);
					fetchedSubmission = mongoTemplate
							.aggregate(resultQuery, AllChecklistFormData.class, AllChecklistFormData.class)
							.getMappedResults();
				}
			}
			List<Question> listOfFetchedQuestion = questionRepository.findAllByFormIdOrderByQuestionOrderAsc(formId);
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			SXSSFSheet sheet = workbook.createSheet("Covid19RMRS");
//			SXSSFCellStyle cellstyleMiddle = ExcelStyleSheet.getStyleForLeftMiddle(workbook);
			CellStyle colStyleHeader = ExcelStyleSheet.getStyleForHeader(workbook,false);
			
			SXSSFCell cell1 = null;
			
			SXSSFRow row = null;
			int rowNum = 0;
			int colNum=0;
			
			row = sheet.createRow(rowNum);
			cell1=row.createCell(colNum);
			cell1.setCellValue("Start Date");
			sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
			cell1.setCellStyle(colStyleHeader);
//			sheet.autoSizeColumn(colNum);
			row.createCell(++colNum).setCellValue(cellStartDate);
			colNum=0;
			row = sheet.createRow(++rowNum);
			cell1=row.createCell(colNum);
			cell1.setCellValue("End Date");
			sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
			cell1.setCellStyle(colStyleHeader);
//			sheet.autoSizeColumn(colNum);
			row.createCell(++colNum).setCellValue(cellEndDate);
			
			
			 rowNum = 2;
			int questionCellCount = 0;
			SXSSFRow questionrow = sheet.createRow(rowNum);
			//SXSSFRow subQuestionrow = sheet.createRow(++rowNum);
			SXSSFRow hiddenRow = sheet.createRow(++rowNum);
			hiddenRow.setZeroHeight(true);
			
			//cell1 = subQuestionrow.createCell(questionCellCount);
			//sheet.addMergedRegion(new CellRangeAddress(2, 3, questionCellCount, questionCellCount));
			 cell1 = questionrow.createCell(questionCellCount);//
			 cell1.setCellValue("Sl.No.");
			 sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
			 cell1.setCellStyle(colStyleHeader);
			++questionCellCount;
			//cell1 = subQuestionrow.createCell(questionCellCount);
			//sheet.addMergedRegion(new CellRangeAddress(2, 3, questionCellCount, questionCellCount));
			 cell1 = questionrow.createCell(questionCellCount);//
			 cell1.setCellValue("Submitted By");
			 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
			 cell1.setCellStyle(colStyleHeader);
			++questionCellCount;
			//cell1 = subQuestionrow.createCell(questionCellCount);
			//sheet.addMergedRegion(new CellRangeAddress(2, 3, questionCellCount, questionCellCount));
			 cell1 = questionrow.createCell(questionCellCount);//
			 cell1.setCellValue("Submitted Date");
			 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
			 cell1.setCellStyle(colStyleHeader);
			++questionCellCount;
			for (Question questions : listOfFetchedQuestion) {
				if (!questions.getControllerType().equals("heading")
						&& !questions.getControllerType().equals("beginrepeat")
						&& !questions.getRepeatSubSection().equalsIgnoreCase("yes")) {
					String hideCellValue = gethiddenCenValue(questions);
					//if (!questions.getFieldType().equals("checkbox")) {
						//cell1 = subQuestionrow.createCell(questionCellCount);
						//sheet.addMergedRegion(new CellRangeAddress(2, 3, questionCellCount, questionCellCount));
						 cell1 = questionrow.createCell(questionCellCount);//
						 cell1.setCellValue(questions.getQuestion());
						 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
						 cell1.setCellStyle(colStyleHeader);
						hiddenRow.createCell(questionCellCount).setCellValue(hideCellValue);
					/*} else {
						List<TypeDetail> listOfTypeDetails = typeDetailRepository.findByType(questions.getTypeId());
						int oldquestionCellCount = questionCellCount;
						int subQuestionCellCount = questionCellCount;
						sheet.addMergedRegion(new CellRangeAddress(2, 2, questionCellCount,
								(questionCellCount + listOfTypeDetails.size() - 1)));
						 cell1 = questionrow.createCell(questionCellCount);
						 cell1.setCellValue(questions.getQuestion());
						 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
						 cell1.setCellStyle(colStyleHeader);

						for (TypeDetail type : listOfTypeDetails) {
							 cell1 = subQuestionrow.createCell(subQuestionCellCount);
							 cell1.setCellValue(type.getName());
							 sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
							 cell1.setCellStyle(colStyleHeader);
//							sheet.autoSizeColumn(subQuestionCellCount);
							hiddenRow.createCell(subQuestionCellCount)
									.setCellValue(hideCellValue + "#" + type.getSlugId());
							subQuestionCellCount++;
						}

						questionCellCount = oldquestionCellCount + listOfTypeDetails.size() - 1;

					}*/

					questionCellCount++;
				}

			}
		/*	List<Area> areaList = areaRepository.findAll();
			Map<Integer, String> areaMap = areaList.stream()
					.collect(Collectors.toMap(Area::getAreaId, Area::getAreaName,(area1, area2) -> area1));*/
			if (fetchedSubmission != null) {
				/*List<TypeDetail> typeDetailList = typeDetailRepository.findByFormId(formId);
				Map<Integer, TypeDetail> typeDetailMap = typeDetailList.stream()
						.collect(Collectors.toMap(TypeDetail::getSlugId, typeDe -> typeDe));*/
				Row dataRow = null;
				Row keyRow = sheet.getRow(3);
				int slno=1;
				for (AllChecklistFormData submissionData : fetchedSubmission) {
					dataRow = sheet.createRow(++rowNum);
					int cellCount = 0;
					dataRow.createCell(cellCount).setCellValue(slno);
					dataRow.createCell(++cellCount).setCellValue(submissionData.getUserName());
					dataRow.createCell(++cellCount).setCellValue(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(submissionData.getSyncDate()));
					++cellCount;
					
					for (Cell cell : keyRow) {

						if (cell.getStringCellValue().split("#")[1].equals("text")
								|| cell.getStringCellValue().split("#")[1].equals("tel")
								|| cell.getStringCellValue().split("#")[1].equals("date")) {
							dataRow.createCell(cellCount).setCellValue(
									(submissionData.getData().get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
													.equals("null"))
															? ""
															: submissionData.getData()
																	.get(cell.getStringCellValue().split("#")[0])
																	.toString());
						} else if (cell.getStringCellValue().split("#")[1].equals("area")) {
							if (submissionData.getData()
									.get(cell.getStringCellValue().split("#")[0]) instanceof List<?>) {
								StringBuilder areaName = new StringBuilder("");
								List<Integer> areaIds = (List<Integer>) submissionData.getData()
										.get(cell.getStringCellValue().split("#")[0]);
								for (Integer areaId : areaIds) {
									//areaName.append(areaMap.get(areaId) + ", ");
								}
								dataRow.createCell(cellCount).setCellValue(
										(submissionData.getData().get(cell.getStringCellValue().split("#")[0]) == null
												|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
														.equals("null")) ? ""
																: Pattern.compile(", $").matcher(areaName)
																		.replaceAll(""));
							} else {

								if (cell.getStringCellValue().split("#")[2].equals("autoCompleteTextView")) {
									dataRow.createCell(cellCount).setCellValue((submissionData.getData()
											.get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
													.equals("null"))
															? ""
															:submissionData.getData().get(cell.getStringCellValue().split("#")[0]).toString());
								} else {
									dataRow.createCell(cellCount).setCellValue((submissionData.getData()
											.get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData.getData().get(cell.getStringCellValue().split("#")[0])
													.equals("null"))
															? ""
															: submissionData.getData().get(cell.getStringCellValue().split("#")[0]).toString());
								}
							}
						} else if (cell.getStringCellValue().split("#")[1].equals("option")) {
							dataRow.createCell(cellCount).setCellValue(
									(submissionData.getData().get(cell.getStringCellValue().split("#")[0]) == null
											|| submissionData
													.getData().get(cell.getStringCellValue().split("#")[0]).equals(
															"null")) ? ""
																	: submissionData.getData().get(cell.getStringCellValue().split("#")[0]).toString());
						} else if (cell.getStringCellValue().split("#")[1].equals("checkbox")) {
							List<Integer> multiValue = (List<Integer>) submissionData.getData()
									.get(cell.getStringCellValue().split("#")[0]);
							if (multiValue == null) {
								dataRow.createCell(cellCount).setCellValue("");
							} else {
								/*if (multiValue.contains(Integer.parseInt(cell.getStringCellValue().split("#")[2]))) {
									// dataRow.createCell(cellCount).setCellValue(submissionData.getData().get(cell.getStringCellValue().split("#")[0])==null?"N/A":typeDetailMap.get(Integer.parseInt(cell.getStringCellValue().split("#")[3])).getName());
									dataRow.createCell(cellCount).setCellValue("YES");
								} else {*/
								
								StringBuilder strbul  = new StringBuilder();
							     Iterator<Integer> iter = multiValue.iterator();
							     while(iter.hasNext())
							     {
							         strbul.append(iter.next());
							        if(iter.hasNext()){
							         strbul.append(",");
							        }
							     }
							 strbul.toString();
									dataRow.createCell(cellCount).setCellValue(strbul.toString());
								//}
							}
						}
						++cellCount;
					}
					++slno;
				}
			}

//			removeRow(sheet, 4);
			String dir = configurableEnvironment.getProperty("report.path");
			File file = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!file.exists())
				file.mkdirs();
			String formName = engineFormRepository.findByFormId(formId).getName();
			/**/

			String name = "Covid19_" + formName + "_" +/* areaMap.get(principal.getAreaIds().get(0)) + "_"
					+*/ new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xlsx";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
			workbook.write(fos);
			fos.close();
			workbook.close();
//			return new ResponseEntity<String>(path, HttpStatus.OK);
			return path;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Action : While Generate rawdata report user with payload {},{}",SecurityContextHolder.getContext().getAuthentication().getName(), e);
			throw new RuntimeException(e);
		}

	}



	@Override
	public String getAreaWiseSkillReport(Integer formId, String startDate, String endDate, OAuth2Authentication oauth) {
		try {
			Date startdateDate = null;
			Date enddateDate = null;
			String cellStartDate = null;
			String cellEndDate = null;
			if(startDate==null) {
				startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse("17-05-2020 00:00:00:000");
				cellStartDate="01-05-2020 00:00:00";
			}else {
				startdateDate=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(startDate+" 00:00:00:000");
				cellStartDate = startDate+" 00:00:00";
			}
			
			if(endDate==null) {
				enddateDate=new Date();
				cellEndDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS").format(enddateDate);
			}else {
				enddateDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").parse(endDate+" 23:59:59:999");
				cellEndDate =endDate+" 23:59:59";
			}

			UserModel principal = oAuth2Utility.getUserModel();
//			List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));

			Map<String,Map<String,String>> dataMap= new TreeMap<>();
//			Map<String,String> valueMap = null;
			
			
//			List<DataValue> dataValueList =null;
			
			
			if (principal.getDesgnName().equals("STATE LEVEL") || principal.getDesgnName().equals("ADMIN")) {
				dataMap= getAgregatedData("data.undp_f1_q18",null,startdateDate,enddateDate);
				
			} else {
				if (principal.getAreaIds() != null) {
					dataMap= getAgregatedData("data.undp_f1_q18",principal.getAreaIds().size() > 1? principal.getAreaIds().get(1) :
						principal.getAreaIds().get(0),startdateDate,enddateDate);
				}
			}
			
			if(dataMap.isEmpty()) {
				return null;
			}
			
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			CellStyle colStyleHeader = ExcelStyleSheet.getStyleForHeader(workbook, false);
			
			
			SXSSFSheet sheet = workbook.createSheet("Data");
			Row row = null;
//			Cell col = null;
			int rowNum = 0;
			int colNum = 0;
			Cell cell1 = null;
			
			
//			if(!dataValueList.isEmpty()) {
				List<TypeDetail> listOfTypeDetails = typeDetailRepository
						.findByType(typeRepository.findByTypeName("Employed Type"));
				row = sheet.createRow(rowNum);
				cell1=row.createCell(colNum);
				cell1.setCellValue("Start Date");
				sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
				cell1.setCellStyle(colStyleHeader);
				row.createCell(++colNum).setCellValue(cellStartDate);
				colNum=0;
				row = sheet.createRow(++rowNum);
				cell1=row.createCell(colNum);
				cell1.setCellValue("End Date");
				sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
				cell1.setCellStyle(colStyleHeader);
				row.createCell(++colNum).setCellValue(cellEndDate);
				
				colNum=0;
				row = sheet.createRow(++rowNum);
				cell1=row.createCell(colNum);
				cell1.setCellValue("Sl.No.");
				sheet.setColumnWidth(cell1.getColumnIndex(), 4000);
				cell1.setCellStyle(colStyleHeader);
				
				
				cell1=row.createCell(++colNum);
				cell1.setCellValue("Skill Name");
				sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*200));
				cell1.setCellStyle(colStyleHeader);
				int slno=1;
				for (TypeDetail typeDetail : listOfTypeDetails) {
					int colCount=0;
					row = sheet.createRow(++rowNum);
					row.createCell(colCount).setCellValue(slno);
					cell1=row.createCell(++colCount);
					cell1.setCellValue(typeDetail.getName());
					sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*200));
					//cell1.setCellStyle(colStyleSkill);
					
					cell1=row.createCell(++colCount);
					cell1.setCellValue(String.valueOf(typeDetail.getSlugId()));
					++slno;
				}
				sheet.setColumnHidden(2, true);
				
//			List<Area> areaList = areaRepository.findByParentAreaIdOrderByAreaName(1);
//			Map<Integer, String> areaMap = areaList.stream()
//					.collect(Collectors.toMap(Area::getAreaId, Area::getAreaName,(area1, area2) -> area1));
			/*StringBuffer areaName = null;
			for (DataValue dataValue : dataValueList) {
				areaName = new StringBuffer();
				areaName.append(areaMap.get(dataValue.getAreaId()));
				if(dataMap.containsKey(areaName.toString())) {
					dataMap.get(areaName.toString()).put(dataValue.getNtypeDetailId(), dataValue.getNumerator());
					
				}else {
					valueMap = new HashMap<>();
					valueMap.put(dataValue.getNtypeDetailId(), dataValue.getNumerator());
					dataMap.put(areaName.toString(), valueMap);
				}
				
			}*/
			
			
//			 TreeMap<String,Map<String,String>> sortedMap = new TreeMap<>(); 
//			 sortedMap.putAll(dataMap);
			
		
			Map<String,Integer> totalCountMap = new HashMap<>();
			 int colCount=3;
			 for (Entry<String, Map<String, String>> areaEntry : dataMap.entrySet()) {
				 int keyRowNum = 2;
				 Row row1 = sheet.getRow(keyRowNum);
				cell1 = row1.createCell(colCount);
				cell1.setCellValue(areaEntry.getKey());
				sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
				cell1.setCellStyle(colStyleHeader);
				++keyRowNum;
				
				for (int i = 0; i <= listOfTypeDetails.size() - 1; i++) {
					 row1 = sheet.getRow(keyRowNum);
					 int value = areaEntry.getValue().get((row1.getCell(2).getStringCellValue()))==null ? 0 : Integer.parseInt(areaEntry.getValue().get((row1.getCell(2).getStringCellValue())));
					row1.createCell(colCount).setCellValue(value);
					if(totalCountMap.containsKey(row1.getCell(2).getStringCellValue())){
						totalCountMap.put(row1.getCell(2).getStringCellValue(), totalCountMap.get(row1.getCell(2).getStringCellValue())+value);
					}else {
						totalCountMap.put(row1.getCell(2).getStringCellValue(), value);
					}
					++keyRowNum;
				}
					++colCount;
			    }
			 
			 int keyRowNum = 2;
			 Row row1 = sheet.getRow(keyRowNum);
			cell1 = row1.createCell(colCount);
			cell1.setCellValue("Total");
			sheet.setColumnWidth(cell1.getColumnIndex(),(cell1.getStringCellValue().length()*300));
			cell1.setCellStyle(colStyleHeader);
			++keyRowNum;
			for (int i = 0; i <= listOfTypeDetails.size() - 1; i++) {
				 row1 = sheet.getRow(keyRowNum);
				row1.createCell(colCount).setCellValue(totalCountMap.get(row1.getCell(2).getStringCellValue()));
				++keyRowNum;
			}
//		}
			
			
			String dir = configurableEnvironment.getProperty("report.path");
			File file = new File(dir);
			if (!file.exists())
				file.mkdirs();
			//String formName = engineFormRepository.findByFormId(formId).getName();
		

			String name = "Covid19_Skillwise_" + new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date()) + ".xlsx";
			String path = dir + "" + name;

			FileOutputStream fos = new FileOutputStream(new File(path));
		workbook.write(fos);
			fos.close();
			workbook.close();
//			return new ResponseEntity<String>(path, HttpStatus.OK);
			return path;
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Action : While Generate skillwise report user with payload {},{}",SecurityContextHolder.getContext().getAuthentication().getName(), e);
				throw new RuntimeException(e);
			}
		}
	
	private Map<String,Map<String,String>> getAgregatedData(String areaColumnPath,Integer areaId, Date startdateDate, Date enddateDate) {
		
	List<Indicator> indicatorList = indicatorRepository.getIndicatorByIndicatorName("Percentage of migrants with type of skill");
	

//	Map<String, String> areaIdIndValMap= new HashMap<>();
//	Map<String, String> areaIdIndValMap2= new HashMap<>();
	List<DataValue> dataValues = new ArrayList<>();
	
	List<Area> areaList = areaRepository.findByParentAreaIdOrderByAreaName(1);
	Map<Integer, String> areaMap = areaList.stream()
			.collect(Collectors.toMap(Area::getAreaId, Area::getAreaName,(area1, area2) -> area1));
	
	Map<String,Map<String,String>> areaIndMap= new TreeMap<>();
	
	String area = areaColumnPath;

	
	Criteria matchCriteria = Criteria.where("isValid").is(true).and("latest").is(true).and("syncDate").gte(startdateDate).lte(enddateDate).and(area).exists(true);
	if(areaId!=null) {
		matchCriteria.andOperator(Criteria.where(area)
				.is(areaId));
	}
	MatchOperation matchOperation = Aggregation.match(matchCriteria);
	ProjectionOperation projectionOperation = Aggregation.project().and("data").as("data");
	UnwindOperation unwindOperation = Aggregation.unwind("data.undp_f1_q29");
	ProjectionOperation projectionOperation1 = Aggregation.project().and(areaColumnPath).as("area");
	//.andExclude("_id");
	GroupOperation groupOperation = Aggregation.group("area");
	List<Map> dataList =null;
//	indicatorList.stream()
//	.forEach(indicator -> {
	for (Indicator indicator : indicatorList) {
		List<Integer> numeratorTds = new ArrayList<>();
//		List<Integer> denominatorTds = new ArrayList<>();

		Arrays.asList(String.valueOf(indicator.getIndicatorDataMap().get("ntypeDetailId")).split("#"))
				.stream().forEach(i -> {
					numeratorTds.add(Integer.parseInt(i));
				});

//		Arrays.asList(String.valueOf(indicator.getIndicatorDataMap().get("dtypeDetailId")).split("#"))
//				.stream().forEach(i -> {
//					denominatorTds.add(Integer.parseInt(i));
//				});

//		unwindOperation =  Aggregation.unwind("data." +  String.valueOf(indicator.getIndicatorDataMap().get("numerator")));
//		if(areaId==null) {
			/*dataList = mongoTemplate.aggregate(getDropdownAggregationResults(areaColumnPath, // areaColumn
				"allChecklistFormData",
				String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
				String.valueOf(indicator.getIndicatorDataMap().get("denominator")), numeratorTds,
				denominatorTds, indicator.getIndicatorDataMap().get("nAggregationRule")!=null ?
						String.valueOf(indicator.getIndicatorDataMap().get("nAggregationRule")): null,
				areaId,startdateDate,enddateDate), AllChecklistFormData.class, Map.class).getMappedResults();*/
			projectionOperation1 =projectionOperation1
					.and(Sum.sumOf(when(where("data." +  String.valueOf(indicator.getIndicatorDataMap().get("numerator")))
							.in(numeratorTds)
							).then(1).otherwise(0)))
					.as("numprojectedData"+indicator.getIndicatorDataMap().get("ntypeDetailId"));

			area = area.replaceAll("data.", "");
			
			groupOperation = groupOperation.sum("numprojectedData"+indicator.getIndicatorDataMap().get("ntypeDetailId"))
					.as(indicator.getIndicatorDataMap().get("ntypeDetailId").toString());
			
			
		/*}else {
			dataList =  mongoTemplate.aggregate(getDropdownAggregationResultsDistrict(areaColumnPath, // areaColumn
					"allChecklistFormData",
					String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
					String.valueOf(indicator.getIndicatorDataMap().get("denominator")), numeratorTds,
					denominatorTds, indicator.getIndicatorDataMap().get("nAggregationRule")!=null ?
							String.valueOf(indicator.getIndicatorDataMap().get("nAggregationRule")): null,
					areaId,startdateDate,enddateDate), AllChecklistFormData.class, Map.class).getMappedResults();
		}*/

//		dataList.forEach(dataMap -> {
//			if(dataMap.get("_id")!=null) {
//				if(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("1")) {
//					areaIdIndValMap.put(String.valueOf(dataMap.get("_id")), dataMap.get("dValue")!=null ?
//							String.valueOf(dataMap.get("dValue")): null);
//				}
//				if(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("9")) {
//					areaIdIndValMap2.put(String.valueOf(dataMap.get("_id")), dataMap.get("nValue")!=null ?
//							String.valueOf(dataMap.get("nValue")): null);
//				}
//				DataValue datadoc=new DataValue();
//				datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
//				datadoc.setNtypeDetailId(String.valueOf(indicator.getIndicatorDataMap().get("ntypeDetailId")));
//				datadoc.setAreaId(Integer.valueOf(String.valueOf(dataMap.get("_id"))));
//				datadoc.setNumerator(String.valueOf(dataMap.get("nValue")));
//				datadoc.setDenominator(areaIdIndValMap.get(String.valueOf(dataMap.get("_id"))));
//				
//				if(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("10")
//						|| String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("11")) {
//					datadoc.setDenominator(areaIdIndValMap2.get(String.valueOf(dataMap.get("_id"))));
//				}
//				
//				Double percent = 	datadoc.getDenominator() != null
//						? (Double.valueOf(String.valueOf(dataMap.get("nValue"))) * 100)
//								/ Double.valueOf(datadoc.getDenominator())
//						: null;
//								
//				datadoc.setDataValue(percent);
//				dataValues.add(datadoc);
//			}
//
//
//		});
	}
//	});
	
	dataList=mongoTemplate.aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, unwindOperation, projectionOperation1,
				groupOperation),
				AllChecklistFormData.class, Map.class).getMappedResults();
	System.out.println("there after aggregation");
	
	
	dataList.forEach(dataMap -> {
		if(dataMap.get("_id")!=null) {
			Map<String,String> indValMa = new HashMap<>();
			dataMap.forEach((k, v) ->{
				if(!k.equals("_id")) {
					indValMa.put(String.valueOf(k) ,String.valueOf(v));
					
//					DataValue datadoc=new DataValue();
//					datadoc.setInid(Integer.valueOf(String.valueOf(k)));
//					datadoc.setNtypeDetailId(String.valueOf(k));
//					datadoc.setAreaId(Integer.valueOf(String.valueOf(dataMap.get("_id"))));
//					datadoc.setNumerator(String.valueOf(v));
//					dataValues.add(datadoc);
				}
				
				areaIndMap.put(areaMap.get(Integer.parseInt(dataMap.get("_id").toString())), indValMa);
			});
		}
		
});
	return areaIndMap;
	}



private Aggregation getDropdownAggregationResultsDistrict(String area, String collection, String numeratorPath,
			String denominatorPath, List<Integer> ntdlist, List<Integer> dtdlist, String conditions, List<Integer> areaId, Date startdateDate, Date enddateDate) {
	List<String> condarr = new ArrayList<>();
	if (conditions != null && !conditions.isEmpty())
		condarr = Arrays.asList(conditions.split(";"));
	Criteria matchCriteria = Criteria.where("isValid").is(true).and("latest").is(true).and(area).in(areaId).and("syncDate").gte(startdateDate).lte(enddateDate);
//	.and(area).is(areaId);
	if (!condarr.isEmpty()) {
		condarr.forEach(_cond -> {
			matchCriteria.andOperator(Criteria.where(_cond.split(":")[0].split("\\(")[1])
					.is(Integer.parseInt(_cond.split(":")[1].split("\\)")[0])));
		});
	}
	MatchOperation matchOperation = Aggregation.match(matchCriteria);
	ProjectionOperation projectionOperation = Aggregation.project().and("data").as("data");
	UnwindOperation unwindOperation =  Aggregation
			.unwind("data." + numeratorPath);

	ProjectionOperation projectionOperation1 = Aggregation.project().and(area).as("area")
			.and(Sum.sumOf(when(where("data." + numeratorPath).in(ntdlist)).then(1).otherwise(0)))
			.as("numprojectedData")
			.and(Sum.sumOf(when(where("data." + denominatorPath).in(dtdlist)).then(1).otherwise(0)))
			.as("denoprojectedData");

	area = area.replaceAll("data.", "");

	GroupOperation groupOperation = Aggregation.group("area").sum("numprojectedData").as("nValue")
			.sum("denoprojectedData").as("dValue");

	return Aggregation.newAggregation(matchOperation, projectionOperation, unwindOperation, projectionOperation1, groupOperation);
}



public Aggregation getDropdownAggregationResults(String area, String collection, String numeratorPath,
			String denominatorPath, List<Integer> ntdlist, List<Integer> dtdlist, String conditions, List<Integer> areaId, Date startdateDate, Date enddateDate) {
		List<String> condarr = new ArrayList<>();
		if (conditions != null && !conditions.isEmpty())
			condarr = Arrays.asList(conditions.split(";"));
		Criteria matchCriteria = Criteria.where("isValid").is(true).and("latest").is(true).and("syncDate").gte(startdateDate).lte(enddateDate).and(area).exists(true);
//		.and(area).is(areaId);
		if (!condarr.isEmpty()) {
			condarr.forEach(_cond -> {
				matchCriteria.andOperator(Criteria.where(_cond.split(":")[0].split("\\(")[1])
						.is(Integer.parseInt(_cond.split(":")[1].split("\\)")[0])));
			});
		}
		MatchOperation matchOperation = Aggregation.match(matchCriteria);
		ProjectionOperation projectionOperation = Aggregation.project().and("data").as("data");
		UnwindOperation unwindOperation =  Aggregation
				.unwind("data." + numeratorPath);

		ProjectionOperation projectionOperation1 = Aggregation.project().and(area).as("area")
				.and(Sum.sumOf(when(where("data." + numeratorPath).in(ntdlist)).then(1).otherwise(0)))
				.as("numprojectedData")
				.and(Sum.sumOf(when(where("data." + denominatorPath).in(dtdlist)).then(1).otherwise(0)))
				.as("denoprojectedData");

		area = area.replaceAll("data.", "");

		GroupOperation groupOperation = Aggregation.group("area").sum("numprojectedData").as("nValue")
				.sum("denoprojectedData").as("dValue");

		return Aggregation.newAggregation(matchOperation, projectionOperation, unwindOperation, projectionOperation1, groupOperation);
	}

}
