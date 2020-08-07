package org.sdrc.pmr.service;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.pmr.collection.AllChecklistFormData;
import org.sdrc.pmr.collection.DataValue;
import org.sdrc.pmr.collection.GroupIndicator;
import org.sdrc.pmr.collection.Indicator;
import org.sdrc.pmr.model.ChartDataModel;
import org.sdrc.pmr.model.GroupChartDataModel;
import org.sdrc.pmr.model.IndicatorGroupModel;
import org.sdrc.pmr.model.LegendModel;
import org.sdrc.pmr.model.SectorModel;
import org.sdrc.pmr.model.SubSectorModel;
import org.sdrc.pmr.model.ValueObject;
import org.sdrc.pmr.repository.AreaRepository;
import org.sdrc.pmr.repository.DataValueRepository;
import org.sdrc.pmr.repository.GroupIndicatorRepository;
import org.sdrc.pmr.repository.IndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators.Sum;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service
public class DashboradServiceImpl implements DashboradService {

	@Autowired
	private IndicatorRepository indicatorRepository;

	@Autowired
	private DataValueRepository dataValueRepository;

	@Autowired
	private GroupIndicatorRepository groupIndicatorRepository;

	@Autowired
	private AreaRepository areaRepository;
	
//	@Autowired
//	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
	private DateFormat ymdDateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	NumberFormat formatter = new DecimalFormat("#0.0");
	
	@Override
	public void importIndicators() throws InvalidFormatException, IOException {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("aggregation/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}
		
//		XSSFWorkbook workbook = new XSSFWorkbook(
//				new File(configurableEnvironment.getProperty("aggregation.template.uri")));
		
		XSSFWorkbook indicatorWorkbook = new XSSFWorkbook(files[0]);
		
		indicatorRepository.deleteAll();

//		XSSFWorkbook indicatorWorkbook = new XSSFWorkbook(
//				new File(configurableEnvironment.getProperty("aggregation.template.uri")));
		XSSFSheet indicatorSheet = indicatorWorkbook.getSheet("Sheet2");
		Map<String, Object> indicatorMap = new HashMap<String, Object>();
		XSSFRow header = indicatorSheet.getRow(0);
//		for (int rowNum = 1; rowNum <= indicatorSheet.getLastRowNum(); rowNum++) {
		for (int rowNum = 1; rowNum <= 99; rowNum++) {
			XSSFRow row = indicatorSheet.getRow(rowNum);
			for (int cellnum = 0; cellnum < row.getLastCellNum(); cellnum++) {
				indicatorMap.put(header.getCell(cellnum).getStringCellValue(),
						getCellValueAsString(row.getCell(cellnum)));
			}
			Indicator i = new Indicator();
			i.setIndicatorDataMap(indicatorMap);
			indicatorRepository.save(i);
		}
		indicatorWorkbook.close();
	}

//	@SuppressWarnings("deprecation")
	public static String getCellValueAsString(Cell cell) {
		String strCellValue = null;
		if (cell != null) {
			switch (cell.getCellTypeEnum()) {
			case STRING:
				strCellValue = cell.toString();
				break;
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					strCellValue = dateFormat.format(cell.getDateCellValue());
				} else {
					Double value = cell.getNumericCellValue();
					Long longValue = value.longValue();
					strCellValue = new String(longValue.toString());
				}
				break;
			case BOOLEAN:
				strCellValue = new String(new Boolean(cell.getBooleanCellValue()).toString());
				break;
			case BLANK:
				strCellValue = "";
				break;
			default:
				strCellValue = "";
				break;
			}
		}
		return strCellValue;
	}
	
