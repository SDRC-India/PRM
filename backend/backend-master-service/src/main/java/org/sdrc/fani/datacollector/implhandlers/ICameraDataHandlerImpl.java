package org.sdrc.fani.datacollector.implhandlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.sdrc.fani.models.FileModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import in.co.sdrc.sdrcdatacollector.handlers.ICameraAndAttachmentsDataHandler;
import in.co.sdrc.sdrcdatacollector.models.DataModel;
import in.co.sdrc.sdrcdatacollector.models.FormAttachmentsModel;
import in.co.sdrc.sdrcdatacollector.models.QuestionModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 *
 */
@Service
@Slf4j
public class ICameraDataHandlerImpl implements ICameraAndAttachmentsDataHandler {

	@Autowired
	private ConfigurableEnvironment configurableEnvironment;

	public QuestionModel readExternal(QuestionModel model, DataModel dataModel, Map<String, Object> paramKeyValMap) {

		try {
			Map<String, List<FormAttachmentsModel>> attachmentsMap = dataModel.getAttachments();

			List<String> base64Values = null;

			List<FileModel> fileModelList = null;

			List<String> localDevicePaths = null;

			if (attachmentsMap != null) {

				List<FormAttachmentsModel> attachments = attachmentsMap.get(model.getColumnName());

				if (attachments != null && !attachments.isEmpty()) {

					base64Values = new ArrayList<>();
					localDevicePaths = new ArrayList<>();
					fileModelList = new ArrayList<>();

					if (paramKeyValMap.get("file") == null) {
						for (FormAttachmentsModel faModel : attachments) {

							String filePath = faModel.getFilePath();
							localDevicePaths.add(faModel.getLocalDevicePath());
							String linkAddress = configurableEnvironment.getProperty("image.download.path");
							linkAddress = linkAddress
									.concat("?path=" + Base64.getUrlEncoder().encodeToString(filePath.getBytes()));
							base64Values.add(linkAddress);

						}
						model.setValue(localDevicePaths);
						model.setAttachmentsInBase64(base64Values);
					} else {

						for (FormAttachmentsModel faModel : attachments) {

							String filePath = faModel.getFilePath();
							localDevicePaths.add(faModel.getLocalDevicePath());
							FileModel fileModel = new FileModel();
							String linkAddress = configurableEnvironment.getProperty("image.download.path");
							linkAddress = linkAddress
									.concat("?path=" + Base64.getUrlEncoder().encodeToString(filePath.getBytes()));
							fileModel.setBase64(linkAddress);
							fileModel.setColumnName(faModel.getColumnName());
							fileModel.setFileName(
									faModel.getOriginalName().concat(".").concat(faModel.getFileExtension()));
							fileModel.setFileSize(faModel.getFileSize());
							fileModel.setFileType(faModel.getFileExtension());
							fileModelList.add(fileModel);
						}
						model.setAttachmentsInBase64(fileModelList);
					}

				}

			}

			return model;

		} catch (Exception e) {
			log.error("Action while generating base 64 value for camera {}", e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * 
	 * @param file- convert file to base64 string
	 * @return - base64 converted value
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private String encodeImageToBase64Binary(String filePath, String extension) throws IOException {

		String encodedString = "data:image/".concat(extension).concat(";base64,");
		byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
		encodedString = encodedString.concat(java.util.Base64.getEncoder().encodeToString(fileContent));
		return encodedString;

	}

	private String encodeFileToBase64Binary(String filePath, String extension) throws IOException {

		String encodedString = "base64:";
		byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
		encodedString = java.util.Base64.getEncoder().encodeToString(fileContent);
		return encodedString;

	}

}
