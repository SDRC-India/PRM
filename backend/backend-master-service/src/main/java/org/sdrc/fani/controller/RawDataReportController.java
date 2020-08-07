package org.sdrc.fani.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.fani.service.RawDataReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import in.co.sdrc.sdrcdatacollector.document.EnginesForm;
import in.co.sdrc.sdrcdatacollector.models.MessageModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author subham
 *
 */
@RestController
@Slf4j
public class RawDataReportController {
	@Autowired
	private RawDataReportService rawDataReportService;

	@RequestMapping(value = "/exportRawData")
	@ResponseBody
	public ResponseEntity<MessageModel> exportRawaData(@RequestParam("formId") Integer formId, String startDate,
			String endDate, Principal principal, OAuth2Authentication auth) {

		return rawDataReportService.exportRawaData(formId, startDate, endDate, principal, auth);

	}

	@RequestMapping(value = "/getReportForms")
	@ResponseBody
	@PreAuthorize("hasAuthority('DOWNLOAD_RAWDATA_REPORT')")
	public List<EnginesForm> getRawDataReportAccessForms(OAuth2Authentication auth) {
		return rawDataReportService.getRawDataReportAccessForms(auth);
	}

	@RequestMapping(value = "/downloadReport", method = RequestMethod.POST)
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
			new File(fileName).delete();

		} catch (IOException e) {
			log.error("error-while downloading with payload : {}", name, e);
			throw new RuntimeException();
		}
	}

	@RequestMapping(value = "/downloadImage", method = RequestMethod.GET)
	public void downLoadAttachments(@RequestParam("path") String name, HttpServletResponse response)
			throws IOException {

		byte[] decodedBytes = Base64.getUrlDecoder().decode(name);
		name = new String(decodedBytes);

		name = name.replace("\\", "//");

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

		} catch (IOException e) {
			log.error("error-while downloading image with payload : {}", name, e);
			throw new RuntimeException();
		}

	}
	
	@GetMapping("/getRawDataReport")
	ResponseEntity<String> getRawData(@RequestParam(value = "formId") Integer formId,OAuth2Authentication oauth) throws Exception {
		return rawDataReportService.getRawDataReport(formId,oauth);
	}
	
	

}
