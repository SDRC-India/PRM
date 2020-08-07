package org.sdrc.fani.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.RegistrationOTP;
import org.sdrc.fani.collections.UserDetails;
import org.sdrc.fani.models.AccountTableJSONModel;
import org.sdrc.fani.models.Gender;
import org.sdrc.fani.models.RejectionModel;
import org.sdrc.fani.models.UserApprovalModel;
import org.sdrc.fani.models.UserStatus;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.CustomAccountRepository;
import org.sdrc.fani.repositories.RegistrationOTPRepository;
import org.sdrc.fani.util.Mail;
import org.sdrc.fani.util.MailService;
import org.sdrc.usermgmt.model.AuthorityControlType;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.AssignedDesignations;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class PartnerRegistrationServiceImpl implements PartnerRegistrationService {
	
	@Qualifier("mongoDesignationRepository")
	@Autowired
	private DesignationRepository designationRepository;
	
	@Autowired
	private ConfigurableEnvironment configurableEnvironment;
	
	@Autowired
	private RegistrationOTPRepository registrationOTPRepository;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private AreaRepository areaRepository;
	
	@Autowired
	@Qualifier("customAccountRepository")
	private CustomAccountRepository customAccountRepository;

	@Value("${upload.file.path}")
	private String photoIdfilepath;
	
	private Path bulkUserPathLocation;
	
	@Value("#{'${manadatory.templatecolumn}'.split(',')}")
	private List<String> columnNo;

	@Value("#{'${manadatory.templatecolumnstatedistrictblock}'.split(',')}")
	private List<String> columnNo1;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Value("${templatedownloadpath.path}")
	private String downloadtemplatepath;
	
	@Value("${invalid.file.error}")
	private String invalidFileError;
	
	@Value("${sheet.blank.error}")
	private String sheetBlankError;
	
	
	@PostConstruct
	public void init() {
		bulkUserPathLocation = Paths.get(photoIdfilepath+"BulkUser/");
	}
	
	@Override
	public List<Designation> getAllRoles() {
		List<Designation> desgList = designationRepository.findAllByOrderByIdAsc();

		if ((!configurableEnvironment.containsProperty("allow.admin.creation"))
				|| (configurableEnvironment.getProperty("allow.admin.creation").equals("false"))) {

			desgList = desgList.stream().filter(desgName -> !"ADMIN".equals(desgName.getName()))
					.collect(Collectors.toList());
		}
		return desgList;
	}

	@Override
	public ResponseEntity<String> getEmailVerificationCode(String email) {
		try {
			Random random = new Random();
			int otp = random
					.nextInt(Integer.parseInt(configurableEnvironment.getProperty("generate.otp.max.digit"))
							- Integer.parseInt(configurableEnvironment.getProperty("generate.otp.min.digit")) + 1)
					+ Integer.parseInt(configurableEnvironment.getProperty("generate.otp.min.digit"));

			RegistrationOTP reOtp = registrationOTPRepository.findByEmailIdAndIsActiveTrue(email);
			ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
			if (reOtp == null) {
				emailExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							createNewOTPAndSendMail(email, String.valueOf(otp));
							// SendEmailUtility.sendmail(emaildummy);
						} catch (Exception e) {
							// logger.error("failed", e);
						}
					}
				});
			
				} else {
				reOtp.setIsActive(false);
				registrationOTPRepository.save(reOtp);

				emailExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							createNewOTPAndSendMail(email, String.valueOf(otp));
							// SendEmailUtility.sendmail(emaildummy);
						} catch (Exception e) {
							// logger.error("failed", e);
						}
					}
				});
				}
			
				//	createNewOTPAndSendMail(email, String.valueOf(otp));
			emailExecutor.shutdown();
			
			
			return new ResponseEntity<>("OTP has been sent to your email", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			// return null;
			return new ResponseEntity<>("Try Again", HttpStatus.OK);
		}
	}

	protected ResponseEntity<String> createNewOTPAndSendMail(String email, String varificationCode) {

		RegistrationOTP registrationOTP = new RegistrationOTP();
		registrationOTP.setEmailId(email);
		try {
			registrationOTP.setIpAddress(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		registrationOTP.setIsActive(true);
		registrationOTP.setCreatedDateAndTime(new Timestamp(System.currentTimeMillis()));
		registrationOTP.setVarificationCode(Integer.parseInt(varificationCode));
		registrationOTPRepository.save(registrationOTP);

		Mail mailModel = new Mail();
		mailModel.setToEmailIds(Arrays.asList(email));
		
		mailModel.setSubject("Covid-19 Odisha: One Time Password");
		mailModel.setToUserName("Dear User,");
		mailModel.setMessage("\n"+"Your OTP for Covid-19 Odisha application: " + Integer.parseInt(varificationCode));
		
		mailModel.setFromUserName("Thank you!" + "\n"+ "\n"+ configurableEnvironment.getProperty("email.donot.reply") +"\n"+ configurableEnvironment.getProperty("email.disclaimer"));
		mailModel.setEmail(email);

		mailService.sendSimpleMessage(mailModel);
		//collectionChannel.sendEmail().send(MessageBuilder.withPayload(mailModel).build());
		return new ResponseEntity<>("OTP verified", HttpStatus.OK);
		// return mailModel;
	}

	@Override
	public ResponseEntity<String> oTPAndEmailAvailibility(String email, Integer varificationCode) {

		RegistrationOTP registrationOTP = registrationOTPRepository
				.findByEmailIdAndVarificationCodeAndIsActiveTrue(email, varificationCode);
		if (registrationOTP != null) {
			long minutes = TimeUnit.MILLISECONDS
					.toMinutes(System.currentTimeMillis() - registrationOTP.getCreatedDateAndTime().getTime());
			if (minutes <= 30) {
				registrationOTP.setIsActive(false);
				registrationOTPRepository.save(registrationOTP);
				return new ResponseEntity<>("OTP verified", HttpStatus.OK);

			} else {
				registrationOTP.setIsActive(false);
				registrationOTPRepository.save(registrationOTP);

				return new ResponseEntity<>("OTP expired! Try another.", HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>("Invalid OTP! Please enter valid OTP", HttpStatus.OK);
		}
	}

	@Override
	public AccountTableJSONModel getPartnersForApproval() {
		List<String> tableColumn = new ArrayList<>();
		tableColumn.add("Username");
		tableColumn.add("Name");
		tableColumn.add("Gender");
		tableColumn.add("Mobile Number");
		tableColumn.add("Designation");
		tableColumn.add("Area");
		tableColumn.add("Submitted On");
		tableColumn.add("Email");

		List<Map<String,Object>> tableMap = new ArrayList<>();
		List<Account> accounts=customAccountRepository.findAll();
		
		List<Area> areaList = areaRepository.findAllByAreaLevelAreaLevelIdIn(Arrays.asList(1,2));
		Map<Integer, String> areaMap = areaList.stream()
				.collect(Collectors.toMap(Area::getAreaId, Area::getAreaName,(area1, area2) -> area1));
		List<Designation> designationList = designationRepository.findAll();
		Map<String, String> designationMap = designationList.stream()
				.collect(Collectors.toMap(Designation::getId, Designation::getName));
		
		
		for (Account acc : accounts) {
			if(!acc.getUserName().equals("admin")) {
			UserDetails user = (UserDetails) acc.getUserDetails();
			//PartnerApprovalModel model = new PartnerApprovalModel();
			Map<String,Object> map = new LinkedHashMap<>();
			
			map.put("Username", acc.getUserName());
			map.put("Name",
					user.getFirstName().concat(" ").concat(user.getLastName() != null ? user.getLastName() : ""));
			map.put("Gender", (user.getGender()!=null ? user.getGender().toString() : null));
			map.put("Mobile Number",user.getMobNo());
			
				if (acc.getMappedAreaIds() != null && !acc.getMappedAreaIds().isEmpty()) {

					List<String> areaNames = new ArrayList<>();
					acc.getMappedAreaIds().forEach(areaId -> {
						areaNames.add(areaMap.get(areaId));
					});
					map.put("Area", areaNames);
				}
			
			map.put("Designation",designationMap.get(acc.getAssignedDesignations().get(0).getDesignationIds()));
			map.put("Email", acc.getEmail());
			map.put("Submitted On", 
					user.getCreatedDate() != null ? new SimpleDateFormat("dd-MM-yyyy").format(user.getCreatedDate())
							: new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
			map.put("status", user.getUserStatus()==null ? UserStatus.APPROVED : user.getUserStatus().toString());
			map.put("id", acc.getId());
			map.put("isActive", acc.isEnabled());
			tableMap.add(map);
		}
		}

		AccountTableJSONModel model = new AccountTableJSONModel();
		model.setTableColumn(tableColumn);
		model.setTableData(tableMap);

		return model;

	}

	@Override
	public ResponseEntity<String> approvePartner(List<String> ids) {
		// Approved users: enabled = true; expired = false; Locked = false
		try {

			List<Account> accs = customAccountRepository.findByIdIn(ids);
			for (Account acc : accs) {
				acc.setEnabled(true);
				acc.setExpired(false);
				acc.setLocked(false);

				UserDetails user = (UserDetails) acc.getUserDetails();
				user.setApprovedOn(new Date());
				user.setApprovedBy(SecurityContextHolder.getContext().getAuthentication().getName());
				user.setUserStatus(UserStatus.APPROVED);
				customAccountRepository.save(acc);
			}

			return new ResponseEntity<String>(configurableEnvironment.getProperty("partner.approve.success"),
					HttpStatus.OK);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseEntity<String> rejectPartner(List<String> ids, List<RejectionModel> rejectionModel) {
		// Rejected users: enabled = true; expired = true; Locked = false
		Map<String, String> modelMap = rejectionModel.stream()
				.collect(Collectors.toMap(RejectionModel::getId, RejectionModel::getRejectionMessage));

		try {

			List<Account> accs = customAccountRepository.findByIdIn(ids);

			for (Account acc : accs) {

				acc.setEnabled(true);
				acc.setExpired(true);
				acc.setLocked(false);

				UserDetails user = (UserDetails) acc.getUserDetails();
				user.setRejectedOn(new Date());
				user.setRejectedBy(SecurityContextHolder.getContext().getAuthentication().getName());
				user.setUserStatus(UserStatus.REJECTED);
				// rejected messages
				user.setRejectionMessage(modelMap.get(acc.getId()));
				customAccountRepository.save(acc);
			}

			return new ResponseEntity<String>(configurableEnvironment.getProperty("partner.reject.success"),
					HttpStatus.OK);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<UserApprovalModel> getUsersByRoleAndPartner(List<String> roleIds) {
		
		AssignedDesignations assignedDesignations = new AssignedDesignations();
		assignedDesignations.setDesignationIds(roleIds.get(0));
		assignedDesignations.setEnable(true);
		
		List<Area> areaList = areaRepository.findAllByAreaLevelAreaLevelIdIn(Arrays.asList(1,2));
		Map<Integer, String> areaMap = areaList.stream()
				.collect(Collectors.toMap(Area::getAreaId, Area::getAreaName,(area1, area2) -> area1));
		List<Designation> designationList = designationRepository.findAll();
		Map<String, Designation> designationMap = designationList.stream()
				.collect(Collectors.toMap(Designation::getId, designation -> designation ));
		
		List<Account> accounts=customAccountRepository.getAssignedDesignations(Arrays.asList(assignedDesignations));
		List<UserApprovalModel> tableMap = new ArrayList<>();
		int i=1;
		for (Account acc : accounts) {
			UserDetails user = (UserDetails) acc.getUserDetails();
			UserApprovalModel usr = new UserApprovalModel();
			usr.setUserName(acc.getUserName());
			usr.setFirstName(user.getFirstName());
			usr.setMiddleName(user.getMiddleName()==null?null:user.getMiddleName());
			usr.setLastName(user.getLastName());
			usr.setDob(user.getDob()==null?null:user.getDob());
			usr.setName(user.getFirstName().concat(" ").concat(user.getMiddleName() != null ? user.getMiddleName() : ""));
			usr.setGender(user.getGender()!=null ? user.getGender().toString() : null);
			usr.setMobileNumber(user.getMobNo());
			
			if(acc.getMappedAreaIds()!=null) {
				if(!acc.getMappedAreaIds().isEmpty()) {
					usr.setAreaName(areaMap.get(acc.getMappedAreaIds().get(0)));
					usr.setAreaId(acc.getMappedAreaIds());
				}
			}
			
			if(acc.getAssignedDesignations()!=null) {
				if(!acc.getAssignedDesignations().isEmpty()) {
					Designation assignedDesignation = designationMap.get(acc.getAssignedDesignations().get(0).getDesignationIds());
					usr.setDesignation(assignedDesignation.getName());
					usr.setDesignationId(Arrays.asList(acc.getAssignedDesignations().get(0).getDesignationIds()));
					usr.setDegSlugId(assignedDesignation.getSlugId());
				}
			}
			usr.setEmail(acc.getEmail());
			usr.setId(acc.getId());
			if(!acc.isLocked() && acc.isEnabled() && !acc.isExpired()){
				usr.setSlNo(i);
				tableMap.add(usr);
				i++;
			}
		}

		return tableMap;

	}

	@Override
	public List<Area> getAreaList(int areaLevelId, int parentAreaId) {
		return areaRepository.findAllByAreaLevelAreaLevelIdInAndParentAreaIdOrderByAreaNameAsc(Arrays.asList(areaLevelId),parentAreaId);
	}

	@Override
	public String downLoadBulkTemplate() throws Exception {

		// List<Area> listOfAreas = areaRepository.findAllByOrderByAreaNameAsc();
		List<Area> listOfAreas = areaRepository.findByParentAreaIdInAndAreaLevelAreaLevelIdOrderByAreaNameAsc(Arrays.asList(1), 2);
		List<String> userLevelList = new ArrayList<>();

		getAllRoles().stream().forEach(level -> {
			userLevelList.add(level.getName());
		});

		List<String> gender = Arrays.asList("Male", "Female");
		List<String> districts = new ArrayList<>();

//		int areRowNum = 1;
		for (Area area : listOfAreas) {
			districts.add(area.getAreaName());
		}

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("bulkUserTemplate/");
		String path = url.getPath().replaceAll("%20", " ");
		File files[] = new File(path).listFiles();

		if (files == null) {
			throw new RuntimeException("No file found in path " + path);
		}

		if (!new File(downloadtemplatepath).exists()) {
			new File(downloadtemplatepath).mkdirs();

		}

		if (new File(downloadtemplatepath + "/bulk-user-template.xlsx").exists()) {
			new File(downloadtemplatepath + "/bulk-user-template.xlsx").delete();

		}

		copyFileUsingChannel(files[0], new File(downloadtemplatepath + "/bulk-user-template.xlsx"));
		FileInputStream file = new FileInputStream(new File(downloadtemplatepath + "/bulk-user-template.xlsx"));
		// Get the workbook instance for XLS file
		XSSFWorkbook workbook = new XSSFWorkbook(file);

		// Get the sheet from the workbook
		XSSFSheet userListSheet = workbook.getSheet("UserList Template");
		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(userListSheet);

		CellRangeAddressList genderList = new CellRangeAddressList(1, 500, 1, 1);
		CellRangeAddressList userRoleList = new CellRangeAddressList(1, 500, 4, 4);
//		CellRangeAddressList districtList = new CellRangeAddressList(1, 500, 5, 5);

		XSSFDataValidationConstraint userLevelConstraint = (XSSFDataValidationConstraint) dvHelper
				.createExplicitListConstraint(Arrays.stream(userLevelList.toArray()).toArray(String[]::new));
		XSSFDataValidationConstraint genderConstraint = (XSSFDataValidationConstraint) dvHelper
				.createExplicitListConstraint(Arrays.stream(gender.toArray()).toArray(String[]::new));
//		XSSFDataValidationConstraint districtConstraint = (XSSFDataValidationConstraint)
//				dvHelper.createExplicitListConstraint(Arrays.stream(districts.toArray()).toArray(String[]::new));
//		
//		
		XSSFDataValidation userRoleValidation = (XSSFDataValidation) dvHelper.createValidation(userLevelConstraint,
				userRoleList);
		XSSFDataValidation genderValidation = (XSSFDataValidation) dvHelper.createValidation(genderConstraint,
				genderList);
//		XSSFDataValidation districtValidation = (XSSFDataValidation)dvHelper.createValidation(
//				districtConstraint, districtList);

//		DataValidation userRoleValidation = new HSSFDataValidation(userRoleList, userLevelConstraint);
//		DataValidation genderValidation = new HSSFDataValidation(genderList, genderConstraint);
//		DataValidation districtValidation = new HSSFDataValidation(districtList, districtConstraint);
		userRoleValidation.setShowErrorBox(true);
		genderValidation.setShowErrorBox(true);
//		districtValidation.setShowErrorBox(true);
//		
		userListSheet.addValidationData(userRoleValidation);
		userListSheet.addValidationData(genderValidation);
//		userListSheet.addValidationData(districtValidation);

		XSSFSheet masterDataSheet = workbook.getSheet("Master_Data");

		int areRowNum = 1;
		for (Area area : listOfAreas) {
			XSSFRow row = masterDataSheet.createRow(areRowNum);
			row.createCell(0).setCellValue(area.getAreaId());
			row.createCell(1).setCellValue(area.getAreaCode());
			row.createCell(2).setCellValue(area.getAreaLevel().getAreaLevelId().toString());
			row.createCell(3).setCellValue(area.getAreaName());
			row.createCell(4).setCellValue(area.getParentAreaId());
			++areRowNum;

		}

		XSSFRow row = masterDataSheet.createRow(5555);
		row.createCell(55).setCellValue("rmwr");
		masterDataSheet.protectSheet("pks@123#");
//		dropDownSheet.protectSheet("pks@123#");
		workbook.setSheetHidden(1, true);
//		workbook.setSheetHidden(3, true);

		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(downloadtemplatepath + "/bulk-user-template" + ".xlsx");
		workbook.write(fileOut);
		fileOut.close();
		// Closing the workbook
		workbook.close();

		return downloadtemplatepath + "/bulk-user-template" + ".xlsx";
	}

	private void copyFileUsingChannel(File source, File dest) throws Exception {

		FileChannel sourceChannel = null;
		FileChannel destChannel = null;
		try {
			sourceChannel = new FileInputStream(source).getChannel();
			destChannel = new FileOutputStream(dest).getChannel();
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		} finally {
			sourceChannel.close();
			destChannel.close();
		}

	}

	@Override
	@Caching(evict = {
		    @CacheEvict(value = "userwisearea", allEntries=true),
		    @CacheEvict(value = "usercount", allEntries=true)
		})
	public List<String> uploadBulkTemplate(MultipartFile multipartfile) throws Exception {

		Date date = Calendar.getInstance().getTime();
		List<Account> accountLists = customAccountRepository.findAll();
		List<String> emailList = new ArrayList<String>();
		List<String> username = new ArrayList<String>();
		accountLists.stream().forEach(account -> {
			if (account.getEmail() != null) {
				if (!account.isExpired())
					emailList.add(account.getEmail().toLowerCase());
			}

			username.add(account.getUserName().toLowerCase());
		});

		File newFile = null;
		newFile = new File(photoIdfilepath + "BulkUser/");
		if (!newFile.exists()) {
			newFile.mkdirs();
		}
		String filePath = FilenameUtils.getBaseName(multipartfile.getOriginalFilename()) + "_" + new Date().getTime()
				+ "." + FilenameUtils.getExtension(multipartfile.getOriginalFilename());
		Files.copy(multipartfile.getInputStream(), this.bulkUserPathLocation.resolve(filePath),
				StandardCopyOption.REPLACE_EXISTING);

		List<String> messagelist = new ArrayList<String>();

		if (!FilenameUtils.getExtension(multipartfile.getOriginalFilename()).equals("xlsx")) {
			messagelist.add(invalidFileError);
			return messagelist;
		}

		FileInputStream file = new FileInputStream(photoIdfilepath + "BulkUser/" + filePath);

		// Get the workbook instance for XLS file
		XSSFWorkbook workbook = new XSSFWorkbook(file);

		XSSFSheet dropDown = workbook.getSheet("Master_Data");
		if (dropDown != null) {
			if (dropDown.getRow(5555) == null) {
				messagelist.add(invalidFileError);
				return messagelist;
			}

		} else {
			messagelist.add(invalidFileError);
			return messagelist;
		}
		XSSFSheet sheet = workbook.getSheet("UserList Template");

		Map<String, Integer> emailMap = new HashMap<>();
		Map<Object, Integer> usernameMap = new HashMap<>();

		List<Account> accountList = new ArrayList<Account>();

		if (isSheetEmpty(sheet)) {

			try {
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					if (!isRowEmpty(sheet.getRow(i))) {

						if (sheet.getRow(i) != null) {
							boolean flag = true;
							// Email duplicate check

							if (!isCellBlank(sheet.getRow(i).getCell(3))) {

								// Email id validation
								if (!isValidEmailAddress(sheet.getRow(i).getCell(3).toString().toLowerCase())) {
									messagelist.add("Email (" + sheet.getRow(i).getCell(3).toString()
											+ ") is incorect format for Row No " + (sheet.getRow(i).getRowNum() + 1));
								}
								// sheet contains duplicate EmailId
								if (!emailMap.containsKey(sheet.getRow(i).getCell(3).toString().trim())) {
									emailMap.put(sheet.getRow(i).getCell(3).toString().trim(),
											(sheet.getRow(i).getRowNum() + 1));
								} else {
									messagelist.add("Email (" + sheet.getRow(i).getCell(3).toString() + ") in Row No "
											+ (sheet.getRow(i).getRowNum() + 1) + " is duplicate for Row no "
											+ emailMap.get(sheet.getRow(i).getCell(3).toString().trim()));
								}

								// duplicate MailId
								if (emailList.contains(sheet.getRow(i).getCell(3).toString().toLowerCase())) {
									messagelist.add("Email (" + sheet.getRow(i).getCell(3).toString()
											+ ") already exists for Row No " + (sheet.getRow(i).getRowNum() + 1));
								}
							}

							// UserName duplicate Check
							if (!isCellBlank(sheet.getRow(i).getCell(6))) {
								if (username.contains(sheet.getRow(i).getCell(6).getCellTypeEnum() == CellType.STRING
										? sheet.getRow(i).getCell(6).getStringCellValue().toLowerCase()
										: sheet.getRow(i).getCell(6).getNumericCellValue())) {
									messagelist.add("Username (" + sheet.getRow(i).getCell(6).getStringCellValue()
											+ ") already exists for Row No " + (sheet.getRow(i).getRowNum() + 1));
								}

								// sheet contains duplicate userName
								if (!usernameMap.containsKey(sheet.getRow(i).getCell(6).getCellTypeEnum() == CellType.STRING
										? sheet.getRow(i).getCell(6).getStringCellValue().toLowerCase().trim()
										: sheet.getRow(i).getCell(6).getNumericCellValue())) {
									usernameMap.put(sheet.getRow(i).getCell(6).getCellTypeEnum() == CellType.STRING
											? sheet.getRow(i).getCell(6).getStringCellValue().toLowerCase().trim()
													: sheet.getRow(i).getCell(6).getNumericCellValue(),
											(sheet.getRow(i).getRowNum() + 1));
								} else {
									messagelist.add(
											"Username (" + (sheet.getRow(i).getCell(6).getCellTypeEnum() == CellType.STRING
													? sheet.getRow(i).getCell(6).getStringCellValue().toLowerCase()
															: sheet.getRow(i).getCell(6).getNumericCellValue())
													+ ") in Row No " + (sheet.getRow(i).getRowNum() + 1)
													+ " is duplicate for Row no " 
													+ usernameMap.get(sheet.getRow(i).getCell(6).getCellTypeEnum() == CellType.STRING
															? sheet.getRow(i).getCell(6).getStringCellValue().toLowerCase()
																	: sheet.getRow(i).getCell(6).getNumericCellValue()));
								}

							}

							for (int j = 0; j < columnNo.size(); j++) {
								if (isCellBlank(sheet.getRow(i).getCell(Integer.parseInt(columnNo.get(j))))) {
									messagelist.add("Manadatory field is blank for "
											+ sheet.getRow(0).getCell(Integer.parseInt(columnNo.get(j))).toString()
											+ " of row no." + (sheet.getRow(i).getRowNum() + 1));
								}
							}
						}

						if (sheet.getRow(i).getCell(4) != null) {

							switch (sheet.getRow(i).getCell(4).getStringCellValue()) {
							case "DISTRICT LEVEL":
								if (isCellBlank(sheet.getRow(i).getCell(Integer.parseInt(columnNo1.get(0))))) {
									messagelist.add("Manadatory field is blank for "
											+ sheet.getRow(0).getCell(Integer.parseInt(columnNo1.get(0))).toString()
											+ " of row no." + (sheet.getRow(i).getRowNum() + 1));
								}
								break;
							case "VOLUNTEER":
								if (isCellBlank(sheet.getRow(i).getCell(Integer.parseInt(columnNo1.get(0))))) {
									messagelist.add("Manadatory field is blank for "
											+ sheet.getRow(0).getCell(Integer.parseInt(columnNo1.get(0))).toString()
											+ " of row no." + (sheet.getRow(i).getRowNum() + 1));
								}
								break;
							}

						}
					}
				}
			} catch (Exception e) {
				messagelist.add(invalidFileError);
				workbook.close();
				return messagelist;
			}
			
			if (messagelist.isEmpty()) {

				Map<String, Area> stateMap = new HashMap<String, Area>();
				Map<String, Area> districtBlockMap = new HashMap<String, Area>();
				areaRepository.findByParentAreaIdInAndAreaLevelAreaLevelIdOrderByAreaNameAsc(Arrays.asList(-1,1), 2).stream()
						.forEach(area -> {
							switch (area.getAreaLevel().getAreaLevelId()) {
							case 1:
								stateMap.put(area.getAreaName(), area);
								break;
							case 2:
								districtBlockMap.put(area.getAreaName() + "-" + area.getParentAreaId(), area);
								break;
							}
						});
				

				Map<String, Designation> designationMap = new HashMap<>();
				designationRepository.findAll().stream().forEach(desg -> {
					designationMap.put(desg.getName(), desg);
				});

				List<Integer> mappedAreaId = null;
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					if (!isRowEmpty(sheet.getRow(i))) {

						Account acc = new Account();
						UserDetails userDetails = new UserDetails();

						mappedAreaId = new ArrayList<>();
						userDetails.setFirstName(sheet.getRow(i).getCell(0).toString().split(" ")[0]);
						userDetails.setLastName(sheet.getRow(i).getCell(0).toString().split(" ").length > 1 
								? sheet.getRow(i).getCell(0).toString().split(" ")[1] : "");
						userDetails.setGender(sheet.getRow(i).getCell(1) == null ? null
								: Gender.valueOf(sheet.getRow(i).getCell(1).toString().toUpperCase()));
						userDetails.setMobNo(sheet.getRow(i).getCell(2) == null ? null
								: sheet.getRow(i).getCell(2).getCellTypeEnum() == CellType.STRING
								? sheet.getRow(i).getCell(2).getStringCellValue()
								: Integer.toString(((int) sheet.getRow(i).getCell(2).getNumericCellValue())));
						acc.setEmail(sheet.getRow(i).getCell(3) == null ? null
								: sheet.getRow(i).getCell(3).toString().toLowerCase());
						// userDetails.setOthersDevPartner(sheet.getRow(i).getCell(13)==null ? null :
						// sheet.getRow(i).getCell(13).toString());
						userDetails.setCreatedDate(date);
						userDetails.setApprovedOn(date);
						userDetails.setUserStatus(UserStatus.APPROVED);
						AssignedDesignations asg = new AssignedDesignations();
						asg.setDesignationIds(designationMap.get(sheet.getRow(i).getCell(4).toString()).getId());
						asg.setEnable(true);
						acc.setAssignedDesignations(Arrays.asList(asg));

//						authId.addAll(
//								Arrays.asList(map.get(sheet.getRow(i).getCell(15).getStringCellValue()).split(",")));
//						acc.setAuthorityIds(authId);

						acc.setUserName(sheet.getRow(i).getCell(6).getCellTypeEnum() == CellType.STRING
								? sheet.getRow(i).getCell(6).getStringCellValue().toLowerCase()
								: Integer.toString(((int) sheet.getRow(i).getCell(6).getNumericCellValue())));

						acc.setPassword(
								passwordEncoder.encode(sheet.getRow(i).getCell(7).getCellTypeEnum() == CellType.STRING
										? sheet.getRow(i).getCell(7).getStringCellValue()
										: Integer.toString(((int) sheet.getRow(i).getCell(7).getNumericCellValue()))));

						switch (sheet.getRow(i).getCell(4).toString()) {
						case "STATE LEVEL":
							mappedAreaId.add(1);
							break;
						case "DISTRICT LEVEL":
						case "VOLUNTEER":
							if(districtBlockMap.containsKey(sheet.getRow(i).getCell(5).toString() + "-1")) {
//								mappedAreaId.add(1);
								mappedAreaId.add(districtBlockMap
										.get(sheet.getRow(i).getCell(5).toString() + "-1")
										.getAreaId());
							}else {
								messagelist.add("Invalid area input "
										+ sheet.getRow(i).getCell(5).toString()
										+ " of row no." + (sheet.getRow(i).getRowNum() + 1));
								messagelist.add("Please download a fresh template and fill the user details.");
								workbook.close();
								return messagelist;
							}
							
							break;
//						case "VOLUNTEER":
//							mappedAreaId.add(1);
//							mappedAreaId.add(districtBlockMap
//									.get(sheet.getRow(i).getCell(5).toString() + "-1")
//									.getAreaId());
//							break;

						}
						acc.setAuthorityControlType(AuthorityControlType.DESIGNATION);
						acc.setMappedAreaIds(mappedAreaId);
						acc.setUserDetails(userDetails);
						accountList.add(acc);

					}

				}
				customAccountRepository.save(accountList);
			}
			workbook.close();
			return messagelist;
		} else {
			messagelist.add(sheetBlankError);
			workbook.close();
			return messagelist;
		}

	}

	private boolean isValidEmailAddress(String email) {

        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
	}
	
	private boolean isSheetEmpty(XSSFSheet sheet) {

		Iterator rows = sheet.rowIterator();
		while (rows.hasNext()) {
			XSSFRow row = (XSSFRow) rows.next();
			if (row.getRowNum() != 0) {
				Iterator cells = row.cellIterator();
				while (cells.hasNext()) {
					XSSFCell cell = (XSSFCell) cells.next();
					if (!cell.getStringCellValue().isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isRowEmpty(XSSFRow hssfRow) {
		for (int c = hssfRow.getFirstCellNum(); c < hssfRow.getLastCellNum(); c++) {
			XSSFCell cell = hssfRow.getCell(c);
			if (cell != null && cell.getCellTypeEnum() != CellType.BLANK)
				return false;
		}
		return true;
	}

	private boolean isCellBlank(XSSFCell cell) {

		if (cell == null) {
			return true;
		} else if (cell.getCellTypeEnum() == CellType.BLANK) {
			return true;
		} else {
			return false;
		}
	}
}