	@Override
	@CacheEvict(value = "dashboarddata", allEntries=true)
	public String aggregate(Integer areaLevelId, Integer areaId) {

		System.out.println("Start time --->>>" + dateTimeFormat.format(new Date()));

		/*
		 * String areaColumn = "";
		 * 
		 * switch (areaLevelId) { case 1: // state areaColumn = "data.undp_f1_q17";
		 * break;
		 * 
		 * case 2: // district areaColumn = "data.undp_f1_q18"; break;
		 * 
		 * case 3:// TAHASIL areaColumn = "data.undp_f1_tahasil"; break;
		 * 
		 * case 4:// BLOCK areaColumn = "data.undp_f1_q19"; break;
		 * 
		 * case 5:// ULB areaColumn = "data.undp_f1_q20_w1"; break;
		 * 
		 * case 6:// VILLAGE areaColumn = "data.undp_f1_q20_w4"; break;
		 * 
		 * default: break; }
		 */
		Date createdDate = new Date();
		List<Indicator> indicatorList = indicatorRepository.findAll();

//		List<String> areaLevels = Arrays.asList( "data.undp_f1_q17",  "data.undp_f1_q18", "data.undp_f1_tahasil","data.undp_f1_q19",  "data.undp_f1_q20_w1",  "data.undp_f1_q20_w4" );
		List<String> areaLevels = Arrays.asList("data.undp_f1_q17", "data.undp_f1_q18");
		List<DataValue> dataValues = new ArrayList<>();
		areaLevels.forEach(areaColumn -> {

			final String areaColumnPath = areaColumn;

//			Map<String, Map<String, Object>> valMap = new HashMap<>();

//			Map<String, Map<String, Map<String, Object>>> areaIndValMap = new HashMap<>();

			Criteria matchCriteria = Criteria.where("isValid").is(true).and("latest").is(true).and(areaColumnPath)
					.exists(true);
//				.and(areaColumnPath).is(areaId);

			MatchOperation matchOperation = Aggregation.match(matchCriteria);

			ProjectionOperation projectionOperation = Aggregation.project().and("data").as("data");

			// Distribution of migrants by originating state //undp_f1_q9

			ProjectionOperation stateProjectionOperation = Aggregation.project().and(areaColumnPath).as("area")
					.and("data.undp_f1_q9").as("origin")
					.and(Sum.sumOf(when(where(areaColumnPath).and("data.undp_f1_q9").ne(null)).then(1).otherwise(0)))
					.as("numo");
			;

			GroupOperation groupStateOperation = Aggregation.group("area", "origin").sum("numo").as("nValue");
			List<Map> stateListMap = mongoTemplate
					.aggregate(Aggregation.newAggregation(matchOperation, projectionOperation, stateProjectionOperation,
							groupStateOperation), AllChecklistFormData.class, Map.class)
					.getMappedResults();

			System.out.println("stateListMap");

			
			// end

			String area = areaColumnPath;
//			ProjectionOperation projectionOperation1 = Aggregation.project().andInclude(area); // .as("area");
			// .andExclude("_id");

			area = area.replaceAll("data.", "");

		
			System.out.println("hi");

			Map<String, String> areaIdIndValMap= new HashMap<>();
			Map<String, String> areaIdIndValMap2= new HashMap<>();
			indicatorList.stream()
//					.filter(ind -> ind.getIndicatorDataMap().get("parentType").equals("dependentDropdown"))
					.forEach(indicator -> {

//						Map<String, Object> resultMap = new HashMap<>();

						List<Integer> numeratorTds = new ArrayList<>();
						List<Integer> denominatorTds = new ArrayList<>();

						Arrays.asList(String.valueOf(indicator.getIndicatorDataMap().get("ntypeDetailId")).split("#"))
								.stream().forEach(i -> {
									numeratorTds.add(Integer.parseInt(i));
								});

						Arrays.asList(String.valueOf(indicator.getIndicatorDataMap().get("dtypeDetailId")).split("#"))
								.stream().forEach(i -> {
									denominatorTds.add(Integer.parseInt(i));
								});

						List<Map> dataList = mongoTemplate.aggregate(getDropdownAggregationResults(areaColumnPath, // areaColumn
								"allChecklistFormData",
								String.valueOf(indicator.getIndicatorDataMap().get("numerator")),
								String.valueOf(indicator.getIndicatorDataMap().get("denominator")), numeratorTds,
								denominatorTds, indicator.getIndicatorDataMap().get("nAggregationRule")!=null ?
										String.valueOf(indicator.getIndicatorDataMap().get("nAggregationRule")): null,
								areaId), AllChecklistFormData.class, Map.class).getMappedResults();

						dataList.forEach(dataMap -> {
							if(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("5")) {
								System.out.println("there");
							}
							if(dataMap.get("_id")!=null) {
								if(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("1")) {
									areaIdIndValMap.put(String.valueOf(dataMap.get("_id")), dataMap.get("dValue")!=null ?
											String.valueOf(dataMap.get("dValue")): null);
								}
								if(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("9")) {
									areaIdIndValMap2.put(String.valueOf(dataMap.get("_id")), dataMap.get("nValue")!=null ?
											String.valueOf(dataMap.get("nValue")): null);
								}
								DataValue datadoc=new DataValue();
								datadoc.setInid(Integer.valueOf(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid"))));
								datadoc.setAreaId(Integer.valueOf(String.valueOf(dataMap.get("_id"))));
								
								datadoc.setNumerator(String.valueOf(dataMap.get("nValue")));
								datadoc.setDenominator(String.valueOf(dataMap.get("dValue")));
//								datadoc.setDenominator(areaIdIndValMap.get(String.valueOf(dataMap.get("_id"))));
								datadoc.setCreatedDate(createdDate);
								if(String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("10")
										|| String.valueOf(indicator.getIndicatorDataMap().get("indicatorNid")).equals("11")) {
									datadoc.setDenominator(areaIdIndValMap2.get(String.valueOf(dataMap.get("_id"))));
								}
								
								Double percent = 	datadoc.getDenominator() != null
										? (Double.valueOf(String.valueOf(dataMap.get("nValue"))) * 100)
												/ Double.valueOf(datadoc.getDenominator())
										: null;
												
								datadoc.setDataValue(percent);
								dataValues.add(datadoc);
							}
							
						});
					});
			stateListMap.stream().forEach(stateEach -> {
				if(stateEach.get("area")!=null) {
					DataValue datadoc=new DataValue();
					datadoc.setInid(82);
					datadoc.setAreaId(Integer.valueOf(String.valueOf(stateEach.get("area"))));
					datadoc.setNumerator(String.valueOf(stateEach.get("nValue")));
					datadoc.setOriginStateId(Integer.valueOf(String.valueOf(stateEach.get("origin"))));
					
					datadoc.setDenominator(areaIdIndValMap.get(String.valueOf(stateEach.get("area"))));
					
					Double percent = 	datadoc.getDenominator()!= null
							? (Double.valueOf(String.valueOf(datadoc.getNumerator())) * 100)
									/ Double.valueOf(String.valueOf(datadoc.getDenominator()))
							: null;
									
					
					datadoc.setDataValue(percent);
					dataValues.add(datadoc);
				}
				
			});


		});
		dataValueRepository.deleteAll();
		dataValueRepository.save(dataValues);
		System.out.println("End time --->>>" + dateTimeFormat.format(new Date()));
		return "done";
	}

	public Aggregation getDropdownAggregationResults(String area, String collection, String numeratorPath,
			String denominatorPath, List<Integer> ntdlist, List<Integer> dtdlist, String conditions, Integer areaId) {
		List<String> condarr = new ArrayList<>();
		if (conditions != null && !conditions.isEmpty())
			condarr = Arrays.asList(conditions.split(";"));
		Criteria matchCriteria = Criteria.where("isValid").is(true).and("latest").is(true).and(area).exists(true);
//				.and(area).is(areaId);
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
//		ProjectionOperation projectionOperation1=Aggregation.project()
//				.and(area).as("area")
//				.and(Sum.sumOf(when(where("data."+path).in(tdlist)).then(1).otherwise(0))).as("projectedData");

		ProjectionOperation projectionOperation1 = Aggregation.project().and(area).as("area")
				.and(Sum.sumOf(when(where("data." + numeratorPath).in(ntdlist)).then(1).otherwise(0)))
				.as("numprojectedData")
				.and(Sum.sumOf(when(where("data." + denominatorPath).in(dtdlist)).then(1).otherwise(0)))
				.as("denoprojectedData");

//		GroupOperation groupOperation= Aggregation.group("area").sum("projectedData").as("value");

		area = area.replaceAll("data.", "");

		GroupOperation groupOperation = Aggregation.group("area").sum("numprojectedData").as("nValue")
				.sum("denoprojectedData").as("dValue");

		return Aggregation.newAggregation(matchOperation, projectionOperation, unwindOperation, projectionOperation1, groupOperation);
	}

	@Override
	public String pushIndicatorGroupData() throws InvalidFormatException, IOException {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("aggregation/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}
		
		groupIndicatorRepository.deleteAll();

//		XSSFWorkbook workbook = new XSSFWorkbook(
//				new File(configurableEnvironment.getProperty("aggregation.template.uri")));
		
		XSSFWorkbook workbook = new XSSFWorkbook(files[0]);
		XSSFSheet sheet = workbook.getSheet("Sheet3");

		List<GroupIndicator> indicatorModels = new ArrayList<>();

		GroupIndicator groupIndicatorModel = null;

		XSSFRow row = null;
		XSSFCell cell = null;
		for (int rowNum = 1; rowNum <= 14; rowNum++) {
			groupIndicatorModel = new GroupIndicator();

			int colNum = 0;

			row = sheet.getRow(rowNum);
			cell = row.getCell(colNum);
			groupIndicatorModel.setIndicatorGroup(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);
			if (cell != null && cell.getCellTypeEnum() == CellType.NUMERIC) {
				groupIndicatorModel.setKpiIndicator((int) cell.getNumericCellValue());
			}
			colNum++;
			cell = row.getCell(colNum);
			groupIndicatorModel.setChartType(cell != null && cell.getCellTypeEnum() != CellType.BLANK
					? new ArrayList<>(Arrays.asList(cell.getStringCellValue().split(",")))
					: Arrays.asList(""));

			colNum++;
			cell = row.getCell(colNum);
			if (cell != null)
				groupIndicatorModel.setChartIndicators(getBarChartIndicators(cell.getStringCellValue()));

			colNum++;
			cell = row.getCell(colNum);
			groupIndicatorModel.setSector(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);

			if (cell != null && cell.getCellTypeEnum() == CellType.NUMERIC) {
				groupIndicatorModel.setSectorId(String.valueOf((int) cell.getNumericCellValue()));
			} else if (cell != null && cell.getCellTypeEnum() == CellType.STRING) {
				groupIndicatorModel.setSectorId(cell.getStringCellValue());
			}

			colNum++;
			cell = row.getCell(colNum);
			groupIndicatorModel.setSubSector(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);
			if (cell != null)
				groupIndicatorModel.setKpiChartHeader(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);
			if (cell != null)
				groupIndicatorModel.setChartHeader(cell.getStringCellValue());

//			cardType	chartLegends	colorLegends	
			colNum++;
			cell = row.getCell(colNum);
			if (cell != null)
				groupIndicatorModel.setCardType(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);
			if (cell != null)
				groupIndicatorModel.setChartLegends(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);
			if (cell != null)
				groupIndicatorModel.setColorLegends(cell.getStringCellValue());

//			align	valueFrom	unit	chartGroup
			colNum++;
			cell = row.getCell(colNum);
			groupIndicatorModel.setAlign(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);

			if (cell != null && cell.getCellTypeEnum() == CellType.NUMERIC) {
				groupIndicatorModel.setValueFrom(String.valueOf((int) cell.getNumericCellValue()));
			} else if (cell != null && cell.getCellTypeEnum() == CellType.STRING) {
				groupIndicatorModel.setValueFrom(cell.getStringCellValue());
			}

			colNum++;
			cell = row.getCell(colNum);
			groupIndicatorModel.setUnit(cell.getStringCellValue());

			colNum++;
			cell = row.getCell(colNum);
			groupIndicatorModel.setChartGroup(cell.getStringCellValue());

			indicatorModels.add(groupIndicatorModel);
		}
		workbook.close();

		groupIndicatorRepository.save(indicatorModels);
		return "indicator saved";
	}

	private List<List<Integer>> getBarChartIndicators(String stringCellValue) {
		List<List<Integer>> barChartIndicators = new ArrayList<>();
		if (!stringCellValue.equals("")) {
			for (int i = 0; i < stringCellValue.split("@").length; i++) {
				barChartIndicators.add(getValue(stringCellValue.split("@")[i]));
			}
		}
		return barChartIndicators;
	}

	private List<Integer> getValue(String stringCellValue) {
		List<Integer> indicators = new ArrayList<>();
		if (!stringCellValue.equals("")) {
			for (int i = 0; i < stringCellValue.split(",").length; i++) {
				if (!stringCellValue.split(",")[i].equals("")) {
					indicators.add(Integer.valueOf(stringCellValue.split(",")[i].trim()));
				}
			}
		}
		return indicators;
	}

//	@Override
//	public List<SectorModel> getDashboardDataFromCache(Integer areaLevel, Integer areaId,OAuth2Authentication oauth ){
//		UserModel principal = oAuth2Utility.getUserModel();
//		
//		areaId = principal.getAreaIds().size() > 1 ?principal.getAreaIds().get(1) : principal.getAreaIds().get(0); 
//		
//		return getDashboardData(areaId);
//	}
	
	@Override
	@Cacheable(value="dashboarddata", key="#areaId")  
	public List<SectorModel> getDashboardData(Integer areaLevel, Integer areaId,OAuth2Authentication oauth ) {

		System.out.println("from method");
//		UserModel principal = oAuth2Utility.getUserModel();
//		
//		areaId = principal.getAreaIds().size() > 1 ?principal.getAreaIds().get(1) : principal.getAreaIds().get(0); 
		
		List<SectorModel> sectorModels = new LinkedList<>();

		Map<String, Map<String, List<IndicatorGroupModel>>> map = new LinkedHashMap<>();

		List<GroupIndicator> groupIndicatorModels = groupIndicatorRepository.findAll();
		
		List<Indicator> indicatorList = indicatorRepository.findAll();
		String lastAggregatedTime = "";
		
		List<Integer> indicatorIds = new LinkedList<>() ;
		Map<Integer, String> indicatorNameMap = new HashMap<>();
		
		for (Indicator indicator : indicatorList) {
			indicatorIds.add(Integer.valueOf((String)indicator.getIndicatorDataMap().get("indicatorNid")));
			indicatorNameMap.put(Integer.valueOf((String)indicator.getIndicatorDataMap().get("indicatorNid")),
					(String) indicator.getIndicatorDataMap().get("indicatorName"));
		}
		
		List<DataValue> dataValues = dataValueRepository.findByAreaId(areaId);
		Map<Integer, List<DataValue>> allMapData = new LinkedHashMap <>();
		
		if (!dataValues.isEmpty()) {
			lastAggregatedTime = ymdDateTimeFormat.format(dataValues.get(0).getCreatedDate());
		}
		
		for (DataValue dataValue : dataValues) {
			
			if(!allMapData.containsKey(dataValue.getInid())) {
				List<DataValue> dvs = new ArrayList<>();
				dvs.add(dataValue);
				allMapData.put(dataValue.getInid(), dvs);
			}else {
				allMapData.get(dataValue.getInid()).add(dataValue);
			}
		}
		Map<Integer, ChartDataModel> areaIdValueMap =getAllAreaChart();
				/*new HashMap<>();
		areaRepository.findByParentAreaIdOrderByAreaName(-1).stream().forEach(state -> {
			ChartDataModel chartDataModel = new ChartDataModel();
			chartDataModel.setAxis(state.getAreaName());	
			chartDataModel.setValue("0");
			chartDataModel.setNumerator("0");
			chartDataModel.setDenominator("0");
			chartDataModel.setTooltipValue("0");
			chartDataModel.setDblVal(Double.parseDouble(chartDataModel.getValue()));
			areaIdValueMap.put(state.getAreaId(), chartDataModel);
		});
		*/
		if(allMapData.containsKey(82)) {
			allMapData.get(82).stream().forEach(stateMap -> {
				ChartDataModel chartDataModel = areaIdValueMap.get(stateMap.getOriginStateId());
//				chartDataModel.setValue(stateMap.getDataValue());
				
				chartDataModel.setValue(stateMap.getDataValue()== null ? "0" :
										 formatter.format(stateMap.getDataValue()));
				
				chartDataModel.setTooltipValue(chartDataModel.getValue());
				
				chartDataModel.setNumerator(
						( stateMap.getNumerator() == null)
								? null
								: String.valueOf(stateMap.getNumerator()));
				chartDataModel.setDenominator(
						(stateMap.getDenominator() == null)
								? null
								: String.valueOf(stateMap.getDenominator()));
				chartDataModel.setId(stateMap.getOriginStateId());
				chartDataModel.setUnit("Percent");
				chartDataModel.setDblVal(Double.parseDouble(chartDataModel.getValue()));
			});
				
		}
	
		List<GroupChartDataModel> stateGroupChartData = new ArrayList<>();
		List<ChartDataModel> chartDataModels = new LinkedList<>();
		List<List<ChartDataModel>> listChartDataModels = new LinkedList<>();
		GroupChartDataModel grchartDataModel =  new GroupChartDataModel();
		grchartDataModel.setHeaderIndicatorName("Percentage of Migrants by Originating State");
		IndicatorGroupModel stateIndicatorGroupModel = new IndicatorGroupModel();
		
		 for (Map.Entry<Integer, ChartDataModel> entry : areaIdValueMap.entrySet()) {
				chartDataModels.add(entry.getValue());
		    }
		 
			
			chartDataModels.sort(Comparator.comparingDouble(ChartDataModel::getDblVal)
	                .reversed());

			
			listChartDataModels.add(chartDataModels);
			grchartDataModel.setChartDataValue(listChartDataModels);
			stateGroupChartData.add(grchartDataModel);
			
			stateIndicatorGroupModel.setChartsAvailable(Arrays.asList("bar"));
			stateIndicatorGroupModel.setAlign("col-md-12");
			stateIndicatorGroupModel.setIndicatorGroupName("Gr_82");
			stateIndicatorGroupModel.setUnit("Percent");
			stateIndicatorGroupModel.setChartAlign("col-md-12");
			stateIndicatorGroupModel.setChartGroup("Gr_82");
			stateIndicatorGroupModel.setChartData(stateGroupChartData);
			
			Map<String, List<IndicatorGroupModel>> subsectorGrMapModel = new LinkedHashMap<>();

			List<IndicatorGroupModel> sectorNewIndicators = new LinkedList<>();
			sectorNewIndicators.add(stateIndicatorGroupModel);

			subsectorGrMapModel.put("Dashboard", sectorNewIndicators);
			
			map.put("Dashboard", subsectorGrMapModel);
		/*MatchOperation match = Aggregation.match(Criteria.where("areaId").is(areaId).and("inid").is(82));
		
		LookupOperation lookup = Aggregation.lookup("area", "originStateId", "areaId", "area_docs");
		
		Aggregation resultQuery=Aggregation.newAggregation(match, lookup);
		List<Map> stateListMap = mongoTemplate.aggregate(resultQuery, DataValue.class, Map.class).getMappedResults();
		
		List<GroupChartDataModel> stateGroupChartData = new ArrayList<>();
		List<ChartDataModel> chartDataModels = new LinkedList<>();
		List<List<ChartDataModel>> listChartDataModels = new LinkedList<>();
		GroupChartDataModel grchartDataModel =  new GroupChartDataModel();
		grchartDataModel.setHeaderIndicatorName("Percentage of Migrants by Originating State");
		IndicatorGroupModel stateIndicatorGroupModel = new IndicatorGroupModel();
		
		Map<String, ChartDataModel> areaIdValueMap = new HashMap<>();
		
	
		for(Map dataDoc: stateListMap) {
			
			ArrayList<Map> areaDocs = (ArrayList) dataDoc.get("area_docs");
			ChartDataModel chartDataModel = new ChartDataModel();// areaIdValueMap.get(dataDoc.get("originStateId").toString());
			chartDataModel.setAxis(areaDocs.get(0).get("areaName").toString());
			chartDataModel.setValue(dataDoc.get("dataValue")!=null ? 
					formatter.format(Double.parseDouble(dataDoc.get("dataValue").toString())) : null);
			
			chartDataModel.setTooltipValue(chartDataModel.getValue());
			
			chartDataModel.setNumerator(dataDoc.get("numerator")!=null ? dataDoc.get("numerator").toString() : null);
			chartDataModel.setDenominator(dataDoc.get("denominator")!=null ? dataDoc.get("denominator").toString() : null);
			
			chartDataModel.setLegend(chartDataModel.getAxis());
			chartDataModel.setId(Integer.parseInt(dataDoc.get("originStateId").toString()));
			chartDataModel.setUnit("Percent");
			chartDataModel.setDblVal(Double.parseDouble(chartDataModel.getValue()));
			chartDataModels.add(chartDataModel);
			areaIdValueMap.put(dataDoc.get("originStateId").toString(), chartDataModel);
		}
	areaRepository.findByParentAreaIdOrderByAreaName(-1).stream().forEach(state -> {
			
		if(!areaIdValueMap.containsKey(state.getAreaId().toString())) {
			ChartDataModel chartDataModel = new ChartDataModel();
			chartDataModel.setAxis(state.getAreaName());	
			chartDataModel.setValue("0");
			chartDataModel.setNumerator("0");
			chartDataModel.setDenominator("0");
			chartDataModel.setTooltipValue("0");
			chartDataModel.setDblVal(Double.parseDouble(chartDataModel.getValue()));
			chartDataModels.add(chartDataModel);
		}
		});
		
//		Comparator<ChartDataModel> compareById = (ChartDataModel o1, ChartDataModel o2) -> 
//		Double.parseDouble(o1.getValue()).comparingDouble(Double.parseDouble(o2.getValue()));
//		 
//		Collections.sort(chartDataModels, compareById);
		
		chartDataModels.sort(Comparator.comparingDouble(ChartDataModel::getDblVal)
                .reversed());

		
		listChartDataModels.add(chartDataModels);
		grchartDataModel.setChartDataValue(listChartDataModels);
		stateGroupChartData.add(grchartDataModel);
		
		stateIndicatorGroupModel.setChartsAvailable(Arrays.asList("bar"));
		stateIndicatorGroupModel.setAlign("col-md-12");
		stateIndicatorGroupModel.setIndicatorGroupName("Gr_82");
		stateIndicatorGroupModel.setUnit("Percent");
		stateIndicatorGroupModel.setChartAlign("col-md-12");
		stateIndicatorGroupModel.setChartGroup("Gr_82");
		stateIndicatorGroupModel.setChartData(stateGroupChartData);
		
		Map<String, List<IndicatorGroupModel>> subsectorGrMapModel = new LinkedHashMap<>();

		List<IndicatorGroupModel> sectorNewIndicators = new LinkedList<>();
		sectorNewIndicators.add(stateIndicatorGroupModel);

		subsectorGrMapModel.put("Dashboard", sectorNewIndicators);
		
		map.put("Dashboard", subsectorGrMapModel);*/
		
		//end of Distribution of migrants by originating state bar chart 
		
		for (GroupIndicator groupIndicatorModel : groupIndicatorModels) {

			IndicatorGroupModel indicatorGroupModel = new IndicatorGroupModel();

			List<GroupChartDataModel> listOfGroupChartData = null;
			GroupChartDataModel chartDataModel = null;
			List<LegendModel> legendModels = null;
			
			if (allMapData != null) {
				indicatorGroupModel
				.setIndicatorValue(allMapData.get(groupIndicatorModel.getKpiIndicator()) == null ? null
						: allMapData.get(groupIndicatorModel.getKpiIndicator()).get(0).getDataValue() == null ? null :
						groupIndicatorModel.getUnit().equalsIgnoreCase("percentage") || groupIndicatorModel.getUnit().equalsIgnoreCase("Average")? 
								formatter.format(allMapData.get(groupIndicatorModel.getKpiIndicator()).get(0).getDataValue()) : 
							String.valueOf(allMapData.get(groupIndicatorModel.getKpiIndicator()).get(0).getDataValue().intValue()));
				indicatorGroupModel.setTooltipValue(indicatorGroupModel.getIndicatorValue());
				
				indicatorGroupModel.setNumerator(
						(allMapData.get(groupIndicatorModel.getKpiIndicator()) == null || allMapData.get(groupIndicatorModel.getKpiIndicator())
								.get(0).getNumerator() == null)
								? "0"
								: String.valueOf(Math.round(Double.parseDouble ( allMapData.get(groupIndicatorModel.getKpiIndicator())
										.get(0).getNumerator()))));
				indicatorGroupModel.setDenominator(
						(allMapData.get(groupIndicatorModel.getKpiIndicator()) == null || allMapData.get(groupIndicatorModel.getKpiIndicator())
								.get(0).getDenominator() == null)
								? "0"
								: String.valueOf(Math.round(Double.parseDouble ( allMapData.get(groupIndicatorModel.getKpiIndicator())
										.get(0).getDenominator()))));
			}
			
			indicatorGroupModel.setIndicatorName(groupIndicatorModel.getKpiChartHeader());
			
			indicatorGroupModel.setIndicatorId(groupIndicatorModel.getKpiIndicator());
			indicatorGroupModel.setChartsAvailable(groupIndicatorModel.getChartType());
			indicatorGroupModel.setAlign(groupIndicatorModel.getAlign());
			indicatorGroupModel.setCardType(groupIndicatorModel.getCardType());
			indicatorGroupModel.setIndicatorGroupName(groupIndicatorModel.getIndicatorGroup());
			indicatorGroupModel.setUnit(groupIndicatorModel.getUnit());
			indicatorGroupModel.setChartAlign(groupIndicatorModel.getChartAlign());
			indicatorGroupModel.setChartGroup(groupIndicatorModel.getChartGroup());
			
			if(groupIndicatorModel.getChartIndicators()!=null &&
					groupIndicatorModel.getChartIndicators().size() > 0 && allMapData!=null){ //for all other chart types
				listOfGroupChartData = new ArrayList<GroupChartDataModel>();
				chartDataModel = new GroupChartDataModel();

				String indName = groupIndicatorModel.getChartHeader();

				chartDataModel.setHeaderIndicatorName(indName);
				
				chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel, allMapData, indicatorNameMap, groupIndicatorModel.getChartType().get(0)
						, null, groupIndicatorModel.getUnit(), "snapshot"));
				
				listOfGroupChartData.add(chartDataModel);
				indicatorGroupModel.setChartData(listOfGroupChartData);
				

				if (groupIndicatorModel.getColorLegends()!=null &&
						groupIndicatorModel.getColorLegends().length() > 0 && allMapData!=null) {
					legendModels = new ArrayList<>();
					String[] legendsList = groupIndicatorModel.getColorLegends().split(",");
					
					for (String string : legendsList) {
						LegendModel legendModel = new LegendModel();
						legendModel.setCssClass(string.split("_")[0]);
						legendModel.setValue(string.split("_")[1]);
						legendModels.add(legendModel);
					}
					chartDataModel.setLegends(legendModels);
				}
			}
				if (!map.containsKey(groupIndicatorModel.getSector())) {

//					Map<String, List<IndicatorGroupModel>> subsectorGrMapModel = new LinkedHashMap<>();

//					List<IndicatorGroupModel> sectorNewIndicators = new LinkedList<>();
					sectorNewIndicators.add(indicatorGroupModel);

					subsectorGrMapModel.put(groupIndicatorModel.getSubSector(), sectorNewIndicators);
					
					map.put(groupIndicatorModel.getSector(), subsectorGrMapModel);
				}
				else {
					
					if(!map.get(groupIndicatorModel.getSector()).containsKey(groupIndicatorModel.getSubSector())) {
						
						List<IndicatorGroupModel> newIndicators = new LinkedList<>();
						newIndicators.add(indicatorGroupModel);

						map.get(groupIndicatorModel.getSector()).put(groupIndicatorModel.getSubSector(), newIndicators);
					}else {
						map.get(groupIndicatorModel.getSector()).get(groupIndicatorModel.getSubSector()).add(indicatorGroupModel);
					}
				}
			
			
			
		}
		
		for (Entry<String, Map<String, List<IndicatorGroupModel>>> entry : map.entrySet()) {
			SectorModel sectorModel = new SectorModel();
			sectorModel.setSectorName(entry.getKey());
			
			List<SubSectorModel> listOfSubsector = new ArrayList<>();
		
			for (Entry<String, List<IndicatorGroupModel>> entry2 : entry.getValue().entrySet()) {
				SubSectorModel subSectorModel = new SubSectorModel();
				subSectorModel.setSubSectorName(entry2.getKey());
				listOfSubsector.add(subSectorModel);
				subSectorModel.setIndicators(entry2.getValue());
			}
			sectorModel.setSubSectors(listOfSubsector);
			sectorModel.setTimePeriod(lastAggregatedTime);
			sectorModels.add(sectorModel);
		}
		return sectorModels;
	}
	
	private List<List<ChartDataModel>> getChartDataValue(GroupIndicator groupIndicatorModel,
			Map<Integer, List<DataValue>> mapData, Map<Integer, String> indicatorNameMap, String chartType,
			Map<Integer, String> cssGroupMap, String unit, String dashboardType) {
		
		List<List<Integer>> chartIndicators = null;
		
		String[] axisList = null ;
		chartIndicators = new ArrayList<>();

		chartIndicators = groupIndicatorModel.getChartIndicators();
		
		if(groupIndicatorModel.getChartLegends()!=null)
			axisList = groupIndicatorModel.getChartLegends().split("@");
		
		if(chartType.equals("pie") || chartType.equals("donut"))
			axisList = groupIndicatorModel.getColorLegends().split(",");
				
		List<List<ChartDataModel>> listChartDataModels = new LinkedList<>();
		List<ChartDataModel> chartDataModels = null;
		
		
			int grCount = 0;
			
			for (List<Integer> indList : chartIndicators) {
					chartDataModels = new LinkedList<>();
					
					for (int i=0; i< indList.size(); i++) {
						
						String axis = null;
						axis = axisList!= null? axisList[i].split("_")[1] : null;
						
						String label = null;
						
						if(chartType.equals("stack") || chartType.equals("table"))
							label = groupIndicatorModel.getColorLegends()!= null? groupIndicatorModel.getColorLegends().split(",")[grCount].split("_")[1] : null;
						
					
						getChartDataModel(indicatorNameMap, indList.get(i), mapData,"all", chartDataModels, 
								cssGroupMap, axis, unit, dashboardType, label, chartType, groupIndicatorModel.getChartHeader());
					}
					if(chartType.equals("bar"))
						chartDataModels.sort(Comparator.comparingDouble(ChartDataModel::getDblVal).reversed());
					listChartDataModels.add(chartDataModels);
						
						grCount++;
				}
			
		
		return listChartDataModels;
	}

	
	private List<ChartDataModel> getChartDataModel(Map<Integer, String> indicatorNameMap, Integer indiId,
			Map<Integer, List<DataValue>> mapData, String numeDeno, List<ChartDataModel> chartDataModels,
			Map<Integer, String> cssGroupMap, String axis,
			String unit, String dashboardType, String label, String chartType, String chartName) {
		
		ChartDataModel chartDataModel = new ChartDataModel();
		chartDataModel.setAxis(axis);
		chartDataModel.setLabel(label);
		
		chartDataModel.setValue(
				(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).get(0).getDataValue() == null) ? null
						: unit.equalsIgnoreCase("percentage")
								? formatter.format((mapData.get(indiId).get(0).getDataValue()))
								: String.valueOf(mapData.get(indiId).get(0).getDataValue().intValue()));
		
		chartDataModel.setTooltipValue(chartDataModel.getValue());
		
		chartDataModel.setNumerator(
				(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).get(0).getNumerator() == null)
						? null
						: String.valueOf(Math.round(Double.parseDouble ( mapData.get(indiId).get(0).getNumerator()))));
		chartDataModel.setDenominator(
				(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).get(0).getDenominator() == null)
						? null
						: String.valueOf(Math.round(Double.parseDouble ( mapData.get(indiId).get(0).getDenominator()))));
		
		chartDataModel.setLegend(axis);
		chartDataModel.setId(indiId);
		chartDataModel.setUnit(unit);
		chartDataModel.setDblVal(chartDataModel.getValue()!=null ? Double.parseDouble(chartDataModel.getValue()) : 0.0);
		chartDataModels.add(chartDataModel);
		
		return chartDataModels;
	}
	
	@Override
	public Map<String, ValueObject> getThematicMapData(Integer inid){
//		List<Integer> districtIds = new ArrayList<>();
		Map<String, Object> keyValMap = areaIdValueMap(); 
		Map<String, ValueObject> areaIdValueMap =(Map<String, ValueObject>) keyValMap.get("map");
		/*areaRepository.findByParentAreaIdOrderByAreaName(1).stream().forEach(district -> {
			districtIds.add(district.getAreaId());
		
			ValueObject vo = new ValueObject();
			vo.setAreaId(district.getAreaId().toString());
			vo.setAreaLevelId(2);
			vo.setAreaName(district.getAreaName());
			vo.setCssClass("zeroslices");
			areaIdValueMap.put(district.getAreaId().toString(), vo);
			
		});*/
		List<DataValue> dataValues = dataValueRepository.findByInidAndAreaIdIn(inid,   (List<Integer>) keyValMap.get("area"));
		for(DataValue dv: dataValues) {
			ValueObject vo =  areaIdValueMap.get(dv.getAreaId().toString());
			vo.setNumerator(dv.getNumerator());
			vo.setDenominator(dv.getDenominator());
			vo.setValue( Double.parseDouble(formatter.format(dv.getDataValue())));
			vo.setCssClass(vo.getValue() <= 25 ? "firstslices" : vo.getValue() <=50 ? "secondslices" : vo.getValue() <=75 ? "thirdslices" : "fourthslices");
		}
		return areaIdValueMap;
	}
	@Override
	public List<ValueObject> getSkillWiseData(Integer areaId){
		
		List<ValueObject> dataVals = new ArrayList<>();
		Map<String, ValueObject> inidValueMap = new HashMap<>();
		List<Integer> inids = new ArrayList<>();
		indicatorRepository.getIndicatorByIndicatorName("Percentage of migrants with type of skill").stream().forEach(ind -> {
		 
			inids.add(Integer.parseInt(ind.getIndicatorDataMap().get("indicatorNid").toString()));
			
			ValueObject vo = new ValueObject();
			vo.setName(ind.getIndicatorDataMap().get("subgroup").toString());
			inidValueMap.put(ind.getIndicatorDataMap().get("indicatorNid").toString(), vo);
	 });
		
		List<DataValue> values = dataValueRepository.findByAreaIdAndInidIn(areaId, inids);
		values.stream().forEach(value -> {
			ValueObject vo = inidValueMap.get(value.getInid().toString());
			vo.setNumerator(value.getNumerator());
			vo.setDenominator(value.getDenominator());
			vo.setValue(Double.parseDouble(formatter.format(value.getDataValue())));
			vo.setInid(value.getInid());
			dataVals.add(vo);
		});
		
		return dataVals;
		
		//Percentage of migrants with type of skill
	}
	
	@Override
	public boolean isDataAvailable() {
		return dataValueRepository.findAll().size() > 0 ? true : false;
	}
	
