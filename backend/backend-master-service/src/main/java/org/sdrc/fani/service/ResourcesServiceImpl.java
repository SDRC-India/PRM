package org.sdrc.fani.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FilenameUtils;
import org.sdrc.fani.collections.Gallery;
import org.sdrc.fani.collections.ImportantLinks;
import org.sdrc.fani.collections.Resources;
import org.sdrc.fani.collections.SuccessStories;
import org.sdrc.fani.collections.UserDetails;
import org.sdrc.fani.collections.WhatsNew;
import org.sdrc.fani.models.FilepathCaptionModel;
import org.sdrc.fani.models.ImageModel;
import org.sdrc.fani.models.TableDataModel;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.repositories.CustomAccountRepository;
import org.sdrc.fani.repositories.GalleryRepository;
import org.sdrc.fani.repositories.ImportantLinksRepository;
import org.sdrc.fani.repositories.ResourcesRepository;
import org.sdrc.fani.repositories.SuccessStoriesRepository;
import org.sdrc.fani.repositories.WhatsNewRepository;
import org.sdrc.fani.util.ImageConverter;
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

import in.co.sdrc.sdrcdatacollector.document.Type;
import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeDetailRepository;
import in.co.sdrc.sdrcdatacollector.mongorepositories.TypeRepository;

@Service
public class ResourcesServiceImpl implements ResourcesService {

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	@Qualifier("customAccountRepository")
	private CustomAccountRepository customAccountRepository;

	@Autowired
	ResourcesRepository resourcesRepository;

	@Autowired
	private TypeRepository typeRepository;

	@Autowired
	private TypeDetailRepository typeDetailRepository;

	@Autowired
	private DesignationRepository designationRepository;
	
	@Autowired
	private ImportantLinksRepository importantLinksRepository;
	
	@Autowired
	private GalleryRepository galleryRepository;
	
	@Autowired
	private WhatsNewRepository whatsNewRepository;
	
	@Autowired
	private SuccessStoriesRepository successStoriesRepository;
	
	@Autowired
	private ConfigurableEnvironment env;

	@Value("${upload.file.path}")
	private String photoIdfilepath;

	private Path photoIdfilePathLocation;

	@PostConstruct
	public void init() {
		photoIdfilePathLocation = Paths.get(photoIdfilepath + "resources/");
	}

	@Override
	public ResponseEntity<String> saveResources(ResourceModel resourceModel, OAuth2Authentication oauth) {

		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);

