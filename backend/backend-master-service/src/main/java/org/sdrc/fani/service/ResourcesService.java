package org.sdrc.fani.service;

import java.util.List;
import java.util.Map;

import org.sdrc.fani.collections.Gallery;
import org.sdrc.fani.collections.ImportantLinks;
import org.sdrc.fani.collections.Resources;
import org.sdrc.fani.collections.SuccessStories;
import org.sdrc.fani.collections.WhatsNew;
import org.sdrc.fani.models.FilepathCaptionModel;
import org.sdrc.fani.models.ImageModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.multipart.MultipartFile;

import in.co.sdrc.sdrcdatacollector.document.TypeDetail;

public interface ResourcesService {

	ResponseEntity<String> saveResources(ResourceModel resourceModel, OAuth2Authentication oauth);

	ResponseEntity<String> importResourceType();

	Map<String, List<TypeDetail>> getTypeDetail();

	ResourceModel getAllResources(OAuth2Authentication oauth);

	ResponseEntity<String> uploadFile(MultipartFile multipartfiles);

	List<FilepathCaptionModel> getResourceDetails(String resourceId);

	ResponseEntity<String> deleteResource(String resourceId, OAuth2Authentication oauth);

	ResponseEntity<String> approveResource(String resourceId, OAuth2Authentication oauth);

	ResponseEntity<String> saveImportantLinks(ImportantLinks importantLinks, OAuth2Authentication oauth);

	ResourceModel getAllImportantLinks(OAuth2Authentication oauth);

	ResponseEntity<String> deleteImportantLinks(String importantLinksId, OAuth2Authentication oauth);

	ResponseEntity<String> approveImportantLinks(String importantLinksId, OAuth2Authentication oauth);

	ResponseEntity<String> saveGallery(Gallery gallery, OAuth2Authentication oauth);

	ResourceModel getAllGallery(OAuth2Authentication oauth);

	List<FilepathCaptionModel> getGalleryDetails(String galleryId);

	ResponseEntity<String> deleteGallery(String galleryId, OAuth2Authentication oauth);

	ResponseEntity<String> approveGallery(String galleryId, OAuth2Authentication oauth);

	ResponseEntity<String> saveWhatsNew(WhatsNew whatsNew, OAuth2Authentication oauth);

	ResourceModel getAllWhatsNew(OAuth2Authentication oauth);

	List<FilepathCaptionModel> getWhatsNewDetails(String whatsNewId);

	ResponseEntity<String> deleteWhatsNew(String whatsNewId, OAuth2Authentication oauth);

	ResponseEntity<String> approveWhatsNew(String whatsNewId, OAuth2Authentication oauth);
	
	List<WhatsNew> getAllApprovedWhatsNew();
	List<Gallery> getAllApprovedGallery();
	List<ImportantLinks> getAllApprovedImportantLinks();
	Map<String,List<Resources>> getAllApprovedResource();
	List<SuccessStories> getAllApprovedSuccessStories();

	Map<String,List<ImageModel>> getAllApprovedGalleryImage();

}
