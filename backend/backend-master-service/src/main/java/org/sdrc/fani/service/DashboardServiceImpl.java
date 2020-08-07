package org.sdrc.fani.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.fani.collections.AllChecklistFormData;
import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.DataValue;
import org.sdrc.fani.collections.DesignationPartnerFormMapping;
import org.sdrc.fani.collections.GroupIndicator;
import org.sdrc.fani.collections.Indicator;
import org.sdrc.fani.collections.Partner;
import org.sdrc.fani.collections.Sector;
import org.sdrc.fani.collections.TimePeriod;
import org.sdrc.fani.models.ChartDataModel;
import org.sdrc.fani.models.GroupChartDataModel;
import org.sdrc.fani.models.IndicatorGroupModel;
import org.sdrc.fani.models.LegendModel;
import org.sdrc.fani.models.PushpinModel;
import org.sdrc.fani.models.SectorModel;
import org.sdrc.fani.models.SubSectorModel;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.repositories.AllChecklistFormDataRepository;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.ClusterDataValueRepository;
import org.sdrc.fani.repositories.ClusterForAggregationRepository;
import org.sdrc.fani.repositories.DataDomainRepository;
import org.sdrc.fani.repositories.DesignationPartnerFormMappingRepository;
import org.sdrc.fani.repositories.GroupIndicatorRepository;
import org.sdrc.fani.repositories.IndicatorRepository;
import org.sdrc.fani.repositories.PartnerRepository;
import org.sdrc.fani.repositories.SectorRepository;
import org.sdrc.fani.repositories.SubsectorRepository;
import org.sdrc.fani.repositories.TimePeriodRepository;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;

@Service
public class DashboardServiceImpl implements DashboardService {
	
	@Autowired
	GroupIndicatorRepository groupIndicatorRepository;
	
	@Autowired
	private ClusterDataValueRepository clusterDataValueRepository;
	
	@Autowired
	private ClusterForAggregationRepository clusterForAggregationRepository;
	
	@Autowired
	private DataDomainRepository dataValueRepository;
	
	@Autowired
	private AreaRepository areaRepository;
	
	@Autowired
	private DesignationPartnerFormMappingRepository designationPartnerFormMappingRepository;
	
	@Autowired
	private SectorRepository sectorRepository;
	
	@Autowired
	private IndicatorRepository indicatorRepository;
	
	@Autowired
	private TimePeriodRepository timePeriodRepository;
	
	@Autowired
	private PartnerRepository partnerRepository;
	
	@Autowired
	private SubsectorRepository subsectorRepository;
	
	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;
	
	@Autowired
	@Qualifier("mongoDesignationRepository")
	private DesignationRepository designationRepository;
	
	@Autowired
	private ConfigurableEnvironment environment;

	@Autowired
	private EngineFormRepository engineFormRepository;
	
	@Autowired
	private AllChecklistFormDataRepository submissionRepository;
	
