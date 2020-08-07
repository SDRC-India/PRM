package org.sdrc.fani.service;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.Organization;
import org.sdrc.fani.collections.Partner;
import org.sdrc.fani.collections.PartnersDetails;
import org.sdrc.fani.collections.TimePeriod;
import org.sdrc.fani.models.OptionModel;
import org.sdrc.fani.models.PartnerDetailsModel;
import org.sdrc.fani.models.PartnerOptionModel;
import org.sdrc.fani.repositories.AreaLevelRepository;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.OrganizationRepository;
import org.sdrc.fani.repositories.PartnerRepository;
import org.sdrc.fani.repositories.PartnersDetailsRepository;
import org.sdrc.usermgmt.mongodb.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;

import in.co.sdrc.sdrcdatacollector.document.Type;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;
//import in.co.sdrc.sdrcdatacollector.models.OptionModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeRepository;

@Service
public class CollectionServiceImpl implements CollectionService {

	@Autowired
	AreaRepository areaRepository;

	@Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	@Qualifier("mongoAccountRepository")
	private AccountRepository accountRepository;

	/*@Autowired
	RegistrationOTPRepository registrationOTPRepository;
*/
//	@Autowired
//	UserDesignationRepository userDesignationRepository;

	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private TypeDetailRepository typeDetailRepository;
	
	@Autowired
	private PartnersDetailsRepository partnersDetailsRepository;
	
	@Autowired
	private AreaLevelRepository areaLevelRepository;
	
	@Autowired
	private PartnerRepository partnerRepository;
	
	
	

	@Override
	public String updateArea() {
		return "area Saved";
	}

	@Override
	public String saveDate() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("organization/");
		String path = url.getPath().replaceAll("%20", " ");
		File[] files = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(files[f]);

				// organization import
				XSSFSheet orgSheet = workbook.getSheetAt(0);
				for (int row = 1; row <= orgSheet.getLastRowNum(); row++) {
					int cols = 0;
					if (orgSheet.getRow(row) == null)
						break;

					XSSFRow xssfRow = orgSheet.getRow(row);

					Cell cell = xssfRow.getCell(cols);
					Organization organization = new Organization();
					organization.setOrganizationId((int) cell.getNumericCellValue());
					++cols;
					Cell cell2 = xssfRow.getCell(cols);
					organization.setOrganizationName(cell2.getStringCellValue());
					organizationRepository.save(organization);

				}
				// userImport
				/*
				 * XSSFSheet areaSheet = workbook.getSheetAt(1);
				 * 
				 * for (int row = 1; row <= orgSheet.getLastRowNum(); row++) {
				 * int cols=0; if (orgSheet.getRow(row) == null) break;
				 * 
				 * XSSFRow xssfRow = orgSheet.getRow(row); Account account = new
				 * Account();
				 * account.setUserName(xssfRow.getCell(cols).getStringCellValue(
				 * )); account.setPassword(xssfRow.getCell(++cols).
				 * getStringCellValue());
				 * account.setEmail(xssfRow.getCell(++cols).getStringCellValue()
				 * ); List<Integer> mappedAreaIds =new ArrayList<>(); String[]
				 * ids =
				 * xssfRow.getCell(++cols).getStringCellValue().split(","); for
				 * (String id : ids) { mappedAreaIds.add(Integer.parseInt(id));
				 * } account.setMappedAreaIds(mappedAreaIds); UserDetails
				 * userDetails =new UserDetails();
				 * userDetails.setOrganization((int)
				 * xssfRow.getCell(++cols).getNumericCellValue());
				 * userDetails.setDesignation((int)
				 * xssfRow.getCell(++cols).getNumericCellValue());
				 * account.setUserDetails(userDetails);
				 * 
				 * accountRepository.save(account); }
				 */

				// FacilityType import
				/*
				 * XSSFSheet facilityTypeareaSheet = workbook.getSheetAt(2); for
				 * (int row = 1; row <= facilityTypeareaSheet.getLastRowNum();
				 * row++) { int cols=0; if (facilityTypeareaSheet.getRow(row) ==
				 * null) break;
				 * 
				 * XSSFRow xssfRow = facilityTypeareaSheet.getRow(row);
				 * FacilityType facilityType = new FacilityType();
				 * facilityType.setFacilityTypeId((int)
				 * xssfRow.getCell(cols).getNumericCellValue());
				 * facilityType.setFacilityTypeName(xssfRow.getCell(++cols).
				 * getStringCellValue() );
				 * facilityTypeRepository.save(facilityType); }
				 */

