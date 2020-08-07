package org.sdrc.fani.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.fani.collections.Gallery;
import org.sdrc.fani.collections.ImportantLinks;
import org.sdrc.fani.collections.Resources;
import org.sdrc.fani.collections.SuccessStories;
import org.sdrc.fani.collections.WhatsNew;
import org.sdrc.fani.models.FilepathCaptionModel;
import org.sdrc.fani.models.ImageModel;
import org.sdrc.fani.service.ResourceModel;
import org.sdrc.fani.service.ResourcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.co.sdrc.sdrcdatacollector.document.TypeDetail;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ResourceController {

	@Autowired
	private ResourcesService resourcesService;

	@ResponseBody
	@RequestMapping(value = "/saveResource")
	public ResponseEntity<String> SaveResource(@RequestBody ResourceModel resourceModel, OAuth2Authentication oauth) {
		return resourcesService.saveResources(resourceModel, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/saveTypeTypeDetails")
	ResponseEntity<String> importType() {
		return resourcesService.importResourceType();
	}

	@ResponseBody
	@RequestMapping(value = "/getResourceTypeDetails")
	Map<String, List<TypeDetail>> getResourceTypeDetails() {
		return resourcesService.getTypeDetail();
	}

	@ResponseBody
	@RequestMapping(value = "/getAllResources")
	public ResourceModel getAllResources(OAuth2Authentication oauth) {
		return resourcesService.getAllResources(oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/resourceUploadFile")
	public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file) {
		return resourcesService.uploadFile(file);

	}

	@ResponseBody
	@RequestMapping(value = "/getResourceDetails")
	public List<FilepathCaptionModel> getResourceDetails(@RequestParam("resourceId") String resourceId) {
		return resourcesService.getResourceDetails(resourceId);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteResource")
	public ResponseEntity<String> deleteResource(@RequestParam("resourceId") String resourceId,
			OAuth2Authentication oauth) {
		return resourcesService.deleteResource(resourceId, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/approveResource")
	public ResponseEntity<String> approveResource(@RequestParam("resourceId") String resourceId,
			OAuth2Authentication oauth) {
		return resourcesService.approveResource(resourceId, oauth);
	}

	@RequestMapping(value = "/downloadFile", method = RequestMethod.POST)
	public void downLoad(@RequestParam("fileName") String name, HttpServletResponse response) throws IOException {

		InputStream inputStream;
		try {
			String fileName = name.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%2C", ",")
					.replaceAll("\\+", " ").replaceAll("%20", " ").replaceAll("%26", "&").replaceAll("%5C", "/");
			inputStream = new FileInputStream(fileName);
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", new java.io.File(fileName).getName());
			response.setHeader(headerKey, headerValue);
			response.setContentType("application/octet-stream"); // for all file
																	// type
			ServletOutputStream outputStream = response.getOutputStream();
			FileCopyUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();
			// new File(fileName).delete();

		} catch (IOException e) {
			log.error("error-while downloading with payload : {}", name, e);
			throw new RuntimeException();
		}
	}

	@ResponseBody
	@RequestMapping(value = "/saveImportantLinks")
	public ResponseEntity<String> saveImportantLinks(@RequestBody ImportantLinks importantLinks,
			OAuth2Authentication oauth) {
		return resourcesService.saveImportantLinks(importantLinks, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/getAllImportantLinks")
	public ResourceModel getAllImportantLinks(OAuth2Authentication oauth) {
		return resourcesService.getAllImportantLinks(oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteImportantLinks")
	public ResponseEntity<String> deleteImportantLinks(@RequestParam("importantLinksId") String importantLinksId,
			OAuth2Authentication oauth) {
		return resourcesService.deleteImportantLinks(importantLinksId, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/approveImportantLinks")
	public ResponseEntity<String> approveImportantLinks(@RequestParam("importantLinksId") String importantLinksId,
			OAuth2Authentication oauth) {
		return resourcesService.approveImportantLinks(importantLinksId, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/saveGallery")
	public ResponseEntity<String> saveGallery(@RequestBody Gallery gallery, OAuth2Authentication oauth) {
		return resourcesService.saveGallery(gallery, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/getAllGallery")
	public ResourceModel getAllGallery(OAuth2Authentication oauth) {
		return resourcesService.getAllGallery(oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteGallery")
	public ResponseEntity<String> deleteGallery(@RequestParam("galleryId") String galleryId,
			OAuth2Authentication oauth) {
		return resourcesService.deleteGallery(galleryId, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/approveGallery")
	public ResponseEntity<String> approveGallery(@RequestParam("galleryId") String galleryId,
			OAuth2Authentication oauth) {
		return resourcesService.approveGallery(galleryId, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/getGalleryDetails")
	public List<FilepathCaptionModel> getGalleryDetails(@RequestParam("galleryId") String galleryId) {
		return resourcesService.getGalleryDetails(galleryId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/saveWhatsNew")
	public ResponseEntity<String> saveWhatsNew(@RequestBody WhatsNew whatsNew, OAuth2Authentication oauth) {
		/*WhatsNew whatsNew=new WhatsNew();
		whatsNew.setTitle("Cyclone Fani");
		whatsNew.setDescription("6 things you should know about this severe cyclone");
		whatsNew.setLink("https://economictimes.indiatimes.com/news/politics-and-nation/cyclone-fani-six-things-you-should-know/articleshow/69151302.cms");*/
		return resourcesService.saveWhatsNew(whatsNew, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/getAllWhatsNew")
	public ResourceModel getAlWhatsNew(OAuth2Authentication oauth) {
		return resourcesService.getAllWhatsNew(oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteWhatsNew")
	public ResponseEntity<String> deleteWhatsNew(@RequestParam("whatsNewId") String whatsNewId,
			OAuth2Authentication oauth) {
		return resourcesService.deleteWhatsNew(whatsNewId, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/approveWhatsNew")
	public ResponseEntity<String> approveWhatsNew(@RequestParam("whatsNewId") String whatsNewId,
			OAuth2Authentication oauth) {
		return resourcesService.approveWhatsNew(whatsNewId, oauth);
	}

	@ResponseBody
	@RequestMapping(value = "/getWhatsNewDetails")
	public List<FilepathCaptionModel> getWhatsNewDetails(@RequestParam("whatsNewId") String whatsNewId
			) {
		return resourcesService.getWhatsNewDetails(whatsNewId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/getAllApprovedWhatsNew")
	public List<WhatsNew> getAllApprovedWhatsNew() {
		return resourcesService.getAllApprovedWhatsNew();
	}
	
	@ResponseBody
	@RequestMapping(value = "/getAllApprovedGallery")
	public List<Gallery> getAllApprovedGallery() {
		return resourcesService.getAllApprovedGallery();
	}
	
	@ResponseBody
	@RequestMapping(value = "/getAllApprovedImportantLinks")
	public List<ImportantLinks>  getAllApprovedImportantLinks() {
		return resourcesService.getAllApprovedImportantLinks();
	}
	
	@ResponseBody
	@RequestMapping(value = "/getAllApprovedResources")
	public Map<String,List<Resources>> getAllApprovedResource() {
		return resourcesService.getAllApprovedResource();
	}
	
	@ResponseBody
	@RequestMapping(value = "/getAllApprovedSuccessStories")
	public List<SuccessStories> getAllApprovedSuccessStories() {
		return resourcesService.getAllApprovedSuccessStories();
	}
	
	@RequestMapping(value = "/downloadAllFile", method = RequestMethod.GET)
	public void downloadAllFile(@RequestParam("fileName") String fileName, HttpServletResponse response) throws IOException {

		InputStream inputStream;
		try {
			String fileNames = fileName.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%2C", ",")
					.replaceAll("\\+", " ").replaceAll("%20", " ").replaceAll("%26", "&").replaceAll("%5C", "/");
			inputStream = new FileInputStream(fileNames);
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", new java.io.File(fileNames).getName());
			response.setHeader(headerKey, headerValue);
			response.setContentType("application/octet-stream"); // for all file
																	// type
			ServletOutputStream outputStream = response.getOutputStream();
			FileCopyUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();
			// new File(fileName).delete();

		} catch (IOException e) {
			log.error("error-while downloading with payload : {}", fileName, e);
			throw new RuntimeException();
		}
	}
	
	@RequestMapping(value = "/downloadCmsDoc", method = RequestMethod.GET)
	public void downLoad(@RequestParam("fileName") String names,
			HttpServletResponse response, @RequestParam(value="inline", required=false) Boolean inline) throws IOException {

		InputStream inputStream;
		String fileName = "";

		try {
			log.info(fileName);
			fileName = names.trim().replaceAll("%3A", ":").replaceAll("%2F", "/")
							.replaceAll("%5C", "/").replaceAll("%2C", ",").replaceAll("\\\\", "/")
							.replaceAll("%20", " ").replaceAll("%26", "&")
							.replaceAll("\\+", " ").replaceAll("%22", "").replaceAll("%3F", "?").replaceAll("%3D", "=");
			
			inputStream = new FileInputStream(fileName);
			String headerKey = "Content-Disposition";
			String headerValue = "";
			
			if(inline!=null && inline) {
				headerValue= String.format("inline; filename=\"%s\"",
						new java.io.File(fileName).getName());
				response.setContentType("application/pdf"); // for pdf file
				// type
			}else {
				headerValue= String.format("attachment; filename=\"%s\"",
						new java.io.File(fileName).getName());
				response.setContentType("application/octet-stream"); // for all file
				// type
			}
				
			response.setHeader(headerKey, headerValue);
			
			ServletOutputStream outputStream = response.getOutputStream();
			FileCopyUtils.copy(inputStream, outputStream);
			inputStream.close();
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException",  e);
		} catch (IOException e) {
			log.error("IOException",  e);
		}
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/getAllApprovedGalleryImage")
	public Map<String,List<ImageModel>> getAllApprovedGalleryImage() {
		return resourcesService.getAllApprovedGalleryImage();
	}

}