///*	@Cacheable(value="areadata")  
	private Map<Integer, ChartDataModel> getAllAreaChart() {
		System.out.println("area from method");
		Map<Integer, ChartDataModel> areaIdValueMap = new HashMap<>();
		areaRepository.findByParentAreaIdOrderByAreaName(-1).stream().forEach(state -> {
			ChartDataModel chartDataModel = new ChartDataModel();
			chartDataModel.setAxis(state.getAreaName());	
			chartDataModel.setValue("0");
			chartDataModel.setNumerator("0");
			chartDataModel.setDenominator("0");
			chartDataModel.setTooltipValue("0");
			chartDataModel.setDblVal(Double.parseDouble(chartDataModel.getValue()));
			areaIdValueMap.put(state.getAreaId(), chartDataModel);
		});
		
		return areaIdValueMap;
	}
//	@Cacheable(value="mapdata")  
	private Map<String, Object> areaIdValueMap(){
		System.out.println("mapdata");
		Map<String, Object> keyValMap = new HashMap<>();
		List<Integer> districtIds = new ArrayList<>();
		Map<String, ValueObject> areaIdValueMap = new HashMap<>();
		
		areaRepository.findByParentAreaIdOrderByAreaName(1).stream().forEach(district -> {
			districtIds.add(district.getAreaId());
		
			ValueObject vo = new ValueObject();
			vo.setAreaId(district.getAreaId().toString());
			vo.setAreaLevelId(2);
			vo.setAreaName(district.getAreaName());
			vo.setCssClass("zeroslices");
			areaIdValueMap.put(district.getAreaId().toString(), vo);
			
		});
		keyValMap.put("map", areaIdValueMap);
		keyValMap.put("area", districtIds);
		return keyValMap;
	}
	
	@Override
	public Map<String, ValueObject> getSkillTypeThematicMapData(Integer inid){
		
		Map<String, Object> keyValMap = areaIdValueMap(); 
//		List<Integer> districtIds = (List<Integer>) areaIdValueMap().get("area");
		Map<String, ValueObject> areaIdValueMap =(Map<String, ValueObject>) keyValMap.get("map");
		DataValue upData = dataValueRepository.findByInidAndAreaIdIn(inid, Arrays.asList(1)).get(0);
		List<DataValue> dataValues = dataValueRepository.findByInidAndAreaIdIn(inid,  (List<Integer>) keyValMap.get("area"));
		
		for(DataValue dv: dataValues) {
			ValueObject vo =  areaIdValueMap.get(dv.getAreaId().toString());
			vo.setNumerator(dv.getNumerator());
			vo.setDenominator(upData.getNumerator());
			vo.setValue(Double.parseDouble(formatter.format(vo.getDenominator()!= null
					? (Double.valueOf(String.valueOf(vo.getNumerator())) * 100)
							/ Double.valueOf(String.valueOf(vo.getDenominator()))
					: null)));
			vo.setCssClass(vo.getValue() <= 25 ? "firstslices" : vo.getValue() <=50 ? "secondslices" : vo.getValue() <=75 ? "thirdslices" : "fourthslices");
		}
		
		return areaIdValueMap;
	}
	
	@Override
	public Map<String, ValueObject> getStateThematicMapData(Integer originStateId){

		Map<String, Object> keyValMap = areaIdValueMap(); 
		Map<String, ValueObject> areaIdValueMap =(Map<String, ValueObject>) keyValMap.get("map");
		DataValue upData = dataValueRepository.findByInidAndAreaIdAndOriginStateId(82, 1, originStateId);
		List<DataValue> dataValues = dataValueRepository.findByInidAndOriginStateIdAndAreaIdIn(82, originStateId, (List<Integer>) keyValMap.get("area"));
		
		for(DataValue dv: dataValues) {
			ValueObject vo =  areaIdValueMap.get(dv.getAreaId().toString());
			vo.setNumerator(dv.getNumerator());
			vo.setDenominator(upData.getNumerator());
			vo.setValue(Double.parseDouble(formatter.format(vo.getDenominator()!= null
					? (Double.valueOf(String.valueOf(vo.getNumerator())) * 100)
							/ Double.valueOf(String.valueOf(vo.getDenominator()))
					: null)));
			vo.setCssClass(vo.getValue() <= 25 ? "firstslices" : vo.getValue() <=50 ? "secondslices" : vo.getValue() <=75 ? "thirdslices" : "fourthslices");
		}
		
		return areaIdValueMap;
	}
}
