package org.sdrc.fani.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sdrc.fani.models.ParamModel;
import org.sdrc.fani.models.SVGModel;

public interface ExportService {

	String downloadChartDataPDF(List<SVGModel> listOfSvgs, String sectorName, String partnerId,HttpServletRequest request,Integer areaId);

	String downloadChartDataExcel(List<SVGModel> listOfSvgs, ParamModel paramModel, HttpServletRequest request);

}