				// FacilityLevel import
				/*
				 * XSSFSheet facilityTypeareaSheet = workbook.getSheetAt(3); for
				 * (int row = 1; row <= facilityTypeareaSheet.getLastRowNum();
				 * row++) { int cols=0; if (facilityTypeareaSheet.getRow(row) ==
				 * null) break;
				 * 
				 * XSSFRow xssfRow = facilityTypeareaSheet.getRow(row);
				 * FacilityLevel facilityLevel = new FacilityLevel();
				 * facilityLevel.setFacilityLevelId((int)
				 * xssfRow.getCell(cols).getNumericCellValue());
				 * facilityLevel.setFacilityLevelName(xssfRow.getCell(++cols).
				 * getStringCellValue ());
				 * facilityLevelRepository.save(facilityLevel); }
				 */
				XSSFSheet userDesignationSheet = workbook.getSheetAt(4);
//				for (int row = 1; row <= userDesignationSheet.getLastRowNum(); row++) {
//					int cols = 0;
//					if (userDesignationSheet.getRow(row) == null)
//						break;
//
//					XSSFRow xssfRow = userDesignationSheet.getRow(row);
//					UserDesignation userDesignation = new UserDesignation();
//					userDesignation.setDesignationId((int) xssfRow.getCell(cols).getNumericCellValue());
//					userDesignation.setDesignationName(xssfRow.getCell(++cols).getStringCellValue());
//					userDesignationRepository.save(userDesignation);
//				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "done";
	}

	@Override
	public String saveTimePeriod() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM");
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		TimePeriod timePeriod = new TimePeriod();
		timePeriod.setCreatedDate(date);
		timePeriod.setStartDate(cal.getTime());
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		timePeriod.setEndDate(cal.getTime());
		timePeriod.setTimePeriodId(1);
		timePeriod.setTimePeriod(simpleDateFormat.format(date).toUpperCase());
		timePeriod.setPeriodicity("1");
		simpleDateFormat = new SimpleDateFormat("YYYY");
		if (cal.get(Calendar.MONTH) > 2) {
			timePeriod.setFinancialYear(simpleDateFormat.format(date).toUpperCase() + "-"
					+ (Integer.parseInt(simpleDateFormat.format(date).toUpperCase()) + 1));
		} else {
			timePeriod.setFinancialYear(Integer.parseInt(simpleDateFormat.format(date).toUpperCase()) - 1 + "-"
					+ simpleDateFormat.format(date).toUpperCase());
		}
		timePeriod.setYear(Integer.parseInt(simpleDateFormat.format(date).toUpperCase()));
		// timePeriodRepository.save(timePeriod);
		return "succes";
	}

	@Override
	public String saveDevelopmentPartners() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("Development partner typedetails/");
		String path = url.getPath().replaceAll("%20", " ");
		File[] files = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(files[f]);

				// save type
				XSSFSheet typeSheet = workbook.getSheetAt(0);
				for (int row = 1; row <= typeSheet.getLastRowNum(); row++) {
					int cols = 0;
					if (typeSheet.getRow(row) == null)
						break;

					XSSFRow xssfRow = typeSheet.getRow(row);

//					Cell cell = xssfRow.getCell(cols);
					Type type = new Type();
					type.setSlugId(typeRepository.findAll().size() + 1);
					type.setTypeName(xssfRow.getCell(cols).getStringCellValue());
					type.setDescription(xssfRow.getCell(++cols).getStringCellValue());
					
					typeRepository.save(type);

				}