		Resources resource = new Resources();
		Account acc = customAccountRepository
				.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		UserDetails user = (UserDetails) acc.getUserDetails();
		if (resourceModel.getId() == null) {
			resource.setResourcesId(resourcesRepository.findAll().size() + 1);
			resource.setDescription(resourceModel.getDescription());
			resource.setFilepathCaptionModel(resourceModel.getFilepathCaptionModel());
			resource.setTags(resourceModel.getTags() == null ? null : resourceModel.getTags());
			resource.setTitle(resourceModel.getTitle());
			resource.setResourceType(resourceModel.getResourceType());
			resource.setVideoLink(resourceModel.getVideoLink() == null ? null : resourceModel.getVideoLink());
			resource.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			resource.setFirstName(user.getFirstName());
			resource.setCreatedDate(new Date());
			resource.setUpdatedDate(new Date());
			resource.setIsActive(true);
			resource.setIsApprove(false);
		} else {
			Resources fetchedResources = resourcesRepository.findById(resourceModel.getId());
			fetchedResources.setIsActive(false);
			resourcesRepository.save(fetchedResources);
			resource.setResourcesId(resourcesRepository.findAll().size() + 1);
			resource.setDescription(resourceModel.getDescription());
			resource.setFilepathCaptionModel(resourceModel.getFilepathCaptionModel());
			resource.setTags(resourceModel.getTags() == null ? null : resourceModel.getTags());
			resource.setTitle(resourceModel.getTitle());
			resource.setResourceType(resourceModel.getResourceType());
			resource.setVideoLink(resourceModel.getVideoLink() == null ? null : resourceModel.getVideoLink());
			resource.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			resource.setCreatedDate(new Date());
			resource.setUpdatedDate(new Date());
			resource.setIsActive(true);
			resource.setIsApprove(false);
		}
		resourcesRepository.save(resource);
		return new ResponseEntity<>("Successfully saved ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> importResourceType() {

		Type fetchedType = typeRepository.findByTypeName("Resource Type");
		if (fetchedType == null) {
			Type type = new Type();
			type.setSlugId(1);
			type.setTypeName("Resource Type");
			type.setDescription("Resource Type");

			Type savedType = typeRepository.save(type);

			TypeDetail typeDetails = null;
			 
			typeDetails = new TypeDetail();
			typeDetails.setSlugId(1);
			typeDetails.setType(savedType);
			typeDetails.setName("Guidelines and Tools");
			typeDetails.setOrderLevel(0);
			typeDetailRepository.save(typeDetails);

			typeDetails = new TypeDetail();
			typeDetails.setSlugId(2);
			typeDetails.setType(savedType);
			typeDetails.setName("Documents");
			typeDetails.setOrderLevel(4);
			typeDetailRepository.save(typeDetails);
			
		}
		
		

		Type fetchedGalleryType = typeRepository.findByTypeName("Gallery Type");
		if (fetchedGalleryType == null) {
			Type type = new Type();
			type.setSlugId(1);
			type.setTypeName("Gallery Type");
			type.setDescription("Gallery Type");

			Type savedType = typeRepository.save(type);

			TypeDetail typeDetails = null;

			/*
			 * Guidelines and Tools Image Gallery Video gallery
			 */
			
			
			typeDetails = new TypeDetail();
			typeDetails.setSlugId(1);
			typeDetails.setType(savedType);
			typeDetails.setName("Image Gallery");
			typeDetails.setOrderLevel(0);
			typeDetailRepository.save(typeDetails);

			typeDetails = new TypeDetail();
			typeDetails.setSlugId(2);
			typeDetails.setType(savedType);
			typeDetails.setName("Video Gallery");
			typeDetails.setOrderLevel(1);
			typeDetailRepository.save(typeDetails);

			
		}

		return new ResponseEntity<>("Successfully saved ", HttpStatus.OK);

	}

	@Override
	public Map<String,List<TypeDetail>> getTypeDetail() {
		Map<String,List<TypeDetail>> typedetailsMap = new HashMap<String, List<TypeDetail>>();
		
		Type resourceType = typeRepository.findByTypeName("Resource Type");
		List<TypeDetail> listOfResourceTypeDetails = typeDetailRepository.findByType(resourceType);
		typedetailsMap.put("Resource Type", listOfResourceTypeDetails);
		
		Type galleryType = typeRepository.findByTypeName("Gallery Type");
		List<TypeDetail> listOfGalleryTypeDetails = typeDetailRepository.findByType(galleryType);
		typedetailsMap.put("Gallery Type", listOfGalleryTypeDetails);
		
		
		
		return typedetailsMap;
	}

