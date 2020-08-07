package org.sdrc.fani.controller;


import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sdrc.fani.collections.Area;
import org.sdrc.fani.collections.Partner;
import org.sdrc.fani.models.ParamModel;
import org.sdrc.fani.models.PushpinModel;
import org.sdrc.fani.models.SVGModel;
import org.sdrc.fani.models.SectorModel;
import org.sdrc.fani.service.DashboardService;
import org.sdrc.fani.service.ExportService;
import org.sdrc.fani.service.SVGJSONModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class DashboardController {
	
	@Autowired
	public DashboardService dashboardService;
	
	@Autowired
	private ExportService exportService;	
	
	@GetMapping(value="/pushIndicatorGroupData")
	@ResponseBody
	public String pushIndicatorGroupData() {
		return	dashboardService.pushIndicatorGroupData();
	}
	
	@GetMapping(value="/getDashboardData")
	public List<SectorModel> getDashboardData(@RequestParam(value="areaId", required=false) Integer areaId,
			@RequestParam(value="partnerId", required=false)  String partnerId,@RequestParam(value="sectorName", required=true)  String sectorName) {
		
		return dashboardService.getDashboardData(areaId,partnerId,sectorName);
	}
	
	@GetMapping(value="/getAllSector")
	public List<String> getAllSector(OAuth2Authentication auth) {
		return dashboardService.getAllSectors(auth);
	}
	
	@GetMapping(value="/getSectorWisePartner")
	public List<Partner> getAllPartnerBySector(@RequestParam(value="sectorName") String sectorName) {
		return dashboardService.getSectorWisePartner(sectorName);
	}
		
	@GetMapping(value="/getPartnerWiseArea")
	public List<Area> getPartnerWiseArea(@RequestParam(value="partnerId", required=false) String partnerId) {
		return dashboardService.getPartnerWiseArea(partnerId);
	}
		

		@PostMapping(value = "/downloadChartDataPDF")
		public ResponseEntity<InputStreamResource> downloadChartDataPDF(@RequestBody List<SVGModel> listOfSvgs,
				@RequestParam(value = "sectorName", required = true) String sectorName,
				@RequestParam(value = "partnerId", required = false) String partnerId, HttpServletResponse response,
				HttpServletRequest request, @RequestParam(value = "areaId", required = false) Integer areaId) {
			
			String filePath = "";
			try {
				filePath = exportService.downloadChartDataPDF(listOfSvgs, sectorName,partnerId,request,areaId);
				File file = new File(filePath);

				HttpHeaders respHeaders = new HttpHeaders();
				respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
				InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

				file.delete();
				return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
			}
		}

		@PostMapping(value = "/downloadChartDataExcel")
		public ResponseEntity<InputStreamResource> downloadChartDataExcel(@RequestBody ParamModel paramModel,
				HttpServletResponse response, HttpServletRequest request) {

			String filePath = "";
			try {
				filePath = exportService.downloadChartDataExcel(paramModel.getListOfSvgs(), paramModel, request);
				File file = new File(filePath);

				HttpHeaders respHeaders = new HttpHeaders();
				respHeaders.add("Content-Disposition", "attachment; filename=" + file.getName());
				InputStreamResource isr = new InputStreamResource(new FileInputStream(file));

				file.delete();
				return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
			}
		}
		
		@GetMapping(value="/getpushpinData")
		public List<PushpinModel> getpushpinData(@RequestParam(value="indicatorId") Integer indicatorId,@RequestParam(value="areaId") Integer areaId) {
		return dashboardService.getpushpinData(indicatorId , areaId);
		}
		
		
		@GetMapping(value="/exportGroupIndicator")
		public String exportGroupIndicator() throws Exception {
			return dashboardService.exportGroupIndicator();
		}
		
		@GetMapping(value="/exportIndicator")
		public String exportIndicator() throws Exception {
			return dashboardService.exportIndicator();
		}
		
		@GetMapping(value="/getLastAggregationTime")
		public String getLastAggregationTime() throws Exception {
			return dashboardService.getLastAggregationTime();
		}
		
		
		@GetMapping(value="/exportDataValue")
		public String exportDataValue() throws Exception {
			return dashboardService.exportDataValue();
		}
	
}
