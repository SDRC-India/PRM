package org.sdrc.pmr.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.pmr.model.ReportChartModel;
import org.sdrc.pmr.service.RawDataReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Debiprasad
 *
 */
@RestController
@Slf4j
public class RawDataReportController {
	@Autowired
	private RawDataReportService rawDataReportService;

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

	/*@GetMapping("/getRawDataReport")
	ResponseEntity<String> getRawData(@RequestParam(value = "formId") Integer formId, OAuth2Authentication oauth)
			throws Exception {
		return rawDataReportService.getRawDataReport(formId, oauth);
	}*/
	
	@PostMapping(value = "/getRawDataReport")
	public ResponseEntity<InputStreamResource> 
	downLoad(@RequestParam(value = "formId") Integer formId,@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate, OAuth2Authentication oauth) throws Exception {
//	       userManagementService.downLoadFile((userManagementService.downLoadBulkTemplate()), response, inline);
		String filePath = "";
		try {
			filePath = rawDataReportService.getRawDataReport(formId,startDate,endDate, oauth);
			
			if(filePath == null) {
				return  ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
			}
			
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			//file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@PostMapping(value = "/getAreaAndUserWiseRawDataReport")
	public ResponseEntity<InputStreamResource> 
	getAreaAndUserWiseRawDataReport(
			@RequestParam(value = "reportType") Integer reportType,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			OAuth2Authentication oauth) throws Exception {
		String filePath = "";
		try {
			filePath = rawDataReportService.getAreaAndUserWiseRawDataReport(reportType,startDate,endDate, oauth);
			
			if(filePath == null) {
				return  ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
			}
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			//file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@GetMapping("/getDataForCard")
	Map<Integer, ReportChartModel> getDataForCard(OAuth2Authentication oauth)
			throws Exception {
		return rawDataReportService.getDataForCard( oauth);
	}
	
	@PostMapping(value = "/getRawDataReportId")
	public ResponseEntity<InputStreamResource> 
	downLoadById(@RequestParam(value = "formId") Integer formId,@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate, OAuth2Authentication oauth) throws Exception {
//	       userManagementService.downLoadFile((userManagementService.downLoadBulkTemplate()), response, inline);
		String filePath = "";
		try {
			filePath = rawDataReportService.getRawDataReportId(formId,startDate,endDate, oauth);
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			//file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	@PostMapping(value = "/getAreaWiseSkillReport")
	public ResponseEntity<InputStreamResource> 
	getAreaWiseSkillReport(@RequestParam(value = "formId") Integer formId,@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate, OAuth2Authentication oauth) throws Exception {
//	       userManagementService.downLoadFile((userManagementService.downLoadBulkTemplate()), response, inline);
		String filePath = "";
		try {
			filePath = rawDataReportService.getAreaWiseSkillReport(formId,startDate,endDate, oauth);
			
			if(filePath == null) {
				return  ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
			}
			File file = new File(filePath);

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
			InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

			//file.delete();
			return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
}