	@Override
	public ResourceModel getAllResources(OAuth2Authentication oauth) {

		Map<String, String> typedetailsMap = new HashedMap<>();
		
		 for (Entry<String, List<TypeDetail>> entry : getTypeDetail().entrySet()) {
			 for (TypeDetail typeDetail : entry.getValue()) {
				 typedetailsMap.put(typeDetail.getId(), typeDetail.getName());
			}
		}
		//typedetailsMap = getTypeDetail().stream().collect(Collectors.toMap(TypeDetail::getId, TypeDetail::getName));
		SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-mm-yyyy");
		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);
		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		List<Resources> listOfResources = null;
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
			listOfResources = resourcesRepository.findAll().stream().filter(resource -> resource.getIsActive() == true)
					.collect(Collectors.toList());
		} else {
			if (isPartnerAdmin) {
				List<Account> accounts = customAccountRepository.findByPartnerIdIn(principal.getPartnerId());
				List<String> userName = new ArrayList<>();
				for (Account account : accounts) {
					userName.add(account.getUserName());
				}
				listOfResources = resourcesRepository.findBySubmittedByInAndIsActiveTrue(userName);

			} else {
				listOfResources = resourcesRepository.findBySubmittedByAndIsActiveTrue(
						SecurityContextHolder.getContext().getAuthentication().getName());
			}
		}

		// List<SuccessStoriesModel> lisSuccessStoriesModels = new ArrayList<>();

		ResourceModel resourceModel = new ResourceModel();
		List<TableDataModel> listOfTableDataModel = new ArrayList<>();
		TableDataModel tableDataModel = null;
		resourceModel.setTableColumn(Arrays.asList("sl_No#Sl. No.", "title#Title", "resourceType#Resource Type",
				"tag#Tags", "publishedBy#Published By"));
		int i = 1;
		if (!listOfResources.isEmpty()) {
			for (Resources resources : listOfResources) {
				tableDataModel = new TableDataModel();
				tableDataModel.setId(resources.getId());
				tableDataModel.setPublishedBy(resources.getSubmittedBy());
				tableDataModel.setSl_No(i);
				tableDataModel.setTitle(resources.getTitle());
				tableDataModel.setTag(resources.getTags());
				tableDataModel.setUploadedOn(simpledateformat.format(resources.getCreatedDate()));
				tableDataModel.setVideoLink(resources.getVideoLink());
				tableDataModel.setResourceType(typedetailsMap.get(resources.getResourceType()).toString());
				tableDataModel.setResourceTypeId(resources.getResourceType());
				tableDataModel.setDescription(resources.getDescription());
				tableDataModel.setIsApprove(resources.getIsApprove());
				listOfTableDataModel.add(tableDataModel);
				++i;
			}
		}
		resourceModel.setTableData(listOfTableDataModel);
		return resourceModel;
	}

	@Override
	public ResponseEntity<String> uploadFile(MultipartFile multipartfiles) {
		String filePath = null;
		if (!new File(photoIdfilepath + "resources/").exists()) {
			new File(photoIdfilepath + "resources/").mkdirs();
		}
		if (multipartfiles != null) {
			try {
				String fileNameWithDateTime = FilenameUtils.getBaseName(multipartfiles.getOriginalFilename()) + "_"
						+ new Date().getTime() + "." + FilenameUtils.getExtension(multipartfiles.getOriginalFilename());

				filePath = photoIdfilepath + "resources/" + fileNameWithDateTime;

				Files.copy(multipartfiles.getInputStream(), this.photoIdfilePathLocation.resolve(fileNameWithDateTime),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<>(filePath, HttpStatus.OK);
	}

	@Override
	public List<FilepathCaptionModel> getResourceDetails(String resourceId) {
		Resources fetchedResources = resourcesRepository.findById(resourceId);
		List<FilepathCaptionModel> listFilepathCaptionModels = new ArrayList<>();
		FilepathCaptionModel filepathCaptionModel = null;
		for (FilepathCaptionModel path : fetchedResources.getFilepathCaptionModel()) {
			filepathCaptionModel=new FilepathCaptionModel();
			filepathCaptionModel.setCaption(path.getCaption()==null ? null : path.getCaption());
			filepathCaptionModel.setFilepath(ImageConverter
					.encodingPhoto(path.getFilepath()));
			filepathCaptionModel.setPath(path.getFilepath());
			
			listFilepathCaptionModels.add(filepathCaptionModel);
		}
		return listFilepathCaptionModels;
	}

	@Override
	public ResponseEntity<String> deleteResource(String resourceId, OAuth2Authentication oauth) {
		Resources fetchedResources = resourcesRepository.findById(resourceId);
		fetchedResources.setIsActive(false);
		resourcesRepository.save(fetchedResources);
		return new ResponseEntity<>("Deleted successfully  ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> approveResource(String resourceId, OAuth2Authentication oauth) {
		Resources fetchedResources = resourcesRepository.findById(resourceId);
		fetchedResources.setIsApprove(true);
		resourcesRepository.save(fetchedResources);
		return new ResponseEntity<>("Approved successfully  ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> saveImportantLinks(ImportantLinks importantLink, OAuth2Authentication oauth) {

		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);

		//ImportantLinks importantLink= new ImportantLinks();
		Account acc = customAccountRepository
				.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		UserDetails user = (UserDetails) acc.getUserDetails();
		if (importantLink.getId() == null) {
			importantLink.setImportantLinksId(importantLinksRepository.findAll().size() + 1);
			importantLink.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			importantLink.setFirstName(user.getFirstName());
			importantLink.setCreatedDate(new Date());
			importantLink.setUpdatedDate(new Date());
			importantLink.setIsActive(true);
			importantLink.setIsApprove(false);
		} else {
			ImportantLinks fetchedImportantLinks = importantLinksRepository.findById(importantLink.getId());
			fetchedImportantLinks.setIsActive(false);
			importantLinksRepository.save(fetchedImportantLinks);
			importantLink.setImportantLinksId(resourcesRepository.findAll().size() + 1);
			importantLink.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			importantLink.setCreatedDate(new Date());
			importantLink.setUpdatedDate(new Date());
			importantLink.setIsActive(true);
			importantLink.setIsApprove(false);
		}
		importantLinksRepository.save(importantLink);
		return new ResponseEntity<>("Successfully saved ", HttpStatus.OK);
	}

	@Override
	public ResourceModel getAllImportantLinks(OAuth2Authentication oauth) {

		SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-mm-yyyy");
		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);
		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		List<ImportantLinks> listOfImportantLinks = null;
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
			listOfImportantLinks = importantLinksRepository.findAll().stream().filter(resource -> resource.getIsActive() == true)
					.collect(Collectors.toList());
		} else {
			if (isPartnerAdmin) {
				List<Account> accounts = customAccountRepository.findByPartnerIdIn(principal.getPartnerId());
				List<String> userName = new ArrayList<>();
				for (Account account : accounts) {
					userName.add(account.getUserName());
				}
				listOfImportantLinks = importantLinksRepository.findBySubmittedByInAndIsActiveTrue(userName);

			} else {
				listOfImportantLinks = importantLinksRepository.findBySubmittedByAndIsActiveTrue(
						SecurityContextHolder.getContext().getAuthentication().getName());
			}
		}

		// List<SuccessStoriesModel> lisSuccessStoriesModels = new ArrayList<>();

		ResourceModel resourceModel = new ResourceModel();
		List<TableDataModel> listOfTableDataModel = new ArrayList<>();
		TableDataModel tableDataModel = null;
		resourceModel.setTableColumn(Arrays.asList("sl_No#Sl. No.", "title#Title", "url#URL", "publishedBy#Published By"));
		int i = 1;
		if (!listOfImportantLinks.isEmpty()) {
			for (ImportantLinks importantLink : listOfImportantLinks) {
				tableDataModel = new TableDataModel();
				tableDataModel.setId(importantLink.getId());
				tableDataModel.setPublishedBy(importantLink.getSubmittedBy());
				tableDataModel.setSl_No(i);
				tableDataModel.setTitle(importantLink.getTitle());
				tableDataModel.setUrl(importantLink.getUrl());
				tableDataModel.setUploadedOn(simpledateformat.format(importantLink.getCreatedDate()));
				tableDataModel.setIsApprove(importantLink.getIsApprove());
				listOfTableDataModel.add(tableDataModel);
				++i;
			}
		}
		resourceModel.setTableData(listOfTableDataModel);
		return resourceModel;
	}

	

	@Override
	public ResponseEntity<String> deleteImportantLinks(String importantLinksId, OAuth2Authentication oauth) {
		ImportantLinks fetchedImportantLinks = importantLinksRepository.findById(importantLinksId);
		fetchedImportantLinks.setIsActive(false);
		importantLinksRepository.save(fetchedImportantLinks);
		return new ResponseEntity<>("Deleted successfully  ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> approveImportantLinks(String importantLinksId, OAuth2Authentication oauth) {
		ImportantLinks fetchedImportantLinks = importantLinksRepository.findById(importantLinksId);
		fetchedImportantLinks.setIsApprove(true);
		importantLinksRepository.save(fetchedImportantLinks);
		return new ResponseEntity<>("Approved successfully  ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> saveGallery(Gallery gallery, OAuth2Authentication oauth) {

		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);

		//Gallery gallerys = new Gallery();
		Account acc = customAccountRepository
				.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		UserDetails user = (UserDetails) acc.getUserDetails();
		if (gallery.getId() == null) {
			gallery.setGalleryId(galleryRepository.findAll().size() + 1);
			gallery.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			gallery.setFirstName(user.getFirstName());
			gallery.setCreatedDate(new Date());
			gallery.setUpdatedDate(new Date());
			gallery.setIsActive(true);
			gallery.setIsApprove(false);
		} else {
			Gallery fetchedGallerys = galleryRepository.findById(gallery.getId());
			fetchedGallerys.setIsActive(false);
			galleryRepository.save(fetchedGallerys);
			gallery.setGalleryId(galleryRepository.findAll().size() + 1);
			gallery.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			gallery.setCreatedDate(new Date());
			gallery.setUpdatedDate(new Date());
			gallery.setIsActive(true);
			gallery.setIsApprove(false);
		}
		galleryRepository.save(gallery);
		return new ResponseEntity<>("Successfully saved ", HttpStatus.OK);
	}

	@Override
	public ResourceModel getAllGallery(OAuth2Authentication oauth) {

		Map<String, String> typedetailsMap = new HashedMap<>();
		
		 for (Entry<String, List<TypeDetail>> entry : getTypeDetail().entrySet()) {
			 for (TypeDetail typeDetail : entry.getValue()) {
				 typedetailsMap.put(typeDetail.getId(), typeDetail.getName());
			}
		}
		//typedetailsMap = getTypeDetail().stream().collect(Collectors.toMap(TypeDetail::getId, TypeDetail::getName));
		SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-mm-yyyy");
		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);
		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		List<Gallery> listOfGallerys = null;
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
			listOfGallerys = galleryRepository.findAll().stream().filter(resource -> resource.getIsActive() == true)
					.collect(Collectors.toList());
		} else {
			if (isPartnerAdmin) {
				List<Account> accounts = customAccountRepository.findByPartnerIdIn(principal.getPartnerId());
				List<String> userName = new ArrayList<>();
				for (Account account : accounts) {
					userName.add(account.getUserName());
				}
				listOfGallerys = galleryRepository.findBySubmittedByInAndIsActiveTrue(userName);

			} else {
				listOfGallerys = galleryRepository.findBySubmittedByAndIsActiveTrue(
						SecurityContextHolder.getContext().getAuthentication().getName());
			}
		}

		// List<SuccessStoriesModel> lisSuccessStoriesModels = new ArrayList<>();

		ResourceModel resourceModel = new ResourceModel();
		List<TableDataModel> listOfTableDataModel = new ArrayList<>();
		TableDataModel tableDataModel = null;
		resourceModel.setTableColumn(Arrays.asList("sl_No#Sl. No.", "caption#Caption", "galleryType#Gallery Type", "publishedBy#Published By"));
		int i = 1;
		if (!listOfGallerys.isEmpty()) {
			for (Gallery gallery : listOfGallerys) {
				tableDataModel = new TableDataModel();
				tableDataModel.setId(gallery.getId());
				tableDataModel.setPublishedBy(gallery.getSubmittedBy());
				tableDataModel.setSl_No(i);
				tableDataModel.setUploadedOn(simpledateformat.format(gallery.getCreatedDate()));
				tableDataModel.setVideoLink(gallery.getVideoLink()==null ? null : gallery.getVideoLink());
				tableDataModel.setGalleryType(typedetailsMap.get(gallery.getGalleryType()).toString());
				tableDataModel.setGalleryTypeId(gallery.getGalleryType());
				tableDataModel.setCaption(gallery.getCaption());
				tableDataModel.setIsApprove(gallery.getIsApprove());
				listOfTableDataModel.add(tableDataModel);
				++i;
			}
		}
		resourceModel.setTableData(listOfTableDataModel);
		return resourceModel;
	}

	@Override
	public ResponseEntity<String> deleteGallery(String galleryId, OAuth2Authentication oauth) {
		Gallery fetchedGallerys = galleryRepository.findById(galleryId);
		fetchedGallerys.setIsActive(false);
		galleryRepository.save(fetchedGallerys);
		return new ResponseEntity<>("Deleted successfully  ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> approveGallery(String galleryId, OAuth2Authentication oauth) {
		Gallery fetchedGallerys = galleryRepository.findById(galleryId);
		fetchedGallerys.setIsApprove(true);
		galleryRepository.save(fetchedGallerys);
		return new ResponseEntity<>("Approved successfully  ", HttpStatus.OK);
	}

	@Override
	public List<FilepathCaptionModel> getGalleryDetails(String galleryId) {
		Gallery fetchedGallerys = galleryRepository.findById(galleryId);
		List<FilepathCaptionModel> listFilepathCaptionModels = new ArrayList<>();
		FilepathCaptionModel filepathCaptionModel = null;
		for (FilepathCaptionModel path : fetchedGallerys.getFilepathCaptionModel()) {
			filepathCaptionModel=new FilepathCaptionModel();
			filepathCaptionModel.setCaption(path.getCaption()==null ? null : path.getCaption());
			filepathCaptionModel.setFilepath(ImageConverter
					.encodingPhoto(path.getFilepath()));
			filepathCaptionModel.setPath(path.getFilepath());
			
			listFilepathCaptionModels.add(filepathCaptionModel);
		}
		return listFilepathCaptionModels;
	}

	@Override
	public ResponseEntity<String> saveWhatsNew(WhatsNew whatsNew, OAuth2Authentication oauth) {

		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);

		//Gallery gallerys = new Gallery();
		Account acc = customAccountRepository
				.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		UserDetails user = (UserDetails) acc.getUserDetails();
		if (whatsNew.getId() == null) {
			whatsNew.setWhatsnewId(whatsNewRepository.findAll().size() + 1);
			whatsNew.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			whatsNew.setFirstName(user.getFirstName());
			whatsNew.setCreatedDate(new Date());
			whatsNew.setUpdatedDate(new Date());
			whatsNew.setIsActive(true);
			whatsNew.setIsApprove(false);
		} else {
			
			WhatsNew fetchedWhatsNew = whatsNewRepository.findById(whatsNew.getId());
			fetchedWhatsNew.setIsActive(false);
			whatsNewRepository.save(fetchedWhatsNew);
			whatsNew.setWhatsnewId(whatsNewRepository.findAll().size() + 1);
			whatsNew.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			whatsNew.setCreatedDate(new Date());
			whatsNew.setUpdatedDate(new Date());
			whatsNew.setIsActive(true);
			whatsNew.setIsApprove(false);
		}
		whatsNewRepository.save(whatsNew);
		return new ResponseEntity<>("Successfully saved ", HttpStatus.OK);
	}

	@Override
	public ResourceModel getAllWhatsNew(OAuth2Authentication oauth) {

		//Map<String, String> typedetailsMap = new HashedMap<>();
		
		
		//typedetailsMap = getTypeDetail().stream().collect(Collectors.toMap(TypeDetail::getId, TypeDetail::getName));
		SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-mm-yyyy");
		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);
		List<Designation> desig = designationRepository.findByIdIn(new ArrayList<String>(principal.getRoleIds()));
		List<WhatsNew> listOfWhatsNew = null;
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
			listOfWhatsNew = whatsNewRepository.findAll().stream().filter(resource -> resource.getIsActive() == true)
					.collect(Collectors.toList());
		} else {
			if (isPartnerAdmin) {
				List<Account> accounts = customAccountRepository.findByPartnerIdIn(principal.getPartnerId());
				List<String> userName = new ArrayList<>();
				for (Account account : accounts) {
					userName.add(account.getUserName());
				}
				listOfWhatsNew = whatsNewRepository.findBySubmittedByInAndIsActiveTrue(userName);

			} else {
				listOfWhatsNew = whatsNewRepository.findBySubmittedByAndIsActiveTrue(
						SecurityContextHolder.getContext().getAuthentication().getName());
			}
		}

		// List<SuccessStoriesModel> lisSuccessStoriesModels = new ArrayList<>();

		ResourceModel resourceModel = new ResourceModel();
		List<TableDataModel> listOfTableDataModel = new ArrayList<>();
		TableDataModel tableDataModel = null;
		resourceModel.setTableColumn(Arrays.asList("sl_No#Sl. No.","title#Title", "link#Link", "publishedBy#Published By"));
		int i = 1;
		if (!listOfWhatsNew.isEmpty()) {
			for (WhatsNew whatsNew : listOfWhatsNew) {
				tableDataModel = new TableDataModel();
				tableDataModel.setId(whatsNew.getId());
				tableDataModel.setPublishedBy(whatsNew.getSubmittedBy());
				tableDataModel.setSl_No(i);
				tableDataModel.setUploadedOn(simpledateformat.format(whatsNew.getCreatedDate()));
				tableDataModel.setTitle(whatsNew.getTitle());
				tableDataModel.setDescription(whatsNew.getDescription());
				tableDataModel.setUrl(whatsNew.getLink());
				tableDataModel.setIsApprove(whatsNew.getIsApprove());
				listOfTableDataModel.add(tableDataModel);
				++i;
			}
		}
		resourceModel.setTableData(listOfTableDataModel);
		return resourceModel;
	}

	@Override
	public List<FilepathCaptionModel> getWhatsNewDetails(String whatsNewId) {
		WhatsNew fetchedWhatsNew = whatsNewRepository.findById(whatsNewId);
		List<FilepathCaptionModel> listFilepathCaptionModels = new ArrayList<>();
		FilepathCaptionModel filepathCaptionModel = null;
		for (FilepathCaptionModel path : fetchedWhatsNew.getFilepathCaptionModel()) {
			filepathCaptionModel=new FilepathCaptionModel();
			filepathCaptionModel.setCaption(path.getCaption()==null ? null : path.getCaption());
			filepathCaptionModel.setFilepath(ImageConverter
					.encodingPhoto(path.getFilepath()));
			filepathCaptionModel.setPath(path.getFilepath());
			
			listFilepathCaptionModels.add(filepathCaptionModel);
		}
		return listFilepathCaptionModels;
	}

	@Override
	public ResponseEntity<String> deleteWhatsNew(String whatsNewId, OAuth2Authentication oauth) {
		WhatsNew fetchedWhatsNew = whatsNewRepository.findById(whatsNewId);
		fetchedWhatsNew.setIsActive(false);
		whatsNewRepository.save(fetchedWhatsNew);
		return new ResponseEntity<>("Deleted successfully  ", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> approveWhatsNew(String whatsNewId, OAuth2Authentication oauth) {
		WhatsNew fetchedWhatsNew = whatsNewRepository.findById(whatsNewId);
		fetchedWhatsNew.setIsApprove(true);
		whatsNewRepository.save(fetchedWhatsNew);
		return new ResponseEntity<>("Approved successfully  ", HttpStatus.OK);
	}

	@Override
	public List<WhatsNew> getAllApprovedWhatsNew() {
		return whatsNewRepository.findByIsActiveTrueAndIsApproveTrue();
	}

	@Override
	public List<Gallery> getAllApprovedGallery() {
		return galleryRepository.findByIsActiveTrueAndIsApproveTrue();
	}

	@Override
	public List<ImportantLinks> getAllApprovedImportantLinks() {
		return importantLinksRepository.findByIsActiveTrueAndIsApproveTrue();
	}

	@Override
	public Map<String,List<Resources>> getAllApprovedResource() {
		
		Map<String,List<Resources>> allApprovedMap = new HashMap<>();
		List<Resources> listOfResources = null;
		
		Map<String, String> typedetailsMap = new HashedMap<>();
			 for (TypeDetail typeDetail : getTypeDetail().get("Resource Type")) {
				 typedetailsMap.put(typeDetail.getId(), typeDetail.getName());
				 //allApprovedMap.put(typeDetail.getName(), listOfResources);
			}
			 
		 List<Resources> listOfFetchedResource = resourcesRepository.findByIsActiveTrueAndIsApproveTrue();
		for (Resources resources : listOfFetchedResource) {
			if (allApprovedMap.containsKey(typedetailsMap.get(resources.getResourceType()))) {
				allApprovedMap.get(typedetailsMap.get(resources.getResourceType())).add(resources);

			} else {
				listOfResources = new ArrayList<>();
				listOfResources.add(resources);
				allApprovedMap.put(typedetailsMap.get(resources.getResourceType()), listOfResources);
			}

		}
		
		
		 for (Entry<String, String> entry : typedetailsMap.entrySet()) {
			if(!allApprovedMap.containsKey(entry.getValue())) {
				allApprovedMap.put(entry.getValue(), new ArrayList<>());
			}
		}
		 
		 
		 return allApprovedMap;
	}

	@Override
	public List<SuccessStories> getAllApprovedSuccessStories() {
		return successStoriesRepository.findByIsActiveTrueAndIsApproveTrue();
	}

	@Override
	public Map<String,List<ImageModel>> getAllApprovedGalleryImage() {
		
		Map<String,List<ImageModel>> allApprovedMap = new HashMap<>();
		List<ImageModel> listOfImageModelImage = new ArrayList<>();
		List<ImageModel> listOfImageModelVideo = new ArrayList<>();
		ImageModel imageModel = null;
		List<Gallery> listOfFetchedGallery = galleryRepository.findByIsActiveTrueAndIsApproveTrue();
		
		for (Gallery gallery : listOfFetchedGallery) {
			if (gallery.getVideoLink() == null) {
				for (FilepathCaptionModel filepathCaptionModel : gallery.getFilepathCaptionModel()) {
					imageModel = new ImageModel();
					imageModel.setCaption(gallery.getCaption());
					imageModel.setGalleryTypeId(gallery.getGalleryType());
					imageModel.setFileName(filepathCaptionModel.getFilepath().split("/")[3]);
					imageModel.setUrl(
							env.getProperty("gallery.image.filepath") + filepathCaptionModel.getFilepath() + "&inline=false");
					listOfImageModelImage.add(imageModel);
				}
			}
			else {
				imageModel = new ImageModel();
				imageModel.setCaption(gallery.getCaption());
				imageModel.setVideoLink(gallery.getVideoLink());
				imageModel.setGalleryTypeId(gallery.getGalleryType());
				listOfImageModelVideo.add(imageModel);
				
			}

		}
		
		Map<String, String> typedetailsMap = new HashedMap<>();
		 for (TypeDetail typeDetail : getTypeDetail().get("Gallery Type")) {
			 typedetailsMap.put(typeDetail.getId(), typeDetail.getName());
			 //allApprovedMap.put(typeDetail.getName(), listOfResources);
		}
		 
		 
		if (!listOfImageModelImage.isEmpty() && listOfImageModelImage != null) {
			if (listOfImageModelImage.get(0) != null) {
				allApprovedMap.put(typedetailsMap.get(listOfImageModelImage.get(0).getGalleryTypeId()),
						listOfImageModelImage);
			}
		}
		
		if (!listOfImageModelVideo.isEmpty() && listOfImageModelVideo != null) {
			if (listOfImageModelVideo.get(0) != null) {
				allApprovedMap.put(typedetailsMap.get(listOfImageModelVideo.get(0).getGalleryTypeId()),
						listOfImageModelVideo);
			}
		}
	
		 for (Entry<String, String> entry : typedetailsMap.entrySet()) {
				if(!allApprovedMap.containsKey(entry.getValue())) {
					allApprovedMap.put(entry.getValue(), new ArrayList<>());
				}
			}
		
		 
		 return allApprovedMap;
		
	
	}

}