//				typeDetailsRepository.findAll().size() + 1
//				typeRepository.findAll().size() + 1
				/*
				 * XSSFSheet typeSheet = workbook.getSheetAt(0); for (int row =
				 * 1; row <= typeSheet.getLastRowNum(); row++) { int cols = 0;
				 * if (typeSheet.getRow(row) == null) break;
				 * 
				 * XSSFRow xssfRow = typeSheet.getRow(row);
				 * 
				 * Cell cell = xssfRow.getCell(cols); Type type = new Type();
				 * type.setSlugId((int) cell.getNumericCellValue());
				 * type.setTypeName(xssfRow.getCell(++cols).getStringCellValue()
				 * ); type.setDescription(xssfRow.getCell(++cols).
				 * getStringCellValue());
				 * 
				 * typeRepository.save(type);
				 * 
				 * }
				 */
				XSSFSheet typeDetailSheet = workbook.getSheetAt(1);

				// save typedetails
				for (int row = 1; row <= typeDetailSheet.getLastRowNum(); row++) {
					int cols = 0;
					if (typeDetailSheet.getRow(row) == null)
						break;

					XSSFRow xssfRow = typeDetailSheet.getRow(row);
					TypeDetail typeDetail = new TypeDetail();
					typeDetail.setSlugId(typeDetailRepository.findAll().size() + 1);
					typeDetail.setName(xssfRow.getCell(cols).getStringCellValue());
					typeDetail.setOrderLevel((int) xssfRow.getCell(++cols).getNumericCellValue());
					typeDetail.setType(typeRepository.findByTypeName(xssfRow.getCell(++cols).getStringCellValue()));

					typeDetailRepository.save(typeDetail);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "done";
	}

	@Override
	public String importPartnerDetails() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("partnerTemp/");
		String path = url.getPath().replaceAll("%20", " ");
		File[] files = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}
		
		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet questionSheet = workbook.getSheetAt(0);
				List<PartnersDetails> partnersDetailsList = new ArrayList<>();

		for (int row = 1; row <= questionSheet.getLastRowNum(); row++) {// row
			if (questionSheet.getRow(row) == null)
				break;
			XSSFRow xssfRow = questionSheet.getRow(row);
			for (int cols = 0; cols < 19; cols++) {// column loop
				Cell cell = xssfRow.getCell(cols);
						switch (cols) {

						case 11:
							if (cell != null && (CellType.STRING == cell.getCellTypeEnum())) {
								String typeName = cell.getStringCellValue();
								if (typeName != null && !typeName.trim().isEmpty()) {

									Type type = typeRepository
											.findByTypeName(StringUtils.trimWhitespace(typeName));
									if (type == null) {
										type = new Type();
										type.setTypeName(StringUtils.trimWhitespace(typeName));
										type.setDescription(StringUtils.trimWhitespace(typeName));
										type.setSlugId(typeRepository.findAll().size() + 1);
										type = typeRepository.save(type);
									}
								}
							}
							break;
						case 12:
							// read type details
							if (cell != null && (CellType.STRING == cell.getCellTypeEnum())) {

								String typeDetails = StringUtils.trimWhitespace(cell.getStringCellValue());
								int order = 0;
								if (typeDetails != null && !typeDetails.trim().isEmpty() && !typeDetails.contains("@@PARENT@@")) {

									String typeDetailNames[] = (typeDetails.split(":")[1].split("@AND@"));
									Type type = typeRepository.findByTypeName(
											StringUtils.trimWhitespace(typeDetails.split(":")[0]));
								
									for (String typeDetailName : typeDetailNames) {
										TypeDetail typeDetail = typeDetailRepository.findByTypeAndName(type,
												StringUtils.trimWhitespace(typeDetailName).split("@@")[0].trim());
										if (typeDetail == null) {
											typeDetail = new TypeDetail();
											typeDetail.setName(StringUtils.trimWhitespace(typeDetailName));
											typeDetail.setType(type);
											typeDetail.setSlugId(typeDetailRepository.findAll().size() + 1);
											typeDetail.setOrderLevel(order++);
											if (typeDetail.getName().contains("@@score")) {
												typeDetail.setName(
														StringUtils.trimWhitespace(typeDetailName).split("@@")[0]
																.trim());
												String score = typeDetailName.split("=")[1];
												typeDetail.setScore(score);
											}
											typeDetailRepository.save(typeDetail);
//										Facility Level :	L1 @@PARENT@@=Facility Type:[Non 24x7 PHC @AND@ SC @AND@ Non-FRU CHC @AND@ 24x7 PHC] 
//											@AND@ L2 @@PARENT@@=Facility Type:[24x7 PHC @AND@ SDH @AND@ AH] @AND@ L3 @@PARENT@@=Facility Type:[MC @AND@ FRU CHC @AND@ AH]
										}

									}
								}							}
						}
			}
			
		}
		
	
		PartnersDetails partnersDetails =null;
		for (int row = 1; row <= questionSheet.getLastRowNum(); row++) {// row
													
			  System.out.println("Row :- "+row);// loop
			if (questionSheet.getRow(row) == null)
				break;
			// System.out.println("Rows::" + row);
			partnersDetails = new PartnersDetails();

			XSSFRow xssfRow = questionSheet.getRow(row);

			for (int cols = 0; cols < 16; cols++) {// column loop
  System.out.println(cols);
				Cell cell = xssfRow.getCell(cols);

				switch (cols) {

				case 0:
					if (cell == null)
						break;

					partnersDetails.setSlugId(partnersDetailsList.size() + 1);
					break;
				case 1:
					partnersDetails.setQuestionOrder((int) cell.getNumericCellValue());
					break;
				case 2:
					partnersDetails.setSection(cell.getStringCellValue());
					break;
				case 3:
					partnersDetails.setSubsection(cell.getStringCellValue());
					break;
				case 4:
					partnersDetails.setPartnersDetails(cell != null ? cell.getStringCellValue().trim() : "");
					break;
				case 5:
					partnersDetails.setColumnName(cell.getStringCellValue());
					break;
				case 6:
					partnersDetails.setControllerType(cell.getStringCellValue());
					break;
				case 7:
					partnersDetails.setFieldType(cell.getStringCellValue());
					break;
				case 8:
					partnersDetails.setPartern(cell != null ? cell.getStringCellValue().trim() : "");
					break;
				case 9:
					partnersDetails.setParentColumnName(cell != null ? cell.getStringCellValue().trim() : "");
					break;

				case 10:

					if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
						String featureName = cell.getStringCellValue();
						if (featureName != null && featureName.contains("fetch_tables")) {
							for (String feature : featureName.split("@AND")) {
								switch (feature.split(":")[0]) {
								case "fetch_tables": {
									partnersDetails.setTableName(feature.split(":")[1]);
								}
									break;

								}
							}
						}
						partnersDetails.setFeatures(StringUtils.trimWhitespace(cell.getStringCellValue()));
					}
					break;
					
				case 11:
					partnersDetails.setTypeId(null);
					if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
						if (cell.getStringCellValue() != null) {
							Type type = typeRepository.findByTypeName(cell.getStringCellValue().trim());
							partnersDetails.setTypeId(type);
						}
					}
					break;
				case 13:
					// constraints
					//
					if (cell != null ) {
						partnersDetails.setRequired(cell.getBooleanCellValue());
					}
					break;
					
				case 14:
					// constraints
					//
					if (cell != null ) {
						partnersDetails.setOptional(cell.getBooleanCellValue());
					}
					break;
					
				case 15:
					// constraints
					//
					if (cell != null ) {
						partnersDetails.setMaxLenth((int)cell.getNumericCellValue());
					}
					break;
			
				}
			}
			partnersDetailsList.add(partnersDetails);
		}
		
	
		
		
		
		partnersDetailsRepository.save(partnersDetailsList);
		
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "success";
	}

	@Override
	public PartnerOptionModel getPartnerDetails() {
		List<PartnersDetails> listOfPartnersDetails = partnersDetailsRepository.findAll();
		Map<String, Map<String, List<PartnerDetailsModel>>> sectionMap = new LinkedHashMap<String, Map<String, List<PartnerDetailsModel>>>();
		Map<String, List<PartnerDetailsModel>> subsectionMap = null;
		List<PartnerDetailsModel> listOfQuestionModel = null;
		PartnerDetailsModel partnerDetailsModel = null;
		
		Map<String, Map<String,List<Map<String, PartnerDetailsModel>>>> optionalSectionMap = new LinkedHashMap<>();
		Map<String,List<Map<String, PartnerDetailsModel>>> optionalSubSectionMapMap =  new LinkedHashMap<>();
		List<Map<String, PartnerDetailsModel>> listpartnerDetailsModelMap = new ArrayList<>();
		Map<String, PartnerDetailsModel> partnerDetailsModelMap = new LinkedHashMap<>();
		for (PartnersDetails partnersDetails : listOfPartnersDetails) {
			String fieldValue = null;
			Boolean isOthersSelected = false;
			String othersValue = null;
			String checkedValue = null;

			
			if(!partnersDetails.getOptional()) {

			if (sectionMap.containsKey(partnersDetails.getSection())) {
				subsectionMap = sectionMap.get(partnersDetails.getSection());

				if (sectionMap.get(partnersDetails.getSection()).containsKey(partnersDetails.getSubsection())) {
					listOfQuestionModel = subsectionMap.get(partnersDetails.getSubsection());
					partnerDetailsModel = setInQuestionModel(partnersDetails, partnerDetailsModel, fieldValue,
							isOthersSelected, othersValue, checkedValue);
					subsectionMap.get(partnersDetails.getSubsection()).add(partnerDetailsModel);
				} else {
					partnerDetailsModel = setInQuestionModel(partnersDetails, partnerDetailsModel, fieldValue,
							isOthersSelected, othersValue, checkedValue);

					listOfQuestionModel = new ArrayList<>();
					listOfQuestionModel.add(partnerDetailsModel);
					subsectionMap.put(partnersDetails.getSubsection(), listOfQuestionModel);
				}
			} else {
				subsectionMap = new LinkedHashMap<String, List<PartnerDetailsModel>>();
				listOfQuestionModel = new ArrayList<>();
				if (subsectionMap.containsKey(partnersDetails.getSubsection())) {

					partnerDetailsModel = setInQuestionModel(partnersDetails, partnerDetailsModel, fieldValue,
							isOthersSelected, othersValue, checkedValue);

					subsectionMap.get(partnersDetails.getSubsection()).add(partnerDetailsModel);
				} else {

					partnerDetailsModel = setInQuestionModel(partnersDetails, partnerDetailsModel, fieldValue,
							isOthersSelected, othersValue, checkedValue);

					listOfQuestionModel.add(partnerDetailsModel);

					subsectionMap.put(partnersDetails.getSubsection(), listOfQuestionModel);

				}
				sectionMap.put(partnersDetails.getSection(), subsectionMap);
			}
		}else {
			String areaLevel = "";
			if(partnersDetails.getFeatures()!=null) {
				String tableName = partnersDetails.getTableName().split("\\$\\$")[0].trim();
				if(tableName.equals("area"))
					areaLevel = partnersDetails.getTableName().split("\\$\\$")[1].trim().split("=")[1];
		}
			
			if(optionalSectionMap.containsKey(partnersDetails.getSection())) {
				//partnerDetailsModelMap = new LinkedHashMap<String, PartnerDetailsModel>();
				if(optionalSubSectionMapMap.containsKey(partnersDetails.getSubsection())) {
					partnerDetailsModel = setInQuestionModel(partnersDetails, partnerDetailsModel, fieldValue,
							isOthersSelected, othersValue, checkedValue);
					partnerDetailsModelMap.put(areaLevel,partnerDetailsModel);
					optionalSubSectionMapMap.put(partnersDetails.getSubsection(),Arrays.asList(partnerDetailsModelMap));
					optionalSectionMap.put(partnersDetails.getSection(), optionalSubSectionMapMap);
					
				}else {
					
				}
				
			}else {
				if(optionalSubSectionMapMap.containsKey(partnersDetails.getSubsection())) {
					
				}else {
					partnerDetailsModel = setInQuestionModel(partnersDetails, partnerDetailsModel, fieldValue,
							isOthersSelected, othersValue, checkedValue);
					partnerDetailsModelMap.put(areaLevel,partnerDetailsModel);
					optionalSubSectionMapMap.put(partnersDetails.getSubsection(),Arrays.asList(partnerDetailsModelMap));
					optionalSectionMap.put(partnersDetails.getSection(), optionalSubSectionMapMap);
					
				}
				
			}
			
			
			
		}

		}
		PartnerOptionModel partnerOptionModel = new PartnerOptionModel();
		partnerOptionModel.setPartnerDetailsModel(sectionMap);
		partnerOptionModel.setOptionalModel(optionalSectionMap);
		
		
		return partnerOptionModel;
	}

	private PartnerDetailsModel setInQuestionModel(PartnersDetails partnersDetails, PartnerDetailsModel partnerDetailsModel,
			String fieldValue, Boolean isOthersSelected, String othersValue, String checkedValue) {
		List<OptionModel> listOfOptionModel = new ArrayList<OptionModel>();
		partnerDetailsModel = new PartnerDetailsModel();
		partnerDetailsModel.setKey(partnersDetails.getSlugId());
		partnerDetailsModel.setLabel(partnersDetails.getPartnersDetails()!=null ? partnersDetails.getPartnersDetails():"");
		partnerDetailsModel.setType(partnersDetails.getFieldType());
		partnerDetailsModel.setControlType(partnersDetails.getControllerType());
		partnerDetailsModel.setColumnName(partnersDetails.getColumnName());
		partnerDetailsModel.setRequired(partnersDetails.getRequired());
		partnerDetailsModel.setParentColumnName(partnersDetails.getParentColumnName());
		partnerDetailsModel.setPattern(partnersDetails.getPartern());
		partnerDetailsModel.setMaxLength(partnersDetails.getMaxLenth());
		
		if(partnersDetails.getTypeId()!=null) {
			
			Type type = typeRepository.findByTypeName(partnersDetails.getTypeId().getTypeName());
			List<TypeDetail> listOfTypeDetails = typeDetailRepository.findByType(type);
			for (TypeDetail typeDetail : listOfTypeDetails) {
				OptionModel optionModel = new OptionModel();
				optionModel.setKey(typeDetail.getSlugId());
				optionModel.setValue(typeDetail.getName());
				optionModel.setOrder(typeDetail.getOrderLevel());
				listOfOptionModel.add(optionModel);
			}
			partnerDetailsModel.setOptions(listOfOptionModel);
			
		}

		if(partnersDetails.getFeatures()!=null) {
			String tableName = partnersDetails.getTableName().split("\\$\\$")[0].trim();
			String areaLevel = "";
			if(tableName.equals("area"))
				areaLevel = partnersDetails.getTableName().split("\\$\\$")[1].trim().split("=")[1];
			List<Area> areas = null;


			switch (tableName) {
			
			case "area":{
//				listOfOptions = new ArrayList<>();
				areas = areaRepository.findByAreaLevel(areaLevelRepository.findByAreaLevelName(areaLevel.toUpperCase()));
				if (areas != null) {

					int order = 0;
					for (Area area : areas) {
						Map<String, Object> extraKeyMap = new HashMap<>();
						OptionModel optionModel = new OptionModel();
						optionModel.setKey(area.getAreaId());
						optionModel.setValue(area.getAreaName());
						optionModel.setOrder(order++);
						optionModel.setParentId(area.getParentAreaId());
						optionModel.setLevel(area.getAreaLevel().getAreaLevelId());
						optionModel.setVisible(true);
						extraKeyMap.put("district_id", area.getDistrictId());
						extraKeyMap.put("block_id", area.getBlockId());
						
//							
						optionModel.setExtraKeyMap(extraKeyMap);
						listOfOptionModel.add(optionModel);
					}
					partnerDetailsModel.setOptions(listOfOptionModel);
				}
			
			}
			
			}

		}

		return partnerDetailsModel;

	}

	@Override
	@Transactional
	public String savePartner(ReceiveEventModel event) {
		Gson gson = new Gson();
		String message=null;
		List<Partner> listOfsavedPartner = partnerRepository.findAll(); 
		List<Partner> listOfPartner = partnerRepository.findByOrganizationName(event.getSubmissionData().get("organization_name").toString().toUpperCase().trim());
		if(!listOfPartner.isEmpty()) {
			message="Partner already registered.";
		} else {
			try {
				Partner dataSubmit = new Partner();
				dataSubmit.setCreatedDate(new Date());
				dataSubmit.setSlugId(listOfsavedPartner.size()+1);
				dataSubmit.setUpdatedDate(new Date());
				dataSubmit.setIsActive(true);
				dataSubmit.setIsApproved(true);
				event.getSubmissionData().put("organization_name",
						event.getSubmissionData().get("organization_name").toString().toUpperCase().trim());
				dataSubmit.setData(event.getSubmissionData());
				partnerRepository.save(dataSubmit);
				message = "Partner registered successfully.";

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//return new ResponseEntity<String>(message, HttpStatus.OK);
		return message;

	}

}
