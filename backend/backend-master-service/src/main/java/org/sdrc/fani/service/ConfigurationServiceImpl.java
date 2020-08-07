package org.sdrc.fani.service;

//import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.fani.collections.AiimsMasterData;
import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.AreaLevel;
import org.sdrc.fani.collections.CollectionAudit;
import org.sdrc.fani.collections.DesignationPartnerFormMapping;
import org.sdrc.fani.collections.EngineRole;
import org.sdrc.fani.collections.EnginesRoleFormMapping;
import org.sdrc.fani.repositories.AiimsMasterDataRepository;
import org.sdrc.fani.repositories.AreaLevelRepository;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.CollectionAuditRepository;
import org.sdrc.fani.repositories.CustomTypeDetailRepository;
import org.sdrc.fani.repositories.DesignationPartnerFormMappingRepository;
import org.sdrc.fani.repositories.EngineRoleFormMappingRepository;
import org.sdrc.fani.repositories.EnginesRoleRepoitory;
import org.sdrc.mongoclientdetails.MongoClientDetails;
import org.sdrc.mongoclientdetails.repository.MongoClientDetailsRepository;
import org.sdrc.usermgmt.mongodb.domain.Authority;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.domain.DesignationAuthorityMapping;
import org.sdrc.usermgmt.mongodb.repository.AuthorityRepository;
import org.sdrc.usermgmt.mongodb.repository.DesignationAuthorityMappingRepository;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.models.AccessType;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeRepository;
import in.co.sdrc.sdrcdatacollector.util.Status;


