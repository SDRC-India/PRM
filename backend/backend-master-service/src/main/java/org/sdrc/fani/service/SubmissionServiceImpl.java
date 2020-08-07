package org.sdrc.fani.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.sdrc.fani.collections.AllChecklistFormData;
import org.sdrc.fani.collections.TimePeriod;
import org.sdrc.fani.models.ChecklistSubmissionStatus;
import org.sdrc.fani.models.UserModel;
import org.sdrc.fani.rabbitmq.CollectionChannel;
import org.sdrc.fani.repositories.AllChecklistFormDataRepository;
import org.sdrc.fani.repositories.CustomAccountRepository;
import org.sdrc.fani.repositories.TimePeriodRepository;
import org.sdrc.fani.util.TokenInfoExtracter;
import org.sdrc.usermgmt.mongodb.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.integration.support.MessageBuilder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.ReceiveEventModel;
import in.co.sdrc.sdrcdatacollector.mongorepositories.EngineFormRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * @author subham
 *
 */
@Service
@Transactional
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

	@Autowired
	private TokenInfoExtracter tokenInfoExtracter;

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	@Autowired
	private AllChecklistFormDataRepository allChecklistFormDataRepository;

	@Autowired
	EngineFormRepository engineFormRepository;
	
	@Autowired
	private CollectionChannel collectionChannel;
	
	@Autowired
	@Qualifier("customAccountRepository")
	private CustomAccountRepository customAccountRepository;
	
	@Override
	public ResponseEntity<String> saveSubmission(ReceiveEventModel event, OAuth2Authentication oauth) {

		Gson gson = new Gson();
//		UserModel principal = tokenInfoExtracter.getUserModelInfo(oauth);
		UserModel principal = tokenInfoExtracter.getUserModelWithOutRoleDesig(oauth);
		try {
			Account account = customAccountRepository.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName());
			if(!account.isEnabled()) {
				
				return new ResponseEntity<String>(gson.toJson("आपकी आईडी अक्षम कर दी गई है, कृपया अधिक जानकारी के लिए अपने व्यवस्थापक से संपर्क करें"), HttpStatus.PRECONDITION_FAILED);
			}else {
			AllChecklistFormData dataSubmit = new AllChecklistFormData();
			dataSubmit.setCreatedDate(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(event.getCreatedDate()));
			dataSubmit.setUpdatedDate(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(event.getUpdatedDate()));
			dataSubmit.setSyncDate(new Date());
			dataSubmit.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
			dataSubmit.setFormId(event.getFormId());
			dataSubmit.setUserId(principal.getUserId());
			dataSubmit.setData(event.getSubmissionData());
			dataSubmit.setUniqueId(event.getUniqueId());
			dataSubmit.setUniqueName(event.getUniqueName());
			dataSubmit.setSubmittedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			dataSubmit.setLatest(true);
			dataSubmit.setAttachmentCount(event.getAttachmentCount());

			dataSubmit.setIsAggregated(false);
			
//			TimePeriod lastTp = timePeriodRepository.findTop1ByPeriodicityOrderByTimePeriodIdDesc("0");
//			dataSubmit.setTimePeriod(lastTp);
			if (event.getAttachmentCount() != 0) {
				dataSubmit.setChecklistSubmissionStatus(ChecklistSubmissionStatus.INCOMEPLETE);
			}

			
			/**
			 * check for same unique id and formid whether any submission is exist which is fresh data that is reject=false
			 * if exist than make the previous data as invalid 
			 */
//			List<AllChecklistFormData> dupData = allChecklistFormDataRepository.findByFormIdAndUniqueIdAndRejectedFalse(dataSubmit.getFormId(),dataSubmit.getUniqueId());
//			
//			dupData.forEach(d->d.setIsValid(false));
//			allChecklistFormDataRepository.save(dupData);
			
			
//		allChecklistFormDataRepository.save(dataSubmit);
			collectionChannel.dataSubmissionChannel().send(MessageBuilder.withPayload(dataSubmit).build());

//			return new ResponseEntity<String>(gson.toJson(dataSubmit.getId()), HttpStatus.OK);
			return new ResponseEntity<String>(gson.toJson(dataSubmit.getUniqueId()), HttpStatus.OK);
		}

		} catch (Exception e) {
			log.error("Action : while submitting new data by with payload {},{},{}",SecurityContextHolder.getContext().getAuthentication().getName(),event,e);
			throw new RuntimeException("custom error", e);
		}

	}

	@Override
	@Transactional
	public String uploadFiles(MultipartFile file, FormAttachmentsModel fileModel) throws Exception {
           EnginesForm form =engineFormRepository.findByFormId(fileModel.getFormId());
		System.out.println("upload file called");
		System.out.println(fileModel);

		List<FormAttachmentsModel> modelList = new ArrayList<FormAttachmentsModel>();

		FormAttachmentsModel model = new FormAttachmentsModel();

		AllChecklistFormData submissionData = allChecklistFormDataRepository
				.findByIdAndFormId(fileModel.getSubmissionId(), fileModel.getFormId());

		Map<String, List<FormAttachmentsModel>> attachments = submissionData.getAttachments();

		System.out.println("attachments=== " + attachments);
		String filePath = getFilePath(file,
				form.getName(),
				fileModel.getFileExtension(), fileModel.getOriginalName(),
				configurableEnvironment.getProperty("upload.file.path"));

		model.setFilePath(filePath);
		model.setFileSize(file.getSize());
		model.setOriginalName(fileModel.getOriginalName().substring(0, fileModel.getOriginalName().lastIndexOf('.')));
		model.setFileExtension(fileModel.getFileExtension());
		model.setColumnName(fileModel.getColumnName());
		model.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
		model.setFormId(fileModel.getFormId());
		model.setLocalDevicePath(fileModel.getLocalDevicePath());

		if (attachments == null) {
			attachments = new HashMap<>();
			modelList.add(model);
			attachments.put(fileModel.getColumnName(), modelList);
		} else {
			List<FormAttachmentsModel> list = attachments.get(fileModel.getColumnName());

			if (list == null) {
				list = new ArrayList<>();
				list.add(model);
				attachments.put(fileModel.getColumnName(), list);
			} else {
				list.add(model);
				attachments.put(fileModel.getColumnName(), list);
			}

		}

		/**
		 * get all the column from attachments map and count the number of value
		 * present
		 */

		Integer count = 0;
		for (Entry<String, List<FormAttachmentsModel>> entry : attachments.entrySet()) {

			int size = attachments.get(entry.getKey()).size();
			count = count + size;
		}
		// System.out.println("count============= "+count);
		if (submissionData.getAttachmentCount() == count) {
			submissionData.setChecklistSubmissionStatus(ChecklistSubmissionStatus.COMPLETE);
		}
		submissionData.setAttachments(attachments);

		allChecklistFormDataRepository.save(submissionData);

		return new Gson().toJson("success");

	}

	/*
	 * it save the file in hard disk and return the complete file path
	 */
	/*
	 * it save the file in hard disk and return the complete file path
	 */
	public String getFilePath(MultipartFile file, String formName, String extension, String originalFileName,
			String dir) {
		String path = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			for (int readNum; (readNum = bis.read(file.getBytes())) != -1;) {
				bos.write(file.getBytes(), 0, readNum);
			}

			byte[] getFileBytes = bos.toByteArray();

			dir = dir + formName.concat("/");

			File filePath = new File(dir);

			/*
			 * make directory if doesn't exist
			 */
			if (!filePath.exists())
				filePath.mkdirs();

			String name = originalFileName.substring(0, originalFileName.lastIndexOf('.'))
					+ new SimpleDateFormat("ddMMyyyyHHmmssSSSS").format(new Date()).concat(".") + extension;

			path = dir + name;

			FileOutputStream fos = new FileOutputStream(path);
			fos.write(getFileBytes);
			fos.flush();
			fos.close();

		} catch (Exception e) {
			log.error("Action : While uploading file formName {}", formName, e);
			throw new RuntimeException(e);
		}

		return path;

	}

}