	@Override
	public String pushIndicatorGroupData() {

//		FormSectorMapping formSectorMapping = null;
		try {
			List < GroupIndicator > indicatorModels = new LinkedList <> ();

			GroupIndicator groupIndicatorModel = null;
			
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			URL url = loader.getResource("indicatorGroup/");
			String path = url.getPath().replaceAll("%20", " ");
			File files[] = new File(path).listFiles();

			if (files == null) {
				throw new RuntimeException("No file found in path " + path);
			}
			
			for (int f = 0; f < files.length; f++) {

				XSSFWorkbook workbook = null;

				try {
					workbook = new XSSFWorkbook(files[f]);
				} catch (InvalidFormatException | IOException e) {

					e.printStackTrace();
				}
		
			
			
			
			
			XSSFSheet sheet = workbook.getSheet("Sheet1");

			XSSFRow row = null;
			XSSFCell cell = null;
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {

				groupIndicatorModel = new GroupIndicator();

				int colNum = 0;

				row = sheet.getRow(rowNum);
				cell = row.getCell(colNum);
				groupIndicatorModel.setIndicatorGroup(cell.getStringCellValue());

//				indicatorGroup	kpiIndicator	chartType	chartIndicators	sector	sectorId	subSector	

				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null && cell.getCellTypeEnum() == CellType.NUMERIC) {
					groupIndicatorModel.setKpiIndicator((int)cell.getNumericCellValue());
				}else if(cell!=null && cell.getCellTypeEnum() == CellType.STRING && !cell.getStringCellValue().equals("")) {
					groupIndicatorModel.setKpiIndicator(Integer.parseInt(cell.getStringCellValue()));
				}
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setChartType(cell!=null && cell.getCellTypeEnum() != CellType.BLANK ? new ArrayList <  > (Arrays.asList(cell.getStringCellValue())): Arrays.asList(""));

				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setChartIndicators(getBarChartIndicators(cell.getStringCellValue()));
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setSector(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				
				if(cell!=null && cell.getCellTypeEnum() == CellType.NUMERIC) {
					groupIndicatorModel.setSectorId(String.valueOf((int) cell.getNumericCellValue()));
				}else if(cell!=null && cell.getCellTypeEnum() == CellType.STRING) {
					groupIndicatorModel.setSectorId(cell.getStringCellValue());
				}
				
//				groupIndicatorModel.setSectorId(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setSubSector(cell.getStringCellValue());

				
//				kpiChartHeader	chartHeader	
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setKpiChartHeader(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setChartHeader(cell.getStringCellValue());
				
//				cardType	chartLegends	colorLegends	
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setCardType(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setChartLegends(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setColorLegends(cell.getStringCellValue());
				
//				align	valueFrom	unit	chartGroup
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setAlign(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				
				if(cell!=null && cell.getCellTypeEnum() == CellType.NUMERIC) {
					groupIndicatorModel.setValueFrom(String.valueOf((int) cell.getNumericCellValue()));
				}else if(cell!=null && cell.getCellTypeEnum() == CellType.STRING) {
					groupIndicatorModel.setValueFrom(cell.getStringCellValue());
				}
				
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setUnit(cell.getStringCellValue());

				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setChartGroup(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				groupIndicatorModel.setExtraInfo(cell!=null ? cell.getStringCellValue() : "");
				
				colNum++;
				cell = row.getCell(colNum);
				
				if(cell!=null && cell.getCellTypeEnum() == CellType.NUMERIC) {
					groupIndicatorModel.setOrderBy((int) cell.getNumericCellValue());
				}else if(cell!=null && cell.getCellTypeEnum() == CellType.STRING) {
					groupIndicatorModel.setOrderBy(Integer.parseInt(cell.getStringCellValue()));
				}
				
				
				/*colNum++;
				cell = row.getCell(colNum);
				if(cell!=null)
				groupIndicatorModel.setParentChart(cell.getStringCellValue());*/

				indicatorModels.add(groupIndicatorModel);

			}
			/*sheet = null;
			sheet = workbook.getSheet("Sheet2");

			row = null;
			cell = null;
			List < FormSectorMapping > formSectorMappingList = new ArrayList <  > ();
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				formSectorMapping = new FormSectorMapping();
				int colNum = 0;

				row = sheet.getRow(rowNum);
				cell = row.getCell(colNum);
				formSectorMapping.setFormId((int)cell.getNumericCellValue());

				colNum++;
				cell = row.getCell(colNum);
				formSectorMapping.setSectorName(cell.getStringCellValue());
				
				colNum++;
				cell = row.getCell(colNum);
				formSectorMapping.setSectorId((int)cell.getNumericCellValue());

				formSectorMappingList.add(formSectorMapping);

			}80-82
			*/

			workbook.close();
			
			groupIndicatorRepository.save(indicatorModels);
//			formSectorMappingRepository.save(formSectorMappingList);
			
		}} catch (IOException e) {
			e.printStackTrace();
		}finally {
			
		}
		
		return "done";
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

	@Override
	public List<SectorModel> getDashboardData(Integer areaId,String partnerId,String sectorNames) {
		
	List<TimePeriod> lastimePeriods = timePeriodRepository.findByPeriodicity("0");
		//System.out.println(lastTwoTimePeriods.get(lastTwoTimePeriods.size()-2).getTimePeriodId());	
		List<Sector> listOfSectors = null;
		List<Sector> sectorsByPartner = null;
		List<String> sectorsIdByPartner = new ArrayList<String>();
		Set<Integer> setofFormIds = new HashSet<>();
		if(areaId==null) {
			areaId=2;
		}
		
		if (partnerId != null) {
			List<DesignationPartnerFormMapping> listOfDesignationPartnerFormMapping = designationPartnerFormMappingRepository
					.findBypartnerIdIn(Arrays.asList(partnerId));
			Set<Integer> setOfAgregateFormId = new HashSet<>();
			listOfSectors = sectorRepository.findBySectorNameIn(Arrays.asList(sectorNames));
			for (Sector sector : listOfSectors) {
				setOfAgregateFormId.add(sector.getFormId());
			}
			for (DesignationPartnerFormMapping designationPartnerFormMapping : listOfDesignationPartnerFormMapping) {
				for (Integer assignedFormId : designationPartnerFormMapping.getFormId()) {
					if (setOfAgregateFormId.contains(assignedFormId)) {
						setofFormIds.add(assignedFormId);
					}

				}
			}
			
			sectorsByPartner = sectorRepository.findByFormIdInAndSectorName(setofFormIds.stream()
					.map(s -> Integer.valueOf(s))
					.collect(Collectors.toList()),sectorNames);
			for (Sector sector : sectorsByPartner) {
				sectorsIdByPartner.add(sector.getSectorId().toString());
			}
			
			

		}
	
		
		List<SectorModel> sectorModels = new LinkedList<>();
		
		Map<String, Map<String, List<IndicatorGroupModel>>> map = new LinkedHashMap<>();
		
		try {
			
			String lastAggregatedTime = "";
			List<GroupIndicator> groupIndicatorModels = null;
			
			if(partnerId != null) {
				groupIndicatorModels = groupIndicatorRepository.findBySectorIdIn(sectorsIdByPartner).stream()
						.sorted(Comparator.comparing(GroupIndicator::getOrderBy))
						.collect(Collectors.toList());
			}else {
				groupIndicatorModels = groupIndicatorRepository.findBySectorIn(Arrays.asList(sectorNames)).stream()
						.sorted(Comparator.comparing(GroupIndicator::getOrderBy))
						.collect(Collectors.toList());
			}
		
			List<Indicator> indicatorList = null;
		
		
			if(partnerId!=null) {
				indicatorList = indicatorRepository.getIndicatorBySubSectorsAndFormIdIn(Arrays.asList(sectorNames),setofFormIds.stream()
						.map(s -> String.valueOf(s))
						.collect(Collectors.toList()));
			}else {
				indicatorList = indicatorRepository.getIndicatorBySubSectors(Arrays.asList(sectorNames));
			}
		
//			}
			List<Integer> indicatorIds = new LinkedList<>() ;
			Map<Integer, String> indicatorNameMap = new HashMap<>();
			
			for (Indicator indicator : indicatorList) {
				indicatorIds.add(Integer.valueOf((String)indicator.getIndicatorDataMap().get("indicatorNid")));
				indicatorNameMap.put(Integer.valueOf((String)indicator.getIndicatorDataMap().get("indicatorNid")),
						(String) indicator.getIndicatorDataMap().get("indicatorName"));
			}
			
			List<GroupIndicator> trendGroupIndicators = groupIndicatorModels.stream().filter(ind -> ind.getChartType().contains("trend")).collect(Collectors.toList());
			
			List<Integer> trendGroupIndicatorIds = trendGroupIndicators.stream()
					.flatMap(v -> v.getChartIndicators().stream())
					.collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
						return l.stream().flatMap(list -> list.stream()).collect(Collectors.toList());
					}));
			
			List<DataValue> dataValues = new ArrayList<>();
//			Map<Integer, DataValue> mapData = null;
			Map<Integer, DataValue> allMapData = new LinkedHashMap <>();
			Map<Integer,Map<Integer, DataValue>> trendData = new LinkedHashMap <>();
			
			
			Map<Integer,TimePeriod > timePeriodIdMap = new LinkedHashMap<>();
			
			List<TimePeriod> utTimeperiodList = null;
			
			if(!trendGroupIndicatorIds.isEmpty()) {
				// get all tp for trend
				utTimeperiodList = timePeriodRepository.findTop6ByPeriodicityOrderByTimePeriodIdDesc("1");
				utTimeperiodList.remove(0);
				Collections.reverse(utTimeperiodList); 
				
				
						List<DataValue> datavalue = dataValueRepository.findByDatumIdInAndTpAndInidIn(Arrays.asList(areaId), lastimePeriods.get(0).getTimePeriodId(), indicatorIds);
							
							dataValues = datavalue.stream().map(v->{
							DataValue dataValue = new DataValue();
							dataValue.setId(v.getId());
							dataValue.setDataValue(v.getDataValue());
							dataValue.setDatumId(v.getDatumId());
							dataValue.setTp(v.getTp());
							dataValue.set_case(v.get_case());
							dataValue.setInid(v.getInid());
							dataValue.setNumerator(v.getNumerator() != null ? v.getNumerator().toString() : null);
							dataValue.setDenominator(v.getDenominator() != null ? v.getDenominator().toString() : null);
							
							return dataValue;
						}).collect(Collectors.toList());
				
				
				for (DataValue dataValue : dataValues) {

					if (!trendData.containsKey(dataValue.getInid())) {
						Map<Integer, DataValue> newMapData = new LinkedHashMap<>();
						newMapData.put(dataValue.getTp(), dataValue);
						trendData.put(dataValue.getInid(), newMapData);
					} else {
						allMapData = trendData.get(dataValue.getInid());
						if (!allMapData.containsKey(dataValue.getTp())) {
							allMapData.put(dataValue.getTp(), dataValue);
						}
					}
				}

				utTimeperiodList.forEach(timePeriod -> {
					timePeriodIdMap.put(timePeriod.getTimePeriodId(), timePeriod);
				});

			} else {
				dataValues=dataValueRepository.findByDatumIdInAndTpAndInidIn(Arrays.asList(areaId),lastimePeriods.get(0).getTimePeriodId(), indicatorIds);
			}
				
			
			
			
			for (DataValue dataValue : dataValues) {
				allMapData.put(dataValue.getInid(), dataValue);
			}
			
			for (GroupIndicator groupIndicatorModel : groupIndicatorModels) {

				IndicatorGroupModel indicatorGroupModel = new IndicatorGroupModel();

				List<GroupChartDataModel> listOfGroupChartData = null;
				GroupChartDataModel chartDataModel = null;
				List<LegendModel> legendModels = null;
				
				//set static indicator value
				if(groupIndicatorModel.getCardType() != null && groupIndicatorModel.getCardType().equals("static")) {
					indicatorGroupModel.setIndicatorValue(groupIndicatorModel.getKpiIndicator().toString());
				}else {
					if (allMapData != null) {
						indicatorGroupModel
						.setIndicatorValue(allMapData.get(groupIndicatorModel.getKpiIndicator()) == null ? null
								: allMapData.get(groupIndicatorModel.getKpiIndicator()).getDataValue() == null ? null :
								groupIndicatorModel.getUnit().equalsIgnoreCase("percentage") || groupIndicatorModel.getUnit().equalsIgnoreCase("Average")? 
								String.valueOf(Math.round((allMapData.get(groupIndicatorModel.getKpiIndicator()).getDataValue())* 10.0) / 10.0) : 
									String.valueOf(allMapData.get(groupIndicatorModel.getKpiIndicator()).getDataValue().intValue()));
//						indicatorGroupModel
//								.setIndicatorValue(allMapData.get(groupIndicatorModel.getKpiIndicator()) == null ? null
//										: String.valueOf(allMapData.get(groupIndicatorModel.getKpiIndicator())
//														.getDataValue().intValue()));
					}
				}

				
//				indicatorGroupModel.setTimeperiod(tp.getTimePeriod() + ", " + tp.getYear());
//				indicatorGroupModel.setTimeperiodId(tp.getTimePeriodId());
//				indicatorGroupModel.setPeriodicity(tp.getPeriodicity());

				String kpiInd = null;
				
				if(groupIndicatorModel.getValueFrom()!=null && groupIndicatorModel.getValueFrom().contains("=")) {
					String[] arrs = groupIndicatorModel.getValueFrom().split(",");
					for(String ar: arrs) {
						
						String[] each = ar.split("=");
						
					}
					
				}else if(groupIndicatorModel.getValueFrom()!=null) {
					kpiInd = groupIndicatorModel.getValueFrom();
				}
				
					
				if(!groupIndicatorModel.getChartType().get(0).contains("card") && kpiInd!=null && !kpiInd.equals("")){
					indicatorGroupModel
							.setIndicatorValue(allMapData.get(Integer.parseInt(kpiInd))!=null ?
									String.valueOf(allMapData.get(Integer.parseInt(kpiInd)).getDataValue().intValue()) : null);
				}
//				groupIndicatorModel.getValueFrom().contains("=")? groupIndicatorModel.getValueFrom();
//				Number of aspirational district === is aggregated in state level,
				//to show district level data put value as 1

				String kpiIndName = groupIndicatorModel.getKpiChartHeader()!=null ?
						groupIndicatorModel.getKpiChartHeader().contains("@")
						? (groupIndicatorModel.getKpiChartHeader().split("@")[0]
								+ (allMapData.get(Integer.parseInt(kpiInd))!=null ? allMapData.get(Integer.parseInt(kpiInd))
										.getDataValue().intValue() : 1)
								+ groupIndicatorModel.getKpiChartHeader().split("@")[1])
						: groupIndicatorModel.getKpiChartHeader() : "";

				indicatorGroupModel.setIndicatorName(kpiIndName);
				
				indicatorGroupModel.setIndicatorId(groupIndicatorModel.getKpiIndicator());
				indicatorGroupModel.setChartsAvailable(groupIndicatorModel.getChartType());
				indicatorGroupModel.setAlign(groupIndicatorModel.getAlign());
				indicatorGroupModel.setCardType(groupIndicatorModel.getCardType());
				indicatorGroupModel.setIndicatorGroupName(groupIndicatorModel.getIndicatorGroup());
				indicatorGroupModel.setUnit(groupIndicatorModel.getUnit());
				indicatorGroupModel.setChartAlign(groupIndicatorModel.getChartAlign());
				indicatorGroupModel.setChartGroup(groupIndicatorModel.getChartGroup());
				indicatorGroupModel.setExtraInfo(groupIndicatorModel.getExtraInfo());
				
				//new code 04-06-2019
				
				
				//for trend chart indicators
				if (groupIndicatorModel.getChartType().get(0).contains("trend") && groupIndicatorModel.getChartIndicators()!=null &&
						groupIndicatorModel.getChartIndicators().size() > 0 && trendData != null && !trendData.isEmpty()) {
					listOfGroupChartData = new ArrayList<GroupChartDataModel>();
					chartDataModel = new GroupChartDataModel();

					String indName = groupIndicatorModel.getChartHeader().contains("@")
							? (groupIndicatorModel.getChartHeader().split("@")[0]
									+ allMapData.get(Integer.parseInt(kpiInd))
											.getDataValue().intValue()
									+ groupIndicatorModel.getChartHeader().split("@")[1])
							: groupIndicatorModel.getChartHeader();

					chartDataModel.setHeaderIndicatorName(indName);
//					chartDataModel.setHeaderIndicatorValue(
//							trendData.get(groupIndicatorModel.getChartIndicators().get(0).get(0)) == null ? null :
//											 trendData.get(groupIndicatorModel.getChartIndicators().get(0).get(0))
//													.get(groupIndicatorModel.getHeaderIndicator()).getDataValue()
//													.intValue());
					chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel,
							null, indicatorNameMap, "trend", null, timePeriodIdMap, utTimeperiodList, 
							groupIndicatorModel.getUnit(), trendData));

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

				}else if(groupIndicatorModel.getChartIndicators()!=null &&
						groupIndicatorModel.getChartIndicators().size() > 0 && allMapData!=null ){ //for all other chart types
					listOfGroupChartData = new ArrayList<GroupChartDataModel>();
					chartDataModel = new GroupChartDataModel();

					String indName = groupIndicatorModel.getChartHeader().contains("@")
							? (groupIndicatorModel.getChartHeader().split("@")[0]
									+ allMapData.get(Integer.parseInt(kpiInd))
											.getDataValue().intValue()
									+ groupIndicatorModel.getChartHeader().split("@")[1])
							: groupIndicatorModel.getChartHeader();

					chartDataModel.setHeaderIndicatorName(indName);
					
//					chartDataModel.setHeaderIndicatorValue(allMapData.get(groupIndicatorModel.getHeaderIndicator()) == null ? null
//									: allMapData.get(groupIndicatorModel.getHeaderIndicator()).getDataValue().intValue());
					
					chartDataModel.setChartDataValue(getChartDataValue(groupIndicatorModel, allMapData, indicatorNameMap, groupIndicatorModel.getChartType().get(0)
							, null, timePeriodIdMap, null, groupIndicatorModel.getUnit(), null));
					
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
				
				//end
				

				if (!map.containsKey(groupIndicatorModel.getSector())) {

					Map<String, List<IndicatorGroupModel>> subsectorGrMapModel = new LinkedHashMap<>();

					List<IndicatorGroupModel> sectorNewIndicators = new LinkedList<>();
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
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sectorModels;
	}

	private List<List<ChartDataModel>> getChartDataValue(GroupIndicator groupIndicatorModel,
			Map<Integer, DataValue> mapData, Map<Integer, String> indicatorNameMap, String chartType,
			Map<Integer, String> cssGroupMap, Map<Integer, TimePeriod> timePeriodIdMap,
			List<TimePeriod> utTimeperiodList, String unit,  Map<Integer, Map<Integer, DataValue>> trendData) {
		
		List<List<Integer>> chartIndicators = null;
		
		String[] axisList = null ;
		chartIndicators = new ArrayList<>();

		chartIndicators = groupIndicatorModel.getChartIndicators();
		
		if(groupIndicatorModel.getChartLegends()!=null)
			axisList = groupIndicatorModel.getChartLegends().split(",");
		
		if(chartType.equals("pie") || chartType.equals("donut"))
			axisList = groupIndicatorModel.getColorLegends().split(",");
				
		List<List<ChartDataModel>> listChartDataModels = new LinkedList<>();
		List<ChartDataModel> chartDataModels = null;
		
		if(chartType.equals("trend")) {
			for (List<Integer> indList : chartIndicators) {
				chartDataModels = new LinkedList<>();
				
				for (int i=0; i< indList.size(); i++) {
					
					if(null!=utTimeperiodList) {
						for(TimePeriod timePeriod :utTimeperiodList) {
							String axis = null;
							axis = trendData ==null || trendData.get(timePeriod.getTimePeriodId()) == null ? timePeriod.getTimePeriod() + "-" +timePeriod.getYear()  :
								timePeriodIdMap.get(timePeriod.getTimePeriodId()).getTimePeriod() + "-"+
									timePeriodIdMap.get(timePeriod.getTimePeriodId()).getYear();
						
							/*getChartDataModel(indicatorNameMap, indList.get(i), trendData.get(indList.get(i)),"trend", 
									chartDataModels, cssGroupMap, axis, timePeriod.getTimePeriodId(), unit, 
									dashboardType,(axisList!= null? axisList[i].split("_")[1] : null));*/
						};
					}
				}
				listChartDataModels.add(chartDataModels);
			}
					
					
		}else {
			mapData.get(groupIndicatorModel.getChartIndicators().get(0).get(0));
			
			int grCount = 0;
			for (List<Integer> indList : chartIndicators) {
					chartDataModels = new LinkedList<>();
					
					for (int i=0; i< indList.size(); i++) {
						
						String axis = null;
						axis = axisList!= null? axisList[i].split("_")[1] : null;
						
						String label = null;
						
						if(chartType.equals("stack"))
							label = groupIndicatorModel.getColorLegends()!= null? groupIndicatorModel.getColorLegends().split(",")[grCount].split("_")[1] : null;
						
					
						getChartDataModel(indicatorNameMap, indList.get(i), mapData,"all", chartDataModels, cssGroupMap, axis, null, unit, label);
					}
						listChartDataModels.add(chartDataModels);
						
						grCount++;
				}
			
			
		}
		
		return listChartDataModels;
	}

	private List<ChartDataModel> getChartDataModel(Map<Integer, String> indicatorNameMap, Integer indiId,
			Map<Integer, DataValue> mapData, String numeDeno, List<ChartDataModel> chartDataModels,
			Map<Integer, String> cssGroupMap, String axis, Integer timePeriodId, String unit,  String label) {
		
		ChartDataModel chartDataModel = new ChartDataModel();
		chartDataModel.setAxis(axis.trim());
		chartDataModel.setLabel(label != null ? label.trim() : null);
		if(numeDeno.equals("all")) {
			
		/*	if(dashboardType.equals("COVERAGE")) { //in coverage dashboard put 0 instead of null
				chartDataModel.setValue(
						(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getDataValue() == null) ? "0"
								: unit.equalsIgnoreCase("percentage")
										? String.valueOf(Math.round((mapData.get(indiId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(indiId).getDataValue().intValue()));
			}else {*/
				chartDataModel.setValue(
						(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getDataValue() == null) ? null
								: unit.equalsIgnoreCase("percentage")
										? String.valueOf(Math.round((mapData.get(indiId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(indiId).getDataValue().intValue()));
		//	}
			

			chartDataModel.setNumerator(
					(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getNumerator() == null)
							? null
							: String.valueOf(Math.round(Double.parseDouble ( mapData.get(indiId).getNumerator()))));
			chartDataModel.setDenominator(
					(mapData == null || mapData.get(indiId) == null || mapData.get(indiId).getDenominator() == null)
							? null
							: String.valueOf(Math.round(Double.parseDouble ( mapData.get(indiId).getDenominator()))));
		}else { //trend chart
			
		/*	if(dashboardType.equals("COVERAGE")) { //in coverage dashboard put 0 instead of null
				chartDataModel
				.setValue((mapData == null || mapData.get(timePeriodId) == null
						|| mapData.get(timePeriodId).getDataValue() == null)
								? "0"
								: unit.equalsIgnoreCase("percentage") ? String.valueOf(
										Math.round((mapData.get(timePeriodId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(timePeriodId).getDataValue().intValue()));
			}else {*/
				chartDataModel
				.setValue((mapData == null || mapData.get(timePeriodId) == null
						|| mapData.get(timePeriodId).getDataValue() == null)
								? null
								: unit.equalsIgnoreCase("percentage") ? String.valueOf(
										Math.round((mapData.get(timePeriodId).getDataValue()) * 10.0) / 10.0)
										: String.valueOf(mapData.get(timePeriodId).getDataValue().intValue()));
			//}
			

			chartDataModel.setNumerator((mapData == null || mapData.get(timePeriodId) == null
					|| mapData.get(timePeriodId).getNumerator() == null) ? null
							: String.valueOf(Math.round(Double.parseDouble(mapData.get(timePeriodId).getNumerator()))));
			chartDataModel.setDenominator((mapData == null || mapData.get(timePeriodId) == null
					|| mapData.get(timePeriodId).getDenominator() == null) ? null
							: String.valueOf(
									Math.round(Double.parseDouble(mapData.get(timePeriodId).getDenominator()))));
		}
		chartDataModel.setLegend(axis.trim());
		chartDataModel.setId(indiId);
		chartDataModel.setUnit(unit);
		chartDataModel.setKey(chartDataModel.getLabel() != null ? chartDataModel.getLabel() : null);
		chartDataModels.add(chartDataModel);
		
		return chartDataModels;
	}

	private List<LegendModel> getLegendModel() {
		List<LegendModel> legendModels = new ArrayList<>();
		legendModels.add(setLegendModel("#d7191c", "0-30", 0.0, 30.0));
		legendModels.add(setLegendModel("#fdae61", "30-50", 30.1, 50.0));
		legendModels.add(setLegendModel("#a6d96a", "50-70", 50.1, 70.0));
		legendModels.add(setLegendModel("#1a9641", "Above 70", 70.1, 100.0));
		return legendModels;
	}

	private LegendModel setLegendModel(String color, String range, double startRange, double endRange) {
		LegendModel legendModel = new LegendModel();
		legendModel.setColor(color);
		legendModel.setRange(range);
		legendModel.setStartRange(startRange);
		legendModel.setEndRange(endRange);
		
		return legendModel;
	}

	@Override
	public List<String> getAllSectors(OAuth2Authentication auth) {
		
		UserModel userModel = tokenInfoExtracter.getUserModelInfo(auth);
		Designation desg = designationRepository.findById(userModel.getRoleIds().iterator().next());
		List<Sector> listOfSectors = sectorRepository.findAll();
		Set<String> setOfSectors = new LinkedHashSet<>() ;
		//setOfSectors.add("Overview");
		if(desg.getCode().equalsIgnoreCase("ADMIN") || desg.getCode().equalsIgnoreCase("MANAGE-DASHBOARD")){
			for (Sector sector : listOfSectors) {
				setOfSectors.add(sector.getSectorName());
			}
		}else {
			for (Sector sector : listOfSectors) {
				if(!sector.getSectorName().equalsIgnoreCase("Review")) {
				setOfSectors.add(sector.getSectorName());
				}
			}
			
		}
		
		
	
		
		
		
		return setOfSectors.stream().collect(Collectors.toList()); 
	}

	@Override
	public List<Partner> getSectorWisePartner(String SectorName) {
		List<Sector> listOfSectors = sectorRepository.findBySectorNameIn(Arrays.asList(SectorName));
		
		List<DesignationPartnerFormMapping> listOfDesignationPartnerFormMapping = designationPartnerFormMappingRepository.findAll();
		Set<String> listOfPartnerId = new HashSet<>();
		for (Sector sector : listOfSectors) {
			for (DesignationPartnerFormMapping designationPartnerFormMapping : listOfDesignationPartnerFormMapping) {
				if(designationPartnerFormMapping.getFormId().contains(sector.getFormId()) && designationPartnerFormMapping.getPartnerId()!=null) {
					listOfPartnerId.add(designationPartnerFormMapping.getPartnerId());
					
					//System.out.println(sector.getSectorId()+"--"+sector.getSectorName()+"--"+designationPartnerFormMapping.getPartnerId());
				}
			}
		}
		
		List<Partner> listOfPartner = partnerRepository.findByIdIn(listOfPartnerId.stream().collect(Collectors.toList()));
		return listOfPartner;
	}
	
	@Override
	public List<Area> getPartnerWiseArea(String partnerId) {
		Map<String,List<Integer>> partnerAreaMap = new HashMap<>();
		String partnerareaMappings = environment.getProperty("partner.area.mapping");
		
		for (String partnerareaMapping : partnerareaMappings.split("#")) {
			partnerAreaMap.put(partnerareaMapping.split("@")[0].toString(),Stream.of(partnerareaMapping.split("@")[1].split(","))
	                .map(Integer::parseInt)
	                .collect(Collectors.toList()));
		}
		if(partnerId != null) {
			return areaRepository.findByAreaIdIn(partnerAreaMap.get(partnerId));
		}else {
			return areaRepository.findByParentAreaId(2);
		}
		
		
	}

	@Override
	public List<PushpinModel> getpushpinData(Integer indicatorId ,Integer areaId) {
		
		List<Indicator> indicator = indicatorRepository.getIndicatiorsIn(Arrays.asList(indicatorId.toString()));
	List<AllChecklistFormData> submissions = submissionRepository.findByFormId(Integer.parseInt((String) indicator.get(0).getIndicatorDataMap().get("formId")));
	List<PushpinModel> listOfLongLat = new ArrayList<PushpinModel>();
	
	List<DataValue> datavalue = dataValueRepository.findByDatumIdInAndTpAndInid(Arrays.asList(areaId), 1, indicatorId);
	if(!datavalue.isEmpty() && datavalue != null) {
	switch ((String) indicator.get(0).getIndicatorDataMap().get("formId")) {
	case "10":
		listOfLongLat = findLatLong(submissions,"f10q014","f10q004_gp","f10q005_vil","f10q006_vil","f10q002_dist",areaId);
		break;
		
	case "7":
		listOfLongLat = findLatLong(submissions,"F739",null,"F705",null,"F702",areaId);
		break;
		
	case "8":
		listOfLongLat = findLatLong(submissions,"F818",null,"F805",null,"F802",areaId);
		break;
		
	case "40":
		listOfLongLat = findLatLong(submissions,"cci16",null,"cci04_cci",null,"cci02_district",areaId);
		break;
		
	case "27":
		listOfLongLat = findLatLong(submissions,"cfd11","cfd4_gp","cfd5_village","cfd6_other","cfd2_district",areaId);
		break;
		
	case "31":
		listOfLongLat = findLatLong(submissions,"skds13","skds4_gp","skds5_village","skds6","skds2_district",areaId);
		break;

	
	}
	}
		return listOfLongLat;
	}

	private List<PushpinModel> findLatLong(List<AllChecklistFormData> submissions, String geoTag_ColumnName,
			String gp_colName, String vil_colName, String otherVillage_colName, String districtColumnName, int areaId) {
		List<PushpinModel> listOfLongLat = new ArrayList<PushpinModel>();
		ObjectMapper oMapper = new ObjectMapper();
		int n=1;
		for (AllChecklistFormData allChecklistFormData : submissions) {
			Area gpArea = null;
			Area villageArea = null;
			String otherVillageName = null;
			String awcName = null;
			String cciName = null;

			if (allChecklistFormData.getFormId() == 10) {
					if (gp_colName != null) {
						if (allChecklistFormData.getData().get(gp_colName) != null) {
							gpArea = areaRepository
									.findByAreaId((Integer) allChecklistFormData.getData().get(gp_colName));
						}
					}

					if (allChecklistFormData.getData().get(vil_colName) != null) {
						Map<String, Object> map = oMapper.convertValue(allChecklistFormData.getData().get(vil_colName),
								Map.class);
						if ((Integer) map.get("key") == 8797) {
							otherVillageName = (String) allChecklistFormData.getData().get(otherVillage_colName);
						} else {
							otherVillageName = (String) map.get("value");
						}

				}
			} else if (allChecklistFormData.getFormId() == 27 || allChecklistFormData.getFormId() == 31) {
					//System.out.println();
					if (gp_colName != null) {
						if (allChecklistFormData.getData().get(gp_colName) != null) {
							gpArea = areaRepository
									.findByAreaId((Integer) allChecklistFormData.getData().get(gp_colName));
						}
					}

					if (allChecklistFormData.getData().get(vil_colName) != null) {
						villageArea = areaRepository
								.findByAreaId((Integer) allChecklistFormData.getData().get(gp_colName));
					}

			} else if (allChecklistFormData.getFormId() == 7 || allChecklistFormData.getFormId() == 8) {

					if (allChecklistFormData.getData().get(vil_colName) != null) {
						Map<String, Object> map = oMapper.convertValue(allChecklistFormData.getData().get(vil_colName),
								Map.class);
						if ((Integer) map.get("key") == 8797) {
							awcName = (String) allChecklistFormData.getData().get(otherVillage_colName);
						} else {
							awcName = (String) map.get("value");
						}

				}
			} else if (allChecklistFormData.getFormId() == 40) {
					if (allChecklistFormData.getData().get(vil_colName) != null) {
						Map<String, Object> map = oMapper.convertValue(allChecklistFormData.getData().get(vil_colName),
								Map.class);
						if ((Integer) map.get("key") == 8797) {
							cciName = (String) allChecklistFormData.getData().get(otherVillage_colName);
						} else {
							cciName = (String) map.get("value");
						}

					}


			}

			String latlong = null;
			if (allChecklistFormData.getData().get(geoTag_ColumnName) != null) {
				if ((Integer) allChecklistFormData.getData().get(districtColumnName) == areaId) {
				latlong = ((String) allChecklistFormData.getData().get(geoTag_ColumnName)).replace("Lat :", "")
						.replace("Long :", "");

				PushpinModel pushpinModel = new PushpinModel();

				pushpinModel.setLatitude(latlong != null ? latlong.split(" ")[0] : null);
				pushpinModel.setLongitude(latlong != null ? latlong.split(" ")[1] : null);
				pushpinModel.setGpName(gpArea != null ? gpArea.getAreaName() : null);
				pushpinModel.setAwcName(awcName == null ? null : awcName);
				pushpinModel.setCciName(cciName == null ? null : cciName);
				pushpinModel.setVillageName(villageArea != null ? villageArea.getAreaName()
						: otherVillageName == null ? null : otherVillageName);
				listOfLongLat.add(pushpinModel);
			}
			}

		}
		return listOfLongLat;
	}

	@Override
	public String exportGroupIndicator() throws Exception {

	      //Create blank workbook
	      XSSFWorkbook workbook = new XSSFWorkbook(); 

	      //Create a blank sheet
	      XSSFSheet spreadsheet = workbook.createSheet("Sheet1");
	      String headings = "indicatorGroup#kpiIndicator#chartType#chartIndicators#sector#sectorId#subSector#kpiChartHeader#chartHeader#cardType#chartLegends#colorLegends#align#valueFrom#unit#chartGroup#extraInfo#orderBy";

	      int rowid = 0;
	      int cellid = 0;
	      Cell cell = null;
	      XSSFRow row = spreadsheet.createRow(rowid);
	      for (String heading : headings.split("#")) {
	    	   cell = row.createCell(cellid);
		      cell.setCellValue(heading);
		      ++cellid;
		}
	      List<GroupIndicator> listOfGroupIndicators = groupIndicatorRepository.findAll();
	      
	      for (GroupIndicator groupIndicators : listOfGroupIndicators) {
	    	  row = spreadsheet.createRow(++rowid);
	    	  //System.out.println(rowid);
	    	  
	    	  if(rowid==85) {
	    		 // System.out.println(rowid);
	    	  }
	    	  
	    	  cellid = 0;
	    	  cell = row.createCell(cellid);
	    	  cell.setCellValue(groupIndicators.getIndicatorGroup()==null?"":groupIndicators.getIndicatorGroup().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  if(groupIndicators.getKpiIndicator()!=null) {
	    		  cell.setCellValue(groupIndicators.getKpiIndicator());  
	    	  }
	    	  
	    	  
	    	 
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getChartType().isEmpty() || groupIndicators.getChartType()==null?"":groupIndicators.getChartType().toString().replace("[", "").replace("]", ""));
	    	  
	    	  cell = row.createCell(++cellid);
	    	  if(groupIndicators.getChartIndicators()!=null) {
	    		  if(!groupIndicators.getChartIndicators().isEmpty()) {
	    	    	  cell.setCellValue(groupIndicators.getChartIndicators().toString().replace("], [", "@").replace("[", "").replace("]", ""));
	    	    	    
	    		  }
	    	  }
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getSector()==null?"":groupIndicators.getSector().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getSectorId()==null?"":groupIndicators.getSectorId().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getSubSector()==null?"":groupIndicators.getSubSector().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getKpiChartHeader()==null?"":groupIndicators.getKpiChartHeader().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getChartHeader()==null?"":groupIndicators.getChartHeader().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getCardType()==null?"":groupIndicators.getCardType().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getChartLegends()==null?"":groupIndicators.getChartLegends().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getColorLegends()==null?"":groupIndicators.getColorLegends().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getAlign()==null?"":groupIndicators.getAlign().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getValueFrom()==null?"":groupIndicators.getValueFrom().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getUnit()==null?"":groupIndicators.getUnit().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getChartGroup()==null?"":groupIndicators.getChartGroup().toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getExtraInfo()==null?"":groupIndicators.getExtraInfo().toString());
	    	  
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(groupIndicators.getOrderBy()==null?"":groupIndicators.getOrderBy().toString());
	    	  
	    	  
		}
	      
	      
	      
	      //Write the workbook in file system
	      FileOutputStream out = new FileOutputStream(new File("D:\\EXPORT\\GroupIndicator"+"_"+new Date().getTime()+".xlsx"));
	      workbook.write(out);
	      out.close();
	      
		return "Success";

	}

	@Override
	public String exportIndicator() throws Exception {

	      //Create blank workbook
	      XSSFWorkbook workbook = new XSSFWorkbook(); 

	      //Create a blank sheet
	      XSSFSheet spreadsheet = workbook.createSheet("Sheet1");
	      String headings = "formId#area#aggregationType#indicatorName#subgroup#aggregationRule#subsector#collection#parentColumn#indicatorNid#parentType#numerator#denominator#unit#periodicity#highIsGood#sector#typeDetailId" + 
	      		"";

	      int rowid = 0;
	      int cellid = 0;
	      Cell cell = null;
	      XSSFRow row = spreadsheet.createRow(rowid);
	      for (String heading : headings.split("#")) {
	    	   cell = row.createCell(cellid);
		      cell.setCellValue(heading);
		      ++cellid;
		}
	      List<Indicator> listOfIndicators = indicatorRepository.findAll();
	      
	      for (Indicator groupIndicators : listOfIndicators) {
	    	  row = spreadsheet.createRow(++rowid);
	    	  //System.out.println(rowid);
	    	  cellid = 0;
	    	  for (String heading : headings.split("#")) {
	    	  cell = row.createCell(cellid);
	    	  cell.setCellValue(groupIndicators.getIndicatorDataMap().get(heading)==null?"":groupIndicators.getIndicatorDataMap().get(heading).toString());
	    	  ++cellid;
	    	  }
	    	  
		}
	      
	      //Write the workbook in file system
	      FileOutputStream out = new FileOutputStream(new File("D:\\EXPORT\\Indicator"+"_"+new Date().getTime()+".xlsx"));
	      workbook.write(out);
	      out.close();
	      
		return "Success";

	}

	@Override
	public String getLastAggregationTime() throws Exception {
		List<TimePeriod> lastimePeriods = timePeriodRepository.findByPeriodicity("0");
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastimePeriods.get(0).getEndDate());
		//cal.add(Calendar.DAY_OF_YEAR, -1);
		Date date = cal.getTime();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        return dateFormat.format(date);
		
	}

	@Override
	public String exportDataValue() throws Exception {
		Map<Integer,Area> areaMap = new HashMap<>();
		List<Area> listOfFetchedArea = areaRepository.findAll();
		areaMap = listOfFetchedArea.stream()
				.collect(Collectors.toMap(Area::getAreaId, area -> area));
		
		Map<Integer,Indicator> indicatorMap = new HashMap<>();
		List<Indicator> listOfFetchedIndicator = indicatorRepository.findAll();
		for (Indicator indicator : listOfFetchedIndicator) {
			indicatorMap.put(Integer.parseInt((String) indicator.getIndicatorDataMap().get("indicatorNid")), indicator);
		}
		
		Map<Integer,EnginesForm> formMap = new HashMap<>();
		List<EnginesForm> listOfFetchedEnginesForm = engineFormRepository.findAll();
		formMap = listOfFetchedEnginesForm.stream()
				.collect(Collectors.toMap(EnginesForm::getFormId, enginesForm -> enginesForm));
		


	      //Create blank workbook
	      XSSFWorkbook workbook = new XSSFWorkbook(); 

	      //Create a blank sheet
	      XSSFSheet spreadsheet = workbook.createSheet("Sheet1");
	      String headings = "Sl. No#CheckList Name#Indicator Name#Area#Numerator_Indicator Name#Numerator Value#Denominator_Indicator Name#Denominator Value#DataValue#Unit";

	      int rowid = 0;
	      int cellid = 0;
	      Cell cell = null;
	      XSSFRow row = spreadsheet.createRow(rowid);
	      for (String heading : headings.split("#")) {
	    	   cell = row.createCell(cellid);
		      cell.setCellValue(heading);
		      ++cellid;
		}
	      List<DataValue> listOfDataValue = dataValueRepository.findAll();
	      int slNo =1;
	      for (DataValue dataValue : listOfDataValue) {
	    	  row = spreadsheet.createRow(++rowid);
	    	  
	    	 
	    	  cellid = 0;
	    	  cell = row.createCell(cellid);
	    	  cell.setCellValue(slNo);
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(formMap.get(Integer.parseInt((String) indicatorMap.get(dataValue.getInid()).getIndicatorDataMap().get("formId"))).getName());
	    	  
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(indicatorMap.get(dataValue.getInid()).getIndicatorDataMap().get("indicatorName").toString());
	    	  
	    	  cell = row.createCell(++cellid);
	    	  if(dataValue.getDatumId() != -1) {
	    	  cell.setCellValue(areaMap.get(dataValue.getDatumId()).getAreaName());
	    	  }
	    	  
	    	  cell = row.createCell(++cellid);
	    	  if(dataValue.getNumerator()!=null) {
	    	  cell.setCellValue(indicatorMap.get(Integer.parseInt((String) indicatorMap.get(dataValue.getInid()).getIndicatorDataMap().get("numerator"))).getIndicatorDataMap().get("indicatorName").toString());
	    	  }
	    	  
	    	  cell = row.createCell(++cellid);
	    	  if(dataValue.getNumerator()!=null) {
	    	  cell.setCellValue(dataValue.getNumerator());
	    	  }
	    	  
	    	  
	    	  cell = row.createCell(++cellid);
	    	  if(dataValue.getNumerator()!=null) {
	    	  cell.setCellValue(indicatorMap.get(Integer.parseInt((String)indicatorMap.get(dataValue.getInid()).getIndicatorDataMap().get("denominator"))).getIndicatorDataMap().get("indicatorName").toString());
	    	  }
	    	  
	    	  cell = row.createCell(++cellid);
	    	  if(dataValue.getNumerator()!=null) {
	    	  cell.setCellValue(dataValue.getDenominator());
	    	  }
	    	  
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(dataValue.getDataValue() != null ? String.valueOf(dataValue.getDataValue()) : "N/A");
	    	  
	    	  
	    	  cell = row.createCell(++cellid);
	    	  cell.setCellValue(indicatorMap.get(dataValue.getInid()).getIndicatorDataMap().get("unit").toString());
	    	  
	    	 ++slNo; 
		}
	      
	      
	      
	      //Write the workbook in file system
	      FileOutputStream out = new FileOutputStream(new File("D:\\EXPORT\\DataValue"+"_"+new Date().getTime()+".xlsx"));
	      workbook.write(out);
	      out.close();
	      
		return "Success";

	
	}

	


}
