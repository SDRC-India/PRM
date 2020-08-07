package org.sdrc.fani.service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.Partner;
import org.sdrc.fani.collections.RegistrationOTP;
import org.sdrc.fani.collections.SuccessStories;
import org.sdrc.fani.collections.UserDetails;
import org.sdrc.fani.models.AreaModel;
import org.sdrc.fani.models.FilepathCaptionModel;
import org.sdrc.fani.models.SuccessStoriesModel;
import org.sdrc.fani.models.TableDataModel;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.CustomAccountRepository;
import org.sdrc.fani.repositories.PartnerRepository;
import org.sdrc.fani.repositories.RegistrationOTPRepository;
import org.sdrc.fani.repositories.SuccessStoriesRepository;
import org.sdrc.fani.util.ImageConverter;
import org.sdrc.fani.util.Mail;
import org.sdrc.fani.util.MailService;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.sdrc.usermgmt.mongodb.domain.Designation;
import org.sdrc.usermgmt.mongodb.repository.DesignationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PartnerServiceImpl implements PartnerService {

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private RegistrationOTPRepository registrationOTPRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	AreaRepository areaRepository;

	@Autowired
	SuccessStoriesRepository successStoriesRepository;

	@Value("${upload.file.path}")
	private String photoIdfilepath;

	@Autowired
	private PartnerRepository partnerRepository;

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	DesignationRepository designationRepository;

	@Autowired
	@Qualifier("customAccountRepository")
	private CustomAccountRepository customAccountRepository;

	private Path photoIdfilePathLocation;

	
	@PostConstruct
	public void init() {
		photoIdfilePathLocation = Paths.get(photoIdfilepath + "successStories/");
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

			// createNewOTPAndSendMail(email, String.valueOf(otp));
			emailExecutor.shutdown();

			return new ResponseEntity<>("OTP has been sent to your email", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			// return null;
			return new ResponseEntity<>("Try Again", HttpStatus.OK);
		}
	}

	private ResponseEntity<String> createNewOTPAndSendMail(String email, String varificationCode) {

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

		mailModel.setSubject("FANIHPM: One Time Password");
		mailModel.setToUserName("Dear User,");
		mailModel.setMessage("\n" + "Your OTP for FANI application: " + Integer.parseInt(varificationCode));

		mailModel.setFromUserName("Thank you!" + "\n" + "\n" + configurableEnvironment.getProperty("email.donot.reply")
				+ "\n" + configurableEnvironment.getProperty("email.disclaimer"));
		mailModel.setEmail(email);

		mailService.sendSimpleMessage(mailModel);
		// collectionChannel.sendEmail().send(MessageBuilder.withPayload(mailModel).build());
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
	public Map<String, List<AreaModel>> getAllAreaList() {

		List<Area> areas = areaRepository.findAllByAreaLevelAreaLevelIdIn(Arrays.asList(2, 3, 4, 5, 6));
		List<AreaModel> areaModelList = new ArrayList<>();
		Map<String, List<AreaModel>> areaMap = new LinkedHashMap<>();
		// setting areas is area-model list
		for (Area area : areas) {
			AreaModel areaModel = new AreaModel();
			areaModel.setAreaCode(area.getAreaCode());
			areaModel.setAreaId(area.getAreaId());
			areaModel.setAreaLevel(area.getAreaLevel().getAreaLevelName());
			areaModel.setAreaName(area.getAreaName());
			areaModel.setParentAreaId(area.getParentAreaId());
			areaModelList.add(areaModel);
		}
		// making levelName as a key
		for (AreaModel areaModel : areaModelList) {
			if (areaMap.containsKey(areaModel.getAreaLevel())) {
				areaMap.get(areaModel.getAreaLevel()).add(areaModel);
			} else {
				areaModelList = new ArrayList<>();
				areaModelList.add(areaModel);
				areaMap.put(areaModel.getAreaLevel(), areaModelList);
			}
		}
		return areaMap;
	}

	@Override
	@Transactional
	public ResponseEntity<String> saveSucessStories(SuccessStoriesModel successStoriesModel,
			OAuth2Authentication oauth) {

		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);

		SuccessStories sucStories = new SuccessStories();
		Account acc = customAccountRepository
				.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		UserDetails user = (UserDetails) acc.getUserDetails();
		if (successStoriesModel.getId() == null) {
			sucStories.setStoryId(successStoriesRepository.findAll().size() + 1);
			sucStories.setDescription(successStoriesModel.getDescription());
			sucStories.setFilepathCaptionModel(successStoriesModel.getFilepathCaptionModel());
			sucStories.setTags(successStoriesModel.getTags());
			sucStories.setTitle(successStoriesModel.getTitle());
			sucStories.setVideoLink(
					successStoriesModel.getVideoLink() == null ? null : successStoriesModel.getVideoLink());
			sucStories.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			sucStories.setFirstName(user.getFirstName());
			sucStories.setCreatedDate(new Date());
			sucStories.setUpdatedDate(new Date());
			sucStories.setIsActive(true);
			sucStories.setIsApprove(false);
		} else {
			SuccessStories fetchedSuccessStorie = successStoriesRepository.findById(successStoriesModel.getId());
			fetchedSuccessStorie.setIsActive(false);
			successStoriesRepository.save(fetchedSuccessStorie);

			sucStories.setStoryId(successStoriesRepository.findAll().size() + 1);
			sucStories.setDescription(successStoriesModel.getDescription());
			sucStories.setFilepathCaptionModel(successStoriesModel.getFilepathCaptionModel());
			sucStories.setTags(successStoriesModel.getTags());
			sucStories.setTitle(successStoriesModel.getTitle());
			sucStories.setVideoLink(
					successStoriesModel.getVideoLink() == null ? null : successStoriesModel.getVideoLink());
			sucStories.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			sucStories.setCreatedDate(new Date());
			sucStories.setUpdatedDate(new Date());
			sucStories.setIsActive(true);
			sucStories.setIsApprove(false);

		}

		successStoriesRepository.save(sucStories);
		return new ResponseEntity<>("Successfully saved ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> uploadFile(MultipartFile multipartfiles) {
		String filePath = null;
		if (!new File(photoIdfilepath + "successStories/").exists()) {
			new File(photoIdfilepath + "successStories/").mkdirs();
		}
		if (multipartfiles != null) {
			try {
				String extentionName = FilenameUtils.getExtension(multipartfiles.getOriginalFilename());
				String fileNameWithDateTime = FilenameUtils.getBaseName(multipartfiles.getOriginalFilename()) + "_"
						+ new Date().getTime() + "." + extentionName;

				filePath = photoIdfilepath + "successStories/" + fileNameWithDateTime;

				Files.copy(multipartfiles.getInputStream(), this.photoIdfilePathLocation.resolve(fileNameWithDateTime),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(filePath, HttpStatus.OK);
	}

	@Override
	public Boolean partnerAvailable(String partnerName) {
		List<Partner> listOfPartner = partnerRepository.findByOrganizationName(partnerName.toUpperCase().trim());
		if (listOfPartner.isEmpty() || listOfPartner == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public SuccessStoriesModel getAllSuccessStories(OAuth2Authentication oauth) {

		SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-mm-yyyy");
		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);
		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		List<SuccessStories> listOfSuccessStories = null;
		boolean isAdmin = false;
		boolean isPartnerAdmin = false;
		for (Designation designation : desig) {
			if (designation.getCode().equals("ADMIN")) {
				isAdmin = true;
			} else if (designation.getCode().equals("PARTNER_ADMIN")) {
				isPartnerAdmin = true;
			}
		}

		if (isAdmin) {
			listOfSuccessStories = successStoriesRepository.findAll().stream()
					.filter(story -> story.getIsActive() == true).collect(Collectors.toList());
		} else {
			if (isPartnerAdmin) {
				List<Account> accounts = customAccountRepository.findByPartnerIdIn(principal.getPartnerId());
				List<String> userName = new ArrayList<>();
				for (Account account : accounts) {
					userName.add(account.getUserName());
				}
				listOfSuccessStories = successStoriesRepository.findBySubmittedByInAndIsActiveTrue(userName);

			} else {
				listOfSuccessStories = successStoriesRepository.findBySubmittedByAndIsActiveTrue(
						SecurityContextHolder.getContext().getAuthentication().getName());
			}
		}

		List<SuccessStoriesModel> lisSuccessStoriesModels = new ArrayList<>();

		SuccessStoriesModel successStoriesModel = new SuccessStoriesModel();
		List<TableDataModel> listOfTableDataModel = new ArrayList<>();
		TableDataModel tableDataModel = null;
		successStoriesModel
				.setTableColumn(Arrays.asList("sl_No#Sl. No.", "title#Title", "tag#Tags", "publishedBy#Published By"));
		int i = 1;
		if (!listOfSuccessStories.isEmpty()) {
			for (SuccessStories successStories : listOfSuccessStories) {
				tableDataModel = new TableDataModel();
				tableDataModel.setId(successStories.getId());
				tableDataModel.setPublishedBy(successStories.getSubmittedBy());
				tableDataModel.setSl_No(i);
				tableDataModel.setTitle(successStories.getTitle());
				tableDataModel.setTag(successStories.getTags());
				tableDataModel.setUploadedOn(simpledateformat.format(successStories.getCreatedDate()));
				tableDataModel.setVideoLink(successStories.getVideoLink());
				tableDataModel.setDescription(successStories.getDescription());
				tableDataModel.setIsApprove(successStories.getIsApprove());
				listOfTableDataModel.add(tableDataModel);
				++i;
			}
		}
		successStoriesModel.setTableData(listOfTableDataModel);
		return successStoriesModel;
	}

	@Override
	public List<FilepathCaptionModel> getSuccessStoriesDetails(String id) {
		SuccessStories fetchedSuccessStories = successStoriesRepository.findById(id);
		List<FilepathCaptionModel> listFilepathCaptionModels = new ArrayList<>();
		FilepathCaptionModel filepathCaptionModel = null;
		for (FilepathCaptionModel path : fetchedSuccessStories.getFilepathCaptionModel()) {
			if (path.getFilepath() != null) {
				filepathCaptionModel = new FilepathCaptionModel();
				filepathCaptionModel.setCaption(path.getCaption() == null ? null : path.getCaption());
				filepathCaptionModel.setFilepath(ImageConverter.encodingPhoto(path.getFilepath()));
				filepathCaptionModel.setPath(path.getFilepath());

				listFilepathCaptionModels.add(filepathCaptionModel);
			}
		}
		return listFilepathCaptionModels;
	}

	@Override
	@Transactional
	public ResponseEntity<String> deleteSuccessStory(String id, OAuth2Authentication oauth) {
		SuccessStories fetchedSuccessStories = successStoriesRepository.findById(id);
		fetchedSuccessStories.setIsActive(false);
		successStoriesRepository.save(fetchedSuccessStories);
		return new ResponseEntity<>("Deleted successfully  ", HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<String> approveSuccessStory(String id, OAuth2Authentication oauth) {
		SuccessStories fetchedSuccessStories = successStoriesRepository.findById(id);
		fetchedSuccessStories.setIsApprove(true);
		successStoriesRepository.save(fetchedSuccessStories);
		return new ResponseEntity<>("Approved successfully  ", HttpStatus.OK);
	}

	@Override
	public SuccessStoriesModel getAllSuccessStoriesForHomePage(OAuth2Authentication oauth) {

		SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-mm-yyyy");
		List<SuccessStories> listOfSuccessStories = successStoriesRepository.findAll().stream()
				.filter(story -> story.getIsActive() == true && story.getIsApprove() == true)
				.collect(Collectors.toList());
		List<TableDataModel> listOfTableDataModel = new ArrayList<>();
		SuccessStoriesModel successStoriesModel = new SuccessStoriesModel();
		int i = 1;
		if (!listOfSuccessStories.isEmpty()) {
			for (SuccessStories successStories : listOfSuccessStories) {
				TableDataModel tableDataModel = new TableDataModel();
				tableDataModel.setId(successStories.getId());
				tableDataModel.setPublishedBy(successStories.getSubmittedBy());
				tableDataModel.setSl_No(i);
				tableDataModel.setTitle(successStories.getTitle());
				tableDataModel.setTag(successStories.getTags());
				tableDataModel.setUploadedOn(simpledateformat.format(successStories.getCreatedDate()));
				tableDataModel.setVideoLink(successStories.getVideoLink());
				tableDataModel.setDescription(successStories.getDescription());
				tableDataModel.setIsApprove(successStories.getIsApprove());
				tableDataModel.setFirstName(successStories.getFirstName());
				listOfTableDataModel.add(tableDataModel);
				++i;
			}
		}
		successStoriesModel.setTableData(listOfTableDataModel);
		return successStoriesModel;
	}
}