/**
 * @author Debiprasad Parida (debiprasad@sdrc.co.in)
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private MongoClientDetailsRepository mongoClientDetailsRepository;

	@Autowired
	AreaLevelRepository areaLevelRepository;

	@Autowired
	AreaRepository areaRepository;

	@Autowired
	AuthorityRepository authorityRepository;

	@Autowired
	DesignationAuthorityMappingRepository designationAuthorityMappingRepository;

	@Autowired
	DesignationRepository designationRepository;
	
	@Autowired
	private TypeDetailRepository typeDetailsRepository;
	
	@Autowired
	private TypeRepository typeRepository;
	
	@Autowired
	private CollectionAuditRepository collectionAuditRepository;

//	@Autowired
//	private IndicatorQuestionMappingRepository indicatorQuestionMappingRepository;
	
	@Autowired
	private CustomTypeDetailRepository customTypeDetailRepository;
	
	@Autowired
	private EngineFormRepository formRepository;
	
	
	@Autowired
	private EnginesRoleRepoitory roleRepository;
	
	@Autowired
	private EngineRoleFormMappingRepository roleFormMappingRepository;
	
	@Autowired
	private DesignationPartnerFormMappingRepository designationPartnerFormMappingRepository;
	
	@Autowired
	private AiimsMasterDataRepository aiimsMasterDataRepository;
	
	@Autowired
	private EngineFormRepository engineFormRepository;

	@Override
	public ResponseEntity<String> importAreas() {

		AreaLevel areaLevel = new AreaLevel();
		/*areaLevel.setAreaLevelId(1);
		areaLevel.setAreaLevelName("NATIONAL");

		areaLevelRepository.save(areaLevel);*/

		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(1);
		areaLevel.setAreaLevelName("STATE");

		areaLevelRepository.save(areaLevel);

		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(2);
		areaLevel.setAreaLevelName("DISTRICT");

		areaLevelRepository.save(areaLevel);
		
		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(3);
		areaLevel.setAreaLevelName("TAHASIL");

		areaLevelRepository.save(areaLevel);

		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(4);
		areaLevel.setAreaLevelName("BLOCK");

		areaLevelRepository.save(areaLevel);
		
		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(5);
		areaLevel.setAreaLevelName("ULB");

		areaLevelRepository.save(areaLevel);
		
		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(6);
		areaLevel.setAreaLevelName("VILLAGE");

		areaLevelRepository.save(areaLevel);
		
		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(7);
		areaLevel.setAreaLevelName("SHELTER HOME");

		areaLevelRepository.save(areaLevel);
		areaLevel = new AreaLevel();
		areaLevel.setAreaLevelId(8);
		areaLevel.setAreaLevelName("TRANSIT POINT");

		areaLevelRepository.save(areaLevel);

		
		Map<String, List<TypeDetail>> formTypeDetails = new HashMap<>();
	
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("area/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				System.out.println("START");
				workbook = new XSSFWorkbook(files[f]);
				System.out.println("END");
			} catch (InvalidFormatException | IOException e) {

				e.printStackTrace();
			}
			XSSFSheet areaSheet = workbook.getSheetAt(0);

			
			
			Area area = null;
			Map<String, Object> formIdTypeDeatils = new HashMap<>();
			
			for (int row = 1; row <= areaSheet.getLastRowNum(); row++) {// row
																		// loop
				
				Integer id = null;
				String areaCode = null;
				String areaName = null;
				Double parentAreaId = null;
				Integer areaLevelId = null;
				
				 System.out.println("Rows::" + row);

				area = new Area();

				XSSFRow xssfRow = areaSheet.getRow(row);

				for (int cols = 0; cols < areaSheet.getRow(row).getLastCellNum(); cols++) {// column loop

					Cell cell = xssfRow.getCell(cols);

					switch (cols) {

					case 0:
						id = (int) cell.getNumericCellValue();
						break;

					case 1:
						if(cell != null && cell.getCellTypeEnum()!=CellType.BLANK ) {
						if(CellType.STRING == cell.getCellTypeEnum())
							areaCode = cell.getStringCellValue();
						else
//							areaCode= String.valueOf(cell.getNumericCellValue());
							areaCode=(int)cell.getNumericCellValue()+"";
						}
						break;

					case 2:
						areaName = cell.getStringCellValue();
						break;

					case 3:
						parentAreaId = cell.getNumericCellValue();
						break;

					case 4:
						areaLevelId = (int) cell.getNumericCellValue();

						Area parentArea = null;
						if (areaLevelId >= 2) {
							parentArea = areaRepository.findByAreaId(parentAreaId.intValue());
						}

						switch (areaLevelId) {

						case 2:
							// district
							area.setStateId(parentArea.getAreaId());
							break;
							
						case 3:
							// tahasil
							area.setDistrictId(parentArea.getAreaId());
							area.setStateId(areaRepository.findByAreaId(parentArea.getAreaId()).getParentAreaId());
							
							break;
						case 4:
							// Block
							area.setTahasilId(parentArea.getAreaId());
							area.setDistrictId(areaRepository.findByAreaId(parentArea.getAreaId()).getParentAreaId());
							area.setStateId(areaRepository.findByAreaId(area.getDistrictId()).getParentAreaId());
							break;
							
						case 5:
							// ulb
							area.setDistrictId(parentArea.getAreaId());
							area.setStateId(areaRepository.findByAreaId(parentArea.getAreaId()).getParentAreaId());
							break;
							
						case 6:
							//village
							area.setTahasilId(parentArea.getAreaId());
							area.setDistrictId(areaRepository.findByAreaId(parentArea.getAreaId()).getParentAreaId());
							area.setStateId(areaRepository.findByAreaId(area.getDistrictId()).getParentAreaId());
							break;
							
						case 7:
							//selterHome
							area.setDistrictId(parentArea.getAreaId());
							area.setStateId(areaRepository.findByAreaId(parentArea.getAreaId()).getParentAreaId());
							break;
							
						case 8:
							//transint point
							area.setDistrictId(parentArea.getAreaId());
							area.setStateId(areaRepository.findByAreaId(parentArea.getAreaId()).getParentAreaId());
							break;
							
					
	
						}
						break;
					}

				}
				area.setAreaId(id);
				area.setAreaCode(areaCode);
				area.setAreaName(areaName);
				
				if(areaLevelId!=null){
					area.setAreaLevel(areaLevelRepository.findByAreaLevelId(areaLevelId));
					area.setParentAreaId((parentAreaId != null && parentAreaId!=0.0 && parentAreaId!=-1.0 && parentAreaId!=-2.0)  ? areaRepository.findByAreaId(parentAreaId.intValue()).getAreaId() : -1);
				}else{
					area.setParentAreaId(-1);
				}
				
				area.setLive(true);
				areaRepository.save(area);
			}
		}
		CollectionAudit collectionAudit = collectionAuditRepository.findByCollectionName("area");
		
		collectionAudit.setUpdateDate(new Date());
		collectionAuditRepository.save(collectionAudit);

		return new ResponseEntity<>("succsess", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> formsValue() {

		return null;
	}

	@Override
	public String createMongoOauth2Client() {

		try {

			MongoClientDetails mongoClientDetails = new MongoClientDetails();

			HashSet<String> scopeSet = new HashSet<>();
			scopeSet.add("read");
			scopeSet.add("write");

			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("dashboard"));

			Set<String> authorizedGrantTypes = new HashSet<>();
			authorizedGrantTypes.add("refresh_token");
			authorizedGrantTypes.add("client_credentials");
			authorizedGrantTypes.add("password");

			Set<String> resourceIds = new HashSet<>();
			resourceIds.add("web-service");

			mongoClientDetails.setClientId("rmncha");
			mongoClientDetails.setClientSecret("rmncha@123#!");
			mongoClientDetails.setScope(scopeSet);
			mongoClientDetails.setAccessTokenValiditySeconds(30000);
			mongoClientDetails.setRefreshTokenValiditySeconds(40000);
			mongoClientDetails.setAuthorities(authorities);
			mongoClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);
			mongoClientDetails.setResourceIds(resourceIds);

			mongoClientDetailsRepository.save(mongoClientDetails);
			return "success";

		} catch (Exception e) {

			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<String> config() {
		// create designation
		List<Designation> designationList = new ArrayList<>();

		Designation desg = new Designation();
		desg.setCode("ADMIN");
		desg.setName("ADMIN");
		desg.setSlugId(1);
		designationList.add(desg);
		
		desg = new Designation();
		desg.setCode("STATE LEVEL");
		desg.setName("STATE LEVEL");
		desg.setSlugId(2);
		designationList.add(desg);

		desg = new Designation();
		desg.setCode("DISTRICT LEVEL");
		desg.setName("DISTRICT LEVEL");
		desg.setSlugId(3);
		designationList.add(desg);
		
		desg = new Designation();
		desg.setCode("VOLUNTEER");
		desg.setName("VOLUNTEER");
		desg.setSlugId(4);
		designationList.add(desg);
		
		desg = new Designation();
		desg.setCode("API_ACCESS");
		desg.setName("API ACCESS");
		desg.setSlugId(5);
		designationList.add(desg);
		
		//designationRepository.save(designationList);

		// create Authority

		List<Authority> authorityList = new ArrayList<>();

		Authority authority = new Authority();
		authority.setAuthority("USER_MGMT_ALL_API");
		authority.setDescription("Allow user to manage usermanagement module");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("CREATE_USER");
		authority.setDescription("Allow user to access createuser API");
		authorityList.add(authority);
		
		authority = new Authority();
		authority.setAuthority("CHANGE_PASSWORD");
		authority.setDescription("Allow user to access changepassword API");
		authorityList.add(authority);
		
		authority = new Authority();
		authority.setAuthority("UPDATE_USER");
		authority.setDescription("Allow user to access updateuser API");
		authorityList.add(authority);
		
		authority = new Authority();
		authority.setAuthority("ENABLE_DISABLE_USER");
		authority.setDescription("Allow user to access enable/disable user API");
		authorityList.add(authority);
		
		authority = new Authority();
		authority.setAuthority("RESET_PASSWORD");
		authority.setDescription("Allow user to access resetpassword API");
		authorityList.add(authority);
		
		authority = new Authority();
		authority.setAuthority("dataentry_HAVING_write");
		authority.setDescription("Allow user to  dataentry module");
		authorityList.add(authority);
		
		authority = new Authority();
		authority.setAuthority("DOWNLOAD_RAWDATA_REPORT");
		authority.setDescription("Allow user to  download raw data report");
		authorityList.add(authority);

		
		authority = new Authority();
		authority.setAuthority("CMS_Admin");
		authority.setDescription("Allow user to access ADMIN CMS");
		authorityList.add(authority);

		authority = new Authority();
		authority.setAuthority("Edit_Submission");
		authority.setDescription("Allow user to edit the submitted data");
		authorityList.add(authority);
		
		
		authority = new Authority();
		authority.setAuthority("API_ACCESS");
		authority.setDescription("Allow user to access API");
		authorityList.add(authority);
		
		//authorityRepository.save(authorityList);

		// Designation-Authority Mapping

		List<DesignationAuthorityMapping> damList = new ArrayList<>();

		DesignationAuthorityMapping dam = new DesignationAuthorityMapping();

		/**
		 * admin
		 */
		dam.setAuthority(authorityRepository.findByAuthority("USER_MGMT_ALL_API"));
		dam.setDesignation(designationRepository.findByCode("ADMIN"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("ADMIN"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CMS_Admin"));
		dam.setDesignation(designationRepository.findByCode("ADMIN"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("dataentry_HAVING_write"));
		dam.setDesignation(designationRepository.findByCode("ADMIN"));
		damList.add(dam);
		
		/**
		 * partner admin
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("STATE LEVEL"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CMS_Admin"));
		dam.setDesignation(designationRepository.findByCode("STATE LEVEL"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("dataentry_HAVING_write"));
		dam.setDesignation(designationRepository.findByCode("STATE LEVEL"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("STATE LEVEL"));
		damList.add(dam);
		/**
		 * District Level
		 */
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("DISTRICT LEVEL"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CMS_Admin"));
		dam.setDesignation(designationRepository.findByCode("DISTRICT LEVEL"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("dataentry_HAVING_write"));
		dam.setDesignation(designationRepository.findByCode("DISTRICT LEVEL"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("DOWNLOAD_RAWDATA_REPORT"));
		dam.setDesignation(designationRepository.findByCode("DISTRICT LEVEL"));
		damList.add(dam);
		
		
//		Edit_Submission
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("Edit_Submission"));
		dam.setDesignation(designationRepository.findByCode("DISTRICT LEVEL"));
		damList.add(dam);
		/**
		 * VOLUNTEER
		 */
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("CHANGE_PASSWORD"));
		dam.setDesignation(designationRepository.findByCode("VOLUNTEER"));
		damList.add(dam);
		
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("dataentry_HAVING_write"));
		dam.setDesignation(designationRepository.findByCode("VOLUNTEER"));
		damList.add(dam);
		
		//api access
		dam = new DesignationAuthorityMapping();
		dam.setAuthority(authorityRepository.findByAuthority("API_ACCESS"));
		dam.setDesignation(designationRepository.findByCode("API_ACCESS"));
		damList.add(dam);
		
		
		designationAuthorityMappingRepository.save(damList);
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> importFilterExpInTypeDetail() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("templates/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {
			System.out.println(files[f].toString());
		}
		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(files[f]);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			XSSFSheet choicesSheet = workbook.getSheet("Choices");

			for (int row = 1; row <= choicesSheet.getLastRowNum(); row++) {
				if (choicesSheet.getRow(row) == null)
					break;

				XSSFRow xssfRow = choicesSheet.getRow(row);
				int formId = 0; // initialize with outer loop when looping
								// multiple forms
				String pTypeName = "";
				String pTypeDetailName = "";
				for (int cols = 0; cols < 4; cols++) {// column loop
					Cell cell = xssfRow.getCell(cols);
					switch (cols) {
					case 0:
						if (cell == null)
							break;
						if (cell != null && (CellType.STRING == cell.getCellTypeEnum())) {
							formId = Integer.valueOf(cell.getStringCellValue());
						} else if (cell != null && (CellType.NUMERIC == cell.getCellTypeEnum())) {
							formId = (int) cell.getNumericCellValue();
						}
						break;
					case 1:
						if (cell == null)
							break;
						if (cell != null && (CellType.NUMERIC == cell.getCellTypeEnum())) {
							pTypeName = cell.getStringCellValue();
						}
						break;

					case 2:
						if (cell == null)
							break;
						if (cell != null && (CellType.NUMERIC == cell.getCellTypeEnum())) {
							pTypeDetailName = cell.getStringCellValue();
						}
						break;

					case 3:
						if (cell == null)
							break;
						if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
							String expressionArray[] = cell.getStringCellValue().split("\\&");
							String modifiedExp = "";

							for (String expression : expressionArray) {
								if (expression.contains("selected")) { // set typedetail id in case of dropdown only

									for (int i = 0; i < expression.split("").length; i++) {

										char ch = expression.charAt(i);

										if (ch == '}') {
											String typeTypeDetails = "";
											for (int j = i + 2; j < expression.split("").length; j++) {

												char chNext = expression.charAt(j);
												if (chNext == ')') {
													break;
												}

												typeTypeDetails = typeTypeDetails + chNext;
											}

											String typeName = typeTypeDetails.split(":")[0];
											String typeDetailsName = typeTypeDetails.split(":")[1];

											TypeDetail td = typeDetailsRepository.findByTypeAndNameAndFormId(
													typeRepository.findByTypeNameAndFormId(
															StringUtils.trimWhitespace(typeName), formId),
													StringUtils.trimWhitespace(typeDetailsName), formId);

											String newExp = expression.replace(typeTypeDetails,
													td.getSlugId().toString());
											modifiedExp = modifiedExp + newExp + " | ";
										}
									}

								} else {
									modifiedExp = modifiedExp + expression + " | ";

								}
							}

							modifiedExp = modifiedExp.substring(0, modifiedExp.length() - 3);
							TypeDetail parentTd = typeDetailsRepository
									.findByTypeAndNameAndFormId(
											typeRepository.findByTypeNameAndFormId(
													StringUtils.trimWhitespace(pTypeName), formId),
											StringUtils.trimWhitespace(pTypeDetailName), formId);
							parentTd.setFilterByExp(modifiedExp);
							typeDetailsRepository.save(parentTd);
						}

					}
				}
			}
		}
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}
	
	@Override
	@Transactional
	public ResponseEntity<String> persistIndicatorMapping(){
		
		/*ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("aggregation/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {
			System.out.println(files[f].toString());
		}
		
		List<IndicatorQuestionMapping> indicatorQuestionMappings = new ArrayList<>();
		IndicatorQuestionMapping indicatorQuestionMapping = null;
		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(files[f]);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			XSSFSheet choicesSheet = workbook.getSheetAt(0);

			for (int row = 1; row <= choicesSheet.getLastRowNum(); row++) {
				if (choicesSheet.getRow(row) == null)
					break;

				indicatorQuestionMapping = new IndicatorQuestionMapping();
				XSSFRow xssfRow = choicesSheet.getRow(row);
				int cols = 0;
//				for (int cols = 0; cols <= 14; cols++) {// column loop
					Cell cell = xssfRow.getCell(cols);
					indicatorQuestionMapping.setIndicatorNid((int) cell.getNumericCellValue());
					
					cell = xssfRow.getCell(++cols);
					indicatorQuestionMapping.setIndicatorName(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum()))
						indicatorQuestionMapping.setParentColumn(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					indicatorQuestionMapping.setUnit(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					indicatorQuestionMapping.setSubgroup(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum()))
						indicatorQuestionMapping.setNumerator(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum()))
						indicatorQuestionMapping.setDenominator(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum()))
						indicatorQuestionMapping.setAggregationType(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum()))
						indicatorQuestionMapping.setParentType(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					indicatorQuestionMapping.setFormId((int) cell.getNumericCellValue());
					
					cell = xssfRow.getCell(++cols);
					indicatorQuestionMapping.setPeriodicity(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum()))
						indicatorQuestionMapping.setFieldType(cell.getStringCellValue());
					
					cell = xssfRow.getCell(++cols);
					
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum() && !cell.getStringCellValue().equals("NULL")) )
					indicatorQuestionMapping.setHighIsGood(cell.getStringCellValue().equalsIgnoreCase("true") 
							? true : false );
					
					cell = xssfRow.getCell(++cols);
					if(cell != null && (CellType.STRING == cell.getCellTypeEnum()))
						indicatorQuestionMapping.setTypeDetailId((int) cell.getNumericCellValue());
					
					cell = xssfRow.getCell(++cols);
					indicatorQuestionMapping.setArea(cell.getStringCellValue());
					
//				}
				
				indicatorQuestionMappings.add(indicatorQuestionMapping);
			}
			
			indicatorQuestionMappingRepository.save(indicatorQuestionMappings);
		}*/
		
		
		
		return new ResponseEntity<String>("success", HttpStatus.OK);
		
	}
	
	@Override
	public Boolean configureRoleFormMapping() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("templates/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet sheet = workbook.getSheetAt(1);

				for (int row = 1; row <= sheet.getLastRowNum(); row++) {// row
																		// loop

					if (sheet.getRow(row) == null)
						break;
					// System.out.println("Rows::" + row);

					XSSFRow xssfRow = sheet.getRow(row);
					String formName = null;
					String roleCode = null;
					Integer roleId = null;
					Integer formId = null;
					String active = null;

					for (int cols = 0; cols < 4; cols++) {
						// column loop
						Cell cell = xssfRow.getCell(cols);

						switch (cols) {

						case 0:// form
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "1:Community Facilitator Input Form"

								formId = Integer
										.valueOf(StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[0]));
								formName = StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[1]);
								active = StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[2]);

								/**
								 * Check the status while uploading form: if
								 * found active- check whether the same form is
								 * present, if not fount insert. if found
								 * inactive - check whether the form exist, if
								 * exist set its status to inactive and update
								 * in db
								 */
								if (active != null) {

									if (active.equals("active")) {

										EnginesForm engineForm = formRepository.findByNameAndFormIdAndStatus(
												StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[1]),
												Integer.valueOf(StringUtils
														.trimWhitespace(cell.getStringCellValue().split(":")[0])),
												Status.ACTIVE);

										if (engineForm == null) {

											EnginesForm form = new EnginesForm();
											form.setFormId(formId);
											form.setName(formName);
											formRepository.save(form);

										}

									} else if (active.equals("inactive")) {

										EnginesForm engineForm = formRepository.findByNameAndFormId(
												StringUtils.trimWhitespace(cell.getStringCellValue().split(":")[1]),
												Integer.valueOf(StringUtils
														.trimWhitespace(cell.getStringCellValue().split(":")[0])));

										if (engineForm != null) {
											engineForm.setStatus(Status.INACTIVE);
											formRepository.save(engineForm);
										}

									} else {
										throw new RuntimeException("Invalid active value while uploading FORM-NAME");
									}
								} else {
									throw new RuntimeException(
											"active or inactive status is not found while uploading sheet");
								}

							}
							break;

						case 1:// role-DATAENTRY
							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "'FACILITATOR:002:2','ROLENAME:ROLECODE:ROLEID'"

								List<String> roleDataEntry = Arrays.asList((cell.getStringCellValue().split(",")));
								for (String rdt : roleDataEntry) {

									// insert in role document
									roleCode = StringUtils.trimWhitespace(rdt.split(":")[1]);
									roleId = Integer.valueOf(StringUtils.trimWhitespace(rdt.split(":")[2]));
									active = StringUtils.trimWhitespace(rdt.split(":")[3]);

									if (roleRepository
											.findByRoleCode(StringUtils.trimWhitespace(rdt.split(":")[1])) == null) {

										EngineRole role = new EngineRole();
										role.setRoleId(roleId);
										role.setRoleCode(roleCode);
										role.setRoleName(StringUtils.trimWhitespace(rdt.split(":")[0]));
										roleRepository.save(role);
									}

									EnginesRoleFormMapping enginesRoleFormMapping = null;

									/**
									 * Check the status while uploading form: if
									 * found active- check whether the same form
									 * is present, if not fount insert. if found
									 * inactive - check whether the form exist,
									 * if exist set its status to inactive and
									 * update in db
									 */
									if (active != null) {
										if (active.equals("active")) {
											enginesRoleFormMapping = roleFormMappingRepository
													.findByRoleRoleIdAndFormFormIdAndAccessTypeAndStatus(roleId, formId,
															AccessType.DATA_ENTRY, Status.ACTIVE);

											if (enginesRoleFormMapping == null) {
												EnginesRoleFormMapping rfMapping = new EnginesRoleFormMapping();
												rfMapping.setForm(formRepository.findByName(formName));
												rfMapping.setRole(roleRepository.findByRoleCode(roleCode));
												rfMapping.setAccessType(AccessType.DATA_ENTRY);
												rfMapping.setRoleFormMappingId(
														roleFormMappingRepository.findAll().size() + 1);
												roleFormMappingRepository.save(rfMapping);
											}

										} else if (active.equals("inactive")) {
											enginesRoleFormMapping = roleFormMappingRepository
													.findByRoleRoleIdAndFormFormIdAndAccessType(roleId, formId,
															AccessType.DATA_ENTRY);
											if (enginesRoleFormMapping != null) {
												enginesRoleFormMapping.setStatus(Status.INACTIVE);
												roleFormMappingRepository.save(enginesRoleFormMapping);
											}
										} else {
											throw new RuntimeException(
													"Invalid active value while uploading ENGINEROLE(ROLENAME:ROLECODE:ROLEID)-DATAENTRY");
										}
									} else {

										throw new RuntimeException(
												"active or inactive status is not found while uploading sheet");
									}

								}

							}
							break;

						case 2:// role-REVIEW

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "'FACILITATOR:002:2','ROLENAME:ROLECODE:ROLEID'"

								List<String> roleDataEntry = Arrays.asList((cell.getStringCellValue().split(",")));
								for (String rdt : roleDataEntry) {

									// insert in role document
									roleCode = StringUtils.trimWhitespace(rdt.split(":")[1]);
									roleId = Integer.valueOf(StringUtils.trimWhitespace(rdt.split(":")[2]));
									active = StringUtils.trimWhitespace(rdt.split(":")[3]);

									if (roleRepository
											.findByRoleCode(StringUtils.trimWhitespace(rdt.split(":")[1])) == null) {

										EngineRole role = new EngineRole();
										role.setRoleId(roleId);
										role.setRoleCode(roleCode);
										role.setRoleName(StringUtils.trimWhitespace(rdt.split(":")[0]));
										roleRepository.save(role);
									}

									EnginesRoleFormMapping enginesRoleFormMapping = null;

									/**
									 * Check the status while uploading form: if
									 * found active- check whether the same form
									 * is present, if not fount insert. if found
									 * inactive - check whether the form exist,
									 * if exist set its status to inactive and
									 * update in db
									 */
									if (active != null) {
										if (active.equals("active")) {
											enginesRoleFormMapping = roleFormMappingRepository
													.findByRoleRoleIdAndFormFormIdAndAccessTypeAndStatus(roleId, formId,
															AccessType.REVIEW, Status.ACTIVE);

											if (enginesRoleFormMapping == null) {
												EnginesRoleFormMapping rfMapping = new EnginesRoleFormMapping();
												rfMapping.setForm(formRepository.findByName(formName));
												rfMapping.setRole(roleRepository.findByRoleCode(roleCode));
												rfMapping.setAccessType(AccessType.REVIEW);
												rfMapping.setRoleFormMappingId(
														roleFormMappingRepository.findAll().size() + 1);
												roleFormMappingRepository.save(rfMapping);
											}

										} else if (active.equals("inactive")) {
											enginesRoleFormMapping = roleFormMappingRepository
													.findByRoleRoleIdAndFormFormIdAndAccessType(roleId, formId,
															AccessType.REVIEW);
											if (enginesRoleFormMapping != null) {
												enginesRoleFormMapping.setStatus(Status.INACTIVE);
												roleFormMappingRepository.save(enginesRoleFormMapping);
											}
										} else {
											throw new RuntimeException(
													"Invalid active value while uploading ENGINEROLE(ROLENAME:ROLECODE:ROLEID)-DATAENTRY");
										}
									} else {

										throw new RuntimeException(
												"active or inactive status is not found while uploading sheet");
									}

								}

							}
							break;
						case 3:// role-RAWDATA-REPORT

							if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {

								// "'FACILITATOR:002:2','ROLENAME:ROLECODE:ROLEID'"

								List<String> roleDataEntry = Arrays.asList((cell.getStringCellValue().split(",")));
								for (String rdt : roleDataEntry) {

									// insert in role document
									roleCode = StringUtils.trimWhitespace(rdt.split(":")[1]);
									roleId = Integer.valueOf(StringUtils.trimWhitespace(rdt.split(":")[2]));
									active = StringUtils.trimWhitespace(rdt.split(":")[3]);

									if (roleRepository
											.findByRoleCode(StringUtils.trimWhitespace(rdt.split(":")[1])) == null) {

										EngineRole role = new EngineRole();
										role.setRoleId(roleId);
										role.setRoleCode(roleCode);
										role.setRoleName(StringUtils.trimWhitespace(rdt.split(":")[0]));
										roleRepository.save(role);
									}

									EnginesRoleFormMapping enginesRoleFormMapping = null;

									/**
									 * Check the status while uploading form: if
									 * found active- check whether the same form
									 * is present, if not fount insert. if found
									 * inactive - check whether the form exist,
									 * if exist set its status to inactive and
									 * update in db
									 */
									if (active != null) {
										if (active.equals("active")) {
											enginesRoleFormMapping = roleFormMappingRepository
													.findByRoleRoleIdAndFormFormIdAndAccessTypeAndStatus(roleId, formId,
															AccessType.DOWNLOAD_RAW_DATA, Status.ACTIVE);

											if (enginesRoleFormMapping == null) {
												EnginesRoleFormMapping rfMapping = new EnginesRoleFormMapping();
												rfMapping.setForm(formRepository.findByName(formName));
												rfMapping.setRole(roleRepository.findByRoleCode(roleCode));
												rfMapping.setAccessType(AccessType.DOWNLOAD_RAW_DATA);
												rfMapping.setRoleFormMappingId(
														roleFormMappingRepository.findAll().size() + 1);
												roleFormMappingRepository.save(rfMapping);
											}

										} else if (active.equals("inactive")) {
											enginesRoleFormMapping = roleFormMappingRepository
													.findByRoleRoleIdAndFormFormIdAndAccessType(roleId, formId,
															AccessType.DOWNLOAD_RAW_DATA);
											if (enginesRoleFormMapping != null) {
												enginesRoleFormMapping.setStatus(Status.INACTIVE);
												roleFormMappingRepository.save(enginesRoleFormMapping);
											}
										} else {
											throw new RuntimeException(
													"Invalid active value while uploading ENGINEROLE(ROLENAME:ROLECODE:ROLEID)-DATAENTRY");
										}
									} else {

										throw new RuntimeException(
												"active or inactive status is not found while uploading sheet");
									}

								}

							}
							break;

						}
					}

					workbook.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		return true;
	}

	@Override
	public ResponseEntity<String> importDesgPartnerFormMapping() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("desg/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		List<DesignationPartnerFormMapping> dpfmList = new ArrayList<>();
		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet sheet = workbook.getSheetAt(0);

				for (int row = 1; row <= sheet.getLastRowNum(); row++) {// row
																		// loop

					if (sheet.getRow(row) == null)
						break;
					// System.out.println("Rows::" + row);

					XSSFRow xssfRow = sheet.getRow(row);
					Integer id=null;
					String desgCode=null;
					List<Integer> formIds = null;
					String partnerId=null;
					String accessType=null;
					
					for (int cols = 0; cols < 5; cols++) {
						// column loop
						Cell cell = xssfRow.getCell(cols);

						switch (cols) {
							
							case 0:{
								
								id=(int) cell.getNumericCellValue();
							}
								
								break;
							
							case 1:{
								desgCode = cell.getStringCellValue();
							}
								break;
								
							case 2:{
								if (cell != null && CellType.STRING == cell.getCellTypeEnum()) {
									String commaSeperatedFormIds = cell.getStringCellValue();
									formIds=Stream.of(commaSeperatedFormIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
								}else{
									int formId = (int) cell.getNumericCellValue();
									formIds=Arrays.asList(formId);
								}
							}
								break;
								
							case 3:
								if (cell == null) {
									partnerId=null;
								}else
									partnerId=cell.getStringCellValue();
								break;
								
							case 4:
								accessType=cell.getStringCellValue();
								break;
						
						}
					}
					
					DesignationPartnerFormMapping mapping = new DesignationPartnerFormMapping();
					
					if(accessType.equals("DATA_ENTRY")){
						mapping.setAccessType(AccessType.DATA_ENTRY);
					}
					
					if(accessType.equals("DOWNLOAD_RAW_DATA")){
						mapping.setAccessType(AccessType.DOWNLOAD_RAW_DATA);
					}
					
					if(accessType.equals("REVIEW")){
						mapping.setAccessType(AccessType.REVIEW);
					}
					mapping.setDesgPartnerFormMappingId(id);
					mapping.setCreatedDate(new Date());
					mapping.setDesignation(designationRepository.findByCode(desgCode));
					mapping.setFormId(formIds);
					mapping.setPartnerId(partnerId!=null?partnerId:null);
					mapping.setStatus(Status.ACTIVE);
					
					dpfmList.add(mapping);
				}
		
				workbook.close();
				
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		
		designationPartnerFormMappingRepository.save(dpfmList);
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> importForms() {
		
		
		List<EnginesForm> enginesForms = formRepository.findAll();
		List<EnginesForm> listOfEnginesForms = new ArrayList<>();
		for (EnginesForm enginesForm : enginesForms) {
			EnginesForm enginesForm2 = new EnginesForm();
			BeanUtils.copyProperties(enginesForm, enginesForm2);
			
			enginesForm2.setCreatedDate(new Date());
			enginesForm2.setUpdatedDate(new Date());
			listOfEnginesForms.add(enginesForm2);
		}
		formRepository.save(listOfEnginesForms);
		
		
		/*

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("engineforms/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		List<EnginesForm> formList = new ArrayList<>();
		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet sheet = workbook.getSheetAt(0);

				for (int row = 1; row <= sheet.getLastRowNum(); row++) {// row
																		// loop
					if (sheet.getRow(row) == null)
						break;
					System.out.println("rowNumber"+row);
					XSSFRow xssfRow = sheet.getRow(row);
					if(xssfRow.getCell(0) != null && xssfRow.getCell(1) != null) {
						if(formRepository.findByFormId((int) xssfRow.getCell(0).getNumericCellValue())==null) {
							
							
						}
						
					}
					
					Integer id=null;
					String name=null;
					String status=null;
					
					for (int cols = 0; cols <2; cols++) {
						// column loop
						Cell cell = xssfRow.getCell(cols);

						switch (cols) {
							
							case 0:{
								id=(int) cell.getNumericCellValue();
							}
							break;
							
							case 1:{
								name=cell.getStringCellValue();
							}
							break;
							case 2:{
								status=cell.getStringCellValue();
							}
							break;
								
						}
					}
					
					///insert
					EnginesForm form = new EnginesForm();
					form.setFormId(id);
					form.setName(name);
					if(status!=null)
					form.setStatus(Status.ACTIVE);
					form.setVersion(0);
					form.setCreatedDate(new Date());
					form.setUpdatedDate(new Date());
					formList.add(form);
				}
		
				workbook.close();
				
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		
		formRepository.save(formList);*/
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> setUpdatedDate(String ids) {
	    List<Integer> formLists=new ArrayList<Integer>();
	    formLists= Arrays.asList(ids.split(","))
	    .stream().map(Integer::valueOf).collect(Collectors.toList());
	    formLists.forEach( s-> System.out.println(s));
	    String operationStatus="successfully changed updatedDate for formIds"+ids;
	    
	    for(Integer i:formLists) {
	    	EnginesForm form = formRepository.findByFormId(i);
	    	if(form!=null) {
	    	form.setUpdatedDate(new Date());
	    	formRepository.save(form);
	    	}
	    	else {
	    		operationStatus="No Forms found with form Id"+ids; 
	    	}
	    }
		return new ResponseEntity<String>(operationStatus,HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> importAiimsData() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("AiimsMasterData/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		List<EnginesForm> formList = new ArrayList<>();
		for (int f = 0; f < files.length; f++) {

			XSSFWorkbook workbook = null;

			try {
				workbook = new XSSFWorkbook(files[f]);
				XSSFSheet sheet = workbook.getSheetAt(0);
				int siNo=0;
				List<AiimsMasterData> listOfAiimsData=new ArrayList<>();
				for (int row = 1; row <= sheet.getLastRowNum(); row++) {// row
					AiimsMasterData mData=new AiimsMasterData();													// loop
					if (sheet.getRow(row) == null)
						break;
					System.out.println("rowNumber"+row);
					XSSFRow xssfRow = sheet.getRow(row);
					siNo++;
					for (int cols = 0; cols <20; cols++) {
						// column loop
						XSSFCell cell = xssfRow.getCell(cols);
						switch (cols) {
							
							case 0:{
								mData.setSiNo(siNo);
							}
							break;
							
							case 1:{
								mData.setAdmissionId(cell.getStringCellValue());
							}
							break;
							case 2:{
								mData.setDistName(cell.getStringCellValue());
							}
							break;
							case 3:{
								mData.setBlockName(cell.getStringCellValue());
							}
							break;
							case 4:{
								mData.setSubCenterName(cell.getStringCellValue());
							}
							break;
							case 5:{
								mData.setVillageName(cell.getStringCellValue());
							}
							break;
							case 6:{
								mData.setCurrentAddress(cell.getStringCellValue());
							}
							break;
							case 7:{
								mData.setSNCUName(cell.getStringCellValue());
							}
							break;
							case 8:{
							   String value = (cell!=null && cell.getCellTypeEnum()!=CellType.BLANK ? new DataFormatter().formatCellValue(cell) : null);
							   if(value!=null && !value.equals("")) {
							    mData.setAdmissionDate(new SimpleDateFormat("dd/MM/yyyy").parse(value));
							   }
							}
							break;
							case 9:{
								if(row==250)
								{
									System.out.println("Hello");
								}
								   String value = (cell!=null && cell.getCellTypeEnum()!=CellType.BLANK ? new DataFormatter().formatCellValue(cell) : null);
								   if(value!=null && !value.equals("")) {
								    mData.setDob(new SimpleDateFormat("yyyy-MM-dd").parse(value));
								   }
							}
							break;
							case 10:{
								mData.setMothersName(cell.getStringCellValue());
							}
							break;
							case 11:{
								mData.setFathersName(cell.getStringCellValue());
							}
							break;
							case 12:{
								mData.setContactNo( Long.parseLong(cell.getStringCellValue()));
							}
							break;
							case 13:{
								if(cell.getStringCellValue()!=null||!cell.getStringCellValue().equalsIgnoreCase(""))
								mData.setCommWorkerName(cell.getStringCellValue());
							}
							break;
							case 14:{
								Long value = (cell!=null && !cell.getStringCellValue().equals("") ? Long.parseLong(cell.getStringCellValue().trim()) : 0);
								mData.setWorkerContactNo(value);
							}
							break;
							case 15:{
								mData.setOutcome(cell.getStringCellValue());
							}
							break;
							case 16:{
								mData.setMaturity(cell.getStringCellValue());
							}
							break;
							case 17:{
								mData.setWeight(cell.getNumericCellValue());
							}
							break;
							case 18:{
								String value = (cell!=null && cell.getCellTypeEnum()!=CellType.BLANK ? new DataFormatter().formatCellValue(cell) : null);
								 if(value!=null && !value.equals("")) {
							    mData.setDischarge(new SimpleDateFormat("dd/MM/yyyy").parse(value));
								}
							}
							break;
							case 19:{
								mData.setIndicationOfAdmission(cell.getStringCellValue());
							}
							break;
								
						}
						
					listOfAiimsData.add(mData);	
					}
				}
				aiimsMasterDataRepository.save(listOfAiimsData);
				workbook.close();
				}catch(Exception e){
					throw new RuntimeException(e);
				}
		
	}
		return new ResponseEntity<String>("Sucessfully uploaded Aiims Master Data",HttpStatus.OK);
	}

	@Override
	public void updateEngineForm() {
		List<EnginesForm> listOfEngineForm = engineFormRepository.findAll();
		for (EnginesForm enginesForm : listOfEngineForm) {
			enginesForm.setUpdatedDate(new Date());
			engineFormRepository.save(enginesForm);
		}
		
	}
}
