package org.sdrc.fani.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdrc.fani.collections.GroupIndicator;
import org.sdrc.fani.models.GroupChartDataModel;
import org.sdrc.fani.models.IndicatorGroupModel;
import org.sdrc.fani.models.LegendModel;
import org.sdrc.fani.models.ParamModel;
import org.sdrc.fani.models.SVGModel;
import org.sdrc.fani.models.SectorModel;
import org.sdrc.fani.models.SubSectorModel;
import org.sdrc.fani.models.ValueObject;
import org.sdrc.fani.repositories.AreaRepository;
import org.sdrc.fani.repositories.GroupIndicatorRepository;
import org.sdrc.fani.repositories.PartnerRepository;
import org.sdrc.fani.util.FANIUtil;
import org.sdrc.fani.util.HeaderFooter;
import org.sdrc.fani.util.ImageEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service

public class ExportServiceImpl implements ExportService {

	@Autowired
	private GroupIndicatorRepository groupIndicatorRepository;

	@Autowired
	private DashboardService dashboardService;

	//String date = new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());

	@Autowired
	private ConfigurableEnvironment env;
	
	@Autowired
	private ImageEncoder encoder;
	
	@Autowired
	private PartnerRepository partnerRepository;
	
	@Autowired
	private AreaRepository areaRepository;
	
	@Override
	public String downloadChartDataPDF(List<SVGModel> listOfSvgs, String sectorName, String partnerId, HttpServletRequest request, Integer areaId) {
		String outputPathPdf = env.getProperty("output.path.pdf");
		
		
		boolean isycell = true;
		List<GroupIndicator> groupIndicatorModels = groupIndicatorRepository.findBySector(sectorName) .stream()
				.sorted(Comparator.comparing(GroupIndicator::getOrderBy))
				.collect(Collectors.toList());
		Map<String, GroupIndicator> groupIndicatorMap = new LinkedHashMap<>();
		for (GroupIndicator groupIndicatorModel : groupIndicatorModels) {
			groupIndicatorMap.put(groupIndicatorModel.getChartGroup(), groupIndicatorModel);
		}
		Map<String, SVGModel> svgMap = new LinkedHashMap<>();
		for (SVGModel eachSvgModel : listOfSvgs) {
			svgMap.put(eachSvgModel.getIndicatorGroupName(), eachSvgModel);
		}
		try {
			String date = new SimpleDateFormat("yyyyMMddHHmmssS").format(new Date());
			String path = outputPathPdf;

			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			outputPathPdf = path + "_" + date + ".pdf";

			Rectangle layout = new Rectangle(PageSize.A4.rotate());
			//layout.setBackgroundColor(new BaseColor(221, 221, 221));

			Document document = new Document(layout);//
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPathPdf));
		
			
			String uri = request.getRequestURI();
			String url = request.getRequestURL().toString();
			url = url.replaceFirst(uri, "");

			HeaderFooter headerFooter = new HeaderFooter(url, "dashboard");
			writer.setPageEvent(headerFooter);

			document.open();
			Paragraph areaParagraph = new Paragraph();
			areaParagraph.setAlignment(Element.ALIGN_CENTER);
			areaParagraph.setSpacingBefore(15);
			areaParagraph.setSpacingAfter(10);
			String chunkName = null;
			
			
			
			Chunk areaChunk = null;
			if(areaId !=null) {
				new Chunk(sectorName+": "+ partnerRepository.findById(partnerId).getData().get("organization_name")+" : "+ areaRepository.findByAreaId(areaId).getAreaName());
			} else if(partnerId != null){
				new Chunk(sectorName+": "+ partnerRepository.findById(partnerId).getData().get("organization_name"));
			}else {
				new Chunk(sectorName);
			}
					
			areaParagraph.add(areaChunk);
			
			Font indFont = new Font(FontFamily.TIMES_ROMAN, 10, Font.BOLD, GrayColor.DARK_GRAY);
			Font legendFont = new Font(FontFamily.TIMES_ROMAN, 7, Font.NORMAL, GrayColor.DARK_GRAY);

			writer.setStrictImageSequence(true);
			
			document.add(areaParagraph);
			String subsectorName = "";
			Set<String> subSectorSet = new HashSet<>();

			float yAxis = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
			float xAxis = 55;
			float xWidth = document.getPageSize().getWidth() - document.leftMargin();
			float textHeight = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin() + 30;

			PdfContentByte canvas = writer.getDirectContent();
//			ImageEncoder encoder = new ImageEncoder();

			String rbpath = path + "chart1_" + date + ".svg";
			for (String indicatorGroupName : groupIndicatorMap.keySet()) {
				
				String align = groupIndicatorMap.get(indicatorGroupName).getAlign();
				subsectorName = groupIndicatorMap.get(indicatorGroupName).getSubSector();
				String legends = groupIndicatorMap.get(indicatorGroupName).getColorLegends();
				
				if (svgMap.containsKey(indicatorGroupName)) {
					
					
					SVGModel svgModel = svgMap.get(indicatorGroupName);
					//System.out.println(indicatorGroupName+"-------"+svgModel.getIndicatorGroupName()+"-------"+subsectorName);
					File svgFile = new File(rbpath);
					FileOutputStream fop = new FileOutputStream(svgFile);
					byte[] contentbytes = svgModel.getSvg().getBytes();
					fop.write(contentbytes);

					String jpgFilePath = encoder.createImgFromFile(rbpath, align, svgModel.getChartType(),null);
					//String jpgFilePath = null;
					Image jpgImage = Image.getInstance(jpgFilePath);

					jpgImage.setAlignment(Element.ALIGN_CENTER);
					jpgImage.setBorder(Rectangle.BOX);

					float scalerColMd12 = (float) (((document.getPageSize().getWidth() - document.leftMargin()
							- document.rightMargin()) / jpgImage.getWidth()) * 97.5);

					float scalerColMd6 = ((document.getPageSize().getWidth() - document.leftMargin()
							- document.rightMargin()) / jpgImage.getWidth()) * 47;

					float scalerColMd4 = ((document.getPageSize().getWidth() - document.leftMargin()
							- document.rightMargin()) / jpgImage.getWidth()) * 30;

					float scalerColMd2 = (float) (((document.getPageSize().getWidth() - document.leftMargin()
							- document.rightMargin()) / jpgImage.getWidth()) * 20);
					
					boolean isSector = false;
					
					
					
					if (!subSectorSet.contains(subsectorName)) {
						boolean iscell = true;
System.out.println("+++++"+subsectorName);
						if(yAxis<jpgImage.getHeight() - document.topMargin() - document.bottomMargin() - 5) {
							textHeight = document.getPageSize().getHeight() - document.topMargin() - 8;
						}else if(yAxis<=235 && groupIndicatorMap.get(svgModel.getIndicatorGroupName()).getAlign().equals("col-md-4") && !svgModel.getChartType().equals("card") ) {
							document.newPage();
							textHeight = document.getPageSize().getHeight() - document.topMargin() - 8;
							yAxis = (float) (document.getPageSize().getHeight() - document.topMargin()
									- document.bottomMargin());
							xAxis = 55;
							xWidth = document.getPageSize().getWidth() - document.leftMargin();
							
						}
							textHeight = yAxis+7;
						

						Font font = new Font(FontFamily.TIMES_ROMAN, 12, Font.BOLD, GrayColor.BLACK);
						PdfPTable tableSubsector = new PdfPTable(1);
						PdfPCell cellSubsector = new PdfPCell();
						Paragraph pSubsector = new Paragraph(subsectorName, font);
						pSubsector.setAlignment(Element.ALIGN_CENTER);
						pSubsector.setSpacingBefore(1);
						pSubsector.setSpacingAfter(3);
						cellSubsector.addElement(pSubsector);
						cellSubsector.setBorder(Rectangle.NO_BORDER);
						tableSubsector.addCell(cellSubsector);
						tableSubsector.setTotalWidth(document.getPageSize().getWidth());
						
						
						if (yAxis < jpgImage.getHeight() - document.topMargin() - document.bottomMargin() - 5) {
							document.newPage();
							textHeight = document.getPageSize().getHeight() - document.topMargin() - 8;
							yAxis = (float) (document.getPageSize().getHeight() - document.topMargin()
									- document.bottomMargin() - (jpgImage.getHeight() / 1.5) - 30);
							xAxis = 55;
							xWidth = document.getPageSize().getWidth() - document.leftMargin();
//								System.out.println("NEW Doc...chart:"+svgModel.getChartType()+" align:"+align+" yAxis: "+yAxis+ " xAxis:"+xAxis +" xWidth:"+xWidth);
						} else {
							xAxis = 55;
							xWidth = document.getPageSize().getWidth() - document.leftMargin();
							yAxis -= jpgImage.getHeight() > 100 ? jpgImage.getHeight() - 90 : jpgImage.getHeight()-10;
							if (yAxis < 0) {
								document.newPage();
								textHeight = document.getPageSize().getHeight() - document.topMargin() - 8;
								yAxis = (float) (document.getPageSize().getHeight() - document.topMargin()
										- document.bottomMargin() - (jpgImage.getHeight() / 1.5) - 8);
							} else { //in case of same page, new sector
								yAxis -=10;
							}

						}
						tableSubsector.writeSelectedRows(-1, -1, 10, textHeight, writer.getDirectContent());

						if (jpgImage.getWidth() > 1000) {// for "col-md-12" images
							xWidth -= jpgImage.getWidth();
							jpgImage.scalePercent(scalerColMd12);
						} else if (jpgImage.getWidth() < 300) {
							jpgImage.scalePercent(scalerColMd2);
						}
						else if (jpgImage.getWidth() < 400) {
							jpgImage.scalePercent(scalerColMd4);
						}
						else {
							jpgImage.scalePercent(scalerColMd6);
						}


						subSectorSet.add(subsectorName);
						isSector = true;
						if(groupIndicatorMap.get(svgModel.getIndicatorGroupName()).getAlign().equals("col-md-4") && !svgModel.getChartType().equals("card")) {
							if(iscell) {
								yAxis -=50;
								iscell=false;
								}
						}else if(svgModel.getIndName()!=null && svgModel.getIndName().length() > 84) {
							yAxis -=15;
							iscell=false;
						}
					}

					if (!isSector) {

						if (xWidth < jpgImage.getWidth() - document.leftMargin()
								&& yAxis < jpgImage.getHeight() - document.topMargin() - document.bottomMargin() - 5) {
							document.newPage();
							yAxis = (float) (document.getPageSize().getHeight() - document.topMargin()
									- document.bottomMargin() - (jpgImage.getHeight() / 1.5) - 8);
							xAxis = 55;
							xWidth = document.getPageSize().getWidth() - document.leftMargin();
						} else if (xWidth < jpgImage.getWidth()) {
							xAxis = 55;
							xWidth = document.getPageSize().getWidth() - document.leftMargin();
							yAxis -= jpgImage.getHeight() > 100 ? jpgImage.getHeight() - 90 : jpgImage.getHeight()-10;

							if (yAxis < 0) {
								document.newPage();
								yAxis = (float) (document.getPageSize().getHeight() - document.topMargin()
										- document.bottomMargin() - (jpgImage.getHeight() / 1.5) - 8);
							} else {
								yAxis -=5;
							}

						} else {
							if(groupIndicatorMap.get(svgModel.getIndicatorGroupName()).getAlign().equals("col-md-6") && svgModel.getChartType().equals("card")) {
								xAxis += jpgImage.getWidth() < 400 ? jpgImage.getWidth() - (jpgImage.getWidth() / 4)
										: jpgImage.getWidth() - (jpgImage.getWidth() / 3.7);
							}else {
							xAxis += jpgImage.getWidth() < 400 ? jpgImage.getWidth() - (jpgImage.getWidth() / 4)
									: jpgImage.getWidth() - (jpgImage.getWidth() / 3);
							}
							xWidth -= jpgImage.getWidth();
						}

						if (jpgImage.getWidth() > 1000) { // for "col-md-12" images
							xWidth -= jpgImage.getWidth();
							jpgImage.scalePercent(scalerColMd12);
						}  else if (jpgImage.getWidth() < 300) {
							jpgImage.scalePercent(scalerColMd2);
						}else if (jpgImage.getWidth() < 400) {
							jpgImage.scalePercent(scalerColMd4);
						} else {
							jpgImage.scalePercent(scalerColMd6);
						}

					
					}
					
					PdfPCell indNameCell = new PdfPCell();
//					Paragraph indNamePara = new Paragraph(svgModel.getIndName(), indFont);
					Paragraph indNamePara = null;
					if(groupIndicatorMap.get(svgModel.getIndicatorGroupName()).getAlign().equals("col-md-4") && !svgModel.getChartType().equals("card")) {
						 indNamePara = new Paragraph(svgModel.getIndName()!=null && svgModel.getIndName().length() > 48
								 ? svgModel.getIndName().substring(0, 48) + "-" + "\n"
								+ svgModel.getIndName().substring(48, svgModel.getIndName().length())
								 : svgModel.getIndName(), indFont);
					}else {
						  indNamePara = new Paragraph(svgModel.getIndName()!=null && svgModel.getIndName().length() > 84
								 ? svgModel.getIndName().substring(0, 84) + "-" + "\n"
								+ svgModel.getIndName().substring(84, svgModel.getIndName().length())
								 : svgModel.getIndName(), indFont);
					}
					
					indNamePara.setAlignment(Element.ALIGN_LEFT);
					indNamePara.setSpacingBefore(1);
					indNamePara.setSpacingAfter(3);
					indNameCell.addElement(indNamePara);
					indNameCell.setBorder(Rectangle.NO_BORDER);

					PdfPTable indNameTblLeft = new PdfPTable(1);
					indNameTblLeft.addCell(indNameCell);
					indNameTblLeft.setTotalWidth(document.getPageSize().getWidth());
					
					if (groupIndicatorMap.get(svgModel.getIndicatorGroupName()).getAlign().equals("col-md-4")
							&& !svgModel.getChartType().equals("card")) {
						indNameTblLeft.writeSelectedRows(-1, -1, xAxis, yAxis + 245, writer.getDirectContent());
						
					} else {
						if (svgModel.getIndName() != null && svgModel.getIndName().length() > 84) {
							indNameTblLeft.writeSelectedRows(-1, -1, xAxis, yAxis + 224, writer.getDirectContent());
							

						} else {
							indNameTblLeft.writeSelectedRows(-1, -1, xAxis, yAxis + 210, writer.getDirectContent());
						}

					}

					// n value
					if(svgModel.getShowValue() != null) {
						PdfPCell nValCell = new PdfPCell();
						Paragraph nValPara = new Paragraph("n="+ svgModel.getShowValue() + " (" +svgModel.getShowNName()+")" , indFont);
						nValPara.setAlignment(Element.ALIGN_RIGHT);
						nValPara.setSpacingBefore(1);
						nValPara.setSpacingAfter(3);
						nValCell.addElement(nValPara);
						nValCell.setBorder(Rectangle.NO_BORDER);

						PdfPTable nValTbl = new PdfPTable(1);
						nValTbl.addCell(nValCell);
						nValTbl.setTotalWidth(document.getPageSize().getWidth());
						
						if (jpgImage.getWidth() > 1000) 
							nValTbl.writeSelectedRows(-1, -1, xAxis-100, yAxis+215, writer.getDirectContent());	
						else
							nValTbl.writeSelectedRows(-1, -1, xAxis-480, yAxis+215, writer.getDirectContent());	
					}
					
					
					if(svgModel.getChartType().equals("pie") || svgModel.getChartType().equals("donut") 
							&& legends!=null && legends.length() > 0) {/*
						
						String css="";
						String leg = "";
						String[] legendsList = legends.split(",");
						int i =5;
						for (String legend : legendsList) {
							css = legend.split("_")[0];
							leg = legend.split("_")[1];

							int color = (int) Long.parseLong(css.split("#")[1], 16);
							int r = (color >> 16) & 0xFF;
							int g = (color >> 8) & 0xFF;
							int b = (color >> 0) & 0xFF;
							
							canvas.rectangle(xAxis+5, yAxis + i + 5, 7, 7);
//							canvas.setColorFill(BaseColor.GRAY);
							canvas.setColorFill(new BaseColor(r, g, b));
							canvas.fill();

							PdfPCell legendCell = new PdfPCell();
							Paragraph legendPara = new Paragraph(leg, legendFont);
							legendPara.setAlignment(Element.ALIGN_LEFT);
							legendPara.setSpacingBefore(1);
							legendPara.setSpacingAfter(3);
							legendCell.addElement(legendPara);
							legendCell.setBorder(Rectangle.NO_BORDER);

							PdfPTable legendTable = new PdfPTable(1);
							legendTable.addCell(legendCell);
							legendTable.setTotalWidth(document.getPageSize().getWidth());
							legendTable.writeSelectedRows(-1, -1, xAxis + 15, yAxis + 18 + i,
									writer.getDirectContent());

							i += 20;
						}
						
						 
					*/}
					
					//group chart legend... optimize code
					
					else if(svgModel.getChartType().equals("groupbar") && legends!=null && legends.length() > 0) {/*
						int i = 0;
						String css="";
						String leg = "";
						String[] legendsList = legends.split(",");
						for (String legend : legendsList) {
							css = legend.split("_")[0];
							leg = legend.split("_")[1];

							int color = (int) Long.parseLong(css.split("#")[1], 16);
							int r = (color >> 16) & 0xFF;
							int g = (color >> 8) & 0xFF;
							int b = (color >> 0) & 0xFF;
							
							canvas.rectangle(i ==0 ? xAxis+i+5 : xAxis+i + (leg.length()*3), yAxis + 5, 7, 7);
//							canvas.setColorFill(BaseColor.GRAY);
							canvas.setColorFill(new BaseColor(r, g, b));
							canvas.fill();

							PdfPCell legendCell = new PdfPCell();
							Paragraph legendPara = new Paragraph(leg, legendFont);
							legendPara.setAlignment(Element.ALIGN_LEFT);
							legendPara.setSpacingBefore(1);
							legendPara.setSpacingAfter(3);
							legendCell.addElement(legendPara);
							legendCell.setBorder(Rectangle.NO_BORDER);

							PdfPTable legendTable = new PdfPTable(1);
							legendTable.addCell(legendCell);
							legendTable.setTotalWidth(document.getPageSize().getWidth());
							legendTable.writeSelectedRows(-1, -1, i==0 ? xAxis+15+i : xAxis+10+i +( leg.length() * 3) , yAxis + 18,
									writer.getDirectContent());
							
							i +=90;

						}
						
						 
					*/}
					
					jpgImage.setBorder(Rectangle.BOX);
					jpgImage.setBorderWidth(2);
					jpgImage.setBorderColor(BaseColor.LIGHT_GRAY);
					jpgImage.setAbsolutePosition(xAxis, yAxis);
					
					document.add(jpgImage);
					
					fop.flush();
					fop.close();

					svgFile.delete();
					new File(jpgFilePath).delete();
				}

			}
			
			document.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputPathPdf;
	}
	
	public static PdfPCell createImageCell(Image jpgImage) throws DocumentException {
	    PdfPCell cell = new PdfPCell(jpgImage, true);
	    return cell;
	}
	
	public static PdfPCell createTextCell(String text) throws DocumentException {
	    PdfPCell cell = new PdfPCell();
	    Paragraph p = new Paragraph(text);
	    p.setAlignment(Element.ALIGN_RIGHT);
	    cell.addElement(p);
	    cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
	    cell.setBorder(Rectangle.NO_BORDER);
	    return cell;
	}
	
	private void createFolder() {
		String pdfPath =  env.getProperty("output.path.pdf");
		File pdfFile = new File(pdfPath);
		if (!pdfFile.exists()) {
			pdfFile.mkdirs();
		}
		String excelPath = env.getProperty("output.path.excel");
		File excelFile = new File(excelPath);
		if (!excelFile.exists()) {
			excelFile.mkdirs();
		}
		
	}

	@Override
	public String downloadChartDataExcel(List<SVGModel> listOfSvgs, ParamModel paramModel, HttpServletRequest request) {
		createFolder();
		if(paramModel.getAreaId()==null) {
			paramModel.setAreaId(2);
		}
		ValueObject getSectorMap = getSectorMap(dashboardService.getDashboardData(paramModel.getAreaId(), paramModel.getPartnerId(),paramModel.getSectorName()));
		
		
		String date = new SimpleDateFormat("yyyyMMddHHmmssS").format(new Date());
		
		String path = env.getProperty("output.path.excel");
		String outputPathExcel = path+paramModel.getSectorName() + "_" + date + ".xlsx";
		List<GroupIndicator> groupIndicatorModels = groupIndicatorRepository.findBySector(paramModel.getSectorName()) .stream()
				.sorted(Comparator.comparing(GroupIndicator::getOrderBy))
				.collect(Collectors.toList());
		Map<String, GroupIndicator> groupIndicatorMap = new LinkedHashMap<>();
		for (GroupIndicator groupIndicatorModel : groupIndicatorModels) {
			groupIndicatorMap.put(groupIndicatorModel.getChartGroup(), groupIndicatorModel);
		}

//		ImageEncoder encoder = new ImageEncoder();

		String rbpath = path + "chart1_" + date + ".svg";
		
		Map<String, SVGModel> svgMap = listOfSvgs.stream()
				.collect(Collectors.toMap(SVGModel::getIndicatorGroupName, v -> v));

		try {

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("FANI");
			XSSFCellStyle cellstyleMiddle = FANIUtil.getStyleForLeftMiddle(workbook);
			XSSFCellStyle cellstyleMiddle1 = FANIUtil.getStyleForLeftMiddle1(workbook);
			XSSFCellStyle cellstyleMiddle2 = FANIUtil.getStyleForLeftMiddle2(workbook);
			XSSFCellStyle cellstyleMiddle3 = FANIUtil.getStyleForLeftMiddle3(workbook);
			XSSFCellStyle cellstyleMiddle4 = FANIUtil.getStyleForLeftMiddle4(workbook);
			XSSFCellStyle cellstyleMiddle5 = FANIUtil.getStyleForLeftMiddle5(workbook);
			XSSFCellStyle cellstyleFont = FANIUtil.getStyleForFont(workbook);
			XSSFCellStyle cellgetStyleForFontDatavalue = FANIUtil.getStyleForFont(workbook);
			XSSFCellStyle cellgetStyleForSector = FANIUtil.getStyleForSectorFont(workbook);
			CellStyle style = workbook.createCellStyle(); //Create new style
			int rowNum = 0;
			
			BufferedImage bImage = ImageIO.read(new File(ResourceUtils.getFile("classpath:images/Header.png").getAbsolutePath()));
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ImageIO.write(bImage, "jpg", bos );
			byte [] headerImgBytes = bos.toByteArray();
			insertimage(0, headerImgBytes, workbook, sheet, null);
			
			int selectionrow=5; 
			XSSFRow rowselection = null;
			XSSFCell cellselection = null;
			rowselection = sheet.createRow(selectionrow);
			
			
			if(paramModel.getSectorName()!=null) {
				++selectionrow;
			rowselection = sheet.createRow(selectionrow);
			cellselection = rowselection.createCell(0);
			cellselection.setCellValue("Sector :- "+paramModel.getSectorName());
			cellselection.setCellStyle(cellgetStyleForFontDatavalue);
			}
			

			rowNum += 12; 
			int cellNum = 0;
			XSSFRow row = null;
			XSSFCell cell = null;
			Set<String> subSectorSet = new HashSet<>();
			for (String indicatorGroupName : groupIndicatorMap.keySet()) {
				if (svgMap.containsKey(indicatorGroupName)) {
					GroupIndicator groupIndiactor = groupIndicatorMap.get(indicatorGroupName);

					SVGModel svgModel = svgMap.get(indicatorGroupName);
					// byte[] svgImageBytes = Base64.decodeBase64(((String)
					// svgModel.getSvg()).split(",")[1]);

					File svgFile = new File(rbpath);
					FileOutputStream fop = new FileOutputStream(svgFile);
					byte[] contentbytes = svgModel.getSvg().getBytes();
					fop.write(contentbytes);

					String jpgFilePath = encoder.createImgFromFile(rbpath, groupIndiactor.getAlign(), svgModel.getChartType(),"excel");
					InputStream inputStream = new FileInputStream(jpgFilePath);
					byte[] imageBytes = IOUtils.toByteArray(inputStream);

					row = sheet.createRow(rowNum);
					cell = row.createCell(cellNum);
					if (!subSectorSet.contains(groupIndiactor.getSubSector())) {
						cell.setCellValue(groupIndiactor.getSubSector());
						sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, cellNum, cellNum + 8));
						cell.setCellStyle(cellgetStyleForSector);
						subSectorSet.add(groupIndiactor.getSubSector());
						rowNum += 1;
					}
					int row2 = insertimage(rowNum, imageBytes, workbook, sheet, groupIndiactor.getAlign());

					int tempRow = 0;
					int tempCol = 0;
					boolean flag = true;

					List<String> value = getSectorMap.getChartMap().get(svgModel.getIndicatorGroupName());
					if (value != null) {

						if (svgModel.getShowValue() != null) {
							row = sheet.getRow(rowNum + 2) == null ? sheet.createRow(rowNum + 2)
									: sheet.getRow(rowNum + 2);
							cell = row.createCell(10);
							cell.setCellValue("n=" + svgModel.getShowValue() + " (" + svgModel.getShowNName() + ")");
						}

						if (svgModel.getChartType().equals("card")) {

							tempRow = rowNum + 3;
							row = sheet.createRow(tempRow);
							cell = row.createCell(10);
							cell.setCellValue(value.get(0).split("#")[0]);
							sheet.autoSizeColumn(value.get(0).split("#")[0].length());
							cell.setCellStyle(cellstyleFont);
							cell = row.createCell(11);
							cell.setCellValue(value.get(0).split("#")[1].equals("null") ? "NA" : value.get(0).split("#")[1]);

						} else if (svgModel.getChartType().equals("groupbar") || svgModel.getChartType().equals("pie")
								|| svgModel.getChartType().equals("donut")) {
							
							List<String> legends = getSectorMap.getLegendsMap().get(svgModel.getIndicatorGroupName());
							System.out.println(legends);
							int divide = value.size() / legends.size();
							tempRow = rowNum + 3;
							int k = 0, l = 0;
							tempCol = 10;
							for (int i = 0; i <= value.size() - 1; i++) {
								if (k > 0) {
									if(divide!=0) {
									if (k == divide || k % divide == 0) {
										tempCol += 3;
										tempRow = rowNum + 4;
										++l;

									}
									}
								}
								if (tempCol == 10) {
									row = sheet.createRow(tempRow);
								} else {
									row = sheet.getRow(tempRow);
								}
								cell = row.createCell(tempCol);
								if (flag) {
									cell.setCellValue(value.get(i).split("#")[0]);
									cell.setCellStyle(cellgetStyleForFontDatavalue);

									++tempRow;
									// sheet.addMergedRegion(new CellRangeAddress(tempRow-1, tempRow-1, tempCol,
									// tempCol+11));
								}
								if (tempCol == 10) {
									row = sheet.createRow(tempRow);
								} else {
									row = sheet.getRow(tempRow);
								}
								cell = row.createCell(tempCol);
								cell.setCellValue(value.get(i).split("#")[1] + " (" + legends.get(l).split("@#")[1] + ")");
								sheet.setColumnWidth(tempCol, 13000);

								style.setWrapText(true);

								cell = row.createCell(tempCol + 1);
								cell.setCellValue(value.get(i).split("#")[2].equals("null") ? "NA" : value.get(i).split("#")[2]);

								++tempRow;
								++k;
								flag = false;
							}
							sheet.autoSizeColumn(tempCol);
							tempRow += 1;
							row = sheet.createRow(++tempRow);
							cell = row.createCell(9);
							cell.setCellValue("Legends");
							cell.setCellStyle(cellgetStyleForFontDatavalue);

							sheet.addMergedRegion(new CellRangeAddress(tempRow, tempRow, 9, 10));
							for (int m = 0; m <= legends.size() - 1; m++) {
								row = sheet.createRow(++tempRow);
								cell = row.createCell(10);
								cell.setCellValue(legends.get(m).split("@#")[1]);

								cell = row.createCell(9);

								switch (legends.get(m).split("@#")[0]) {
								case "#094086":
									cell.setCellStyle(cellstyleMiddle);
									break;
								case "#3a74b9":
									cell.setCellStyle(cellstyleMiddle1);
									break;
								case "#bbb":
									cell.setCellStyle(cellstyleMiddle2);
									break;
								case "#67acf4":
									cell.setCellStyle(cellstyleMiddle3);
									break;
								case "#6261c2":
									cell.setCellStyle(cellstyleMiddle4);
									break;
								case "#a6cce6":
									cell.setCellStyle(cellstyleMiddle5);
									break;
									
								}
							}

						} else {
							tempRow = rowNum + 3;
							tempCol = 10;
							for (int i = 0; i <= value.size() - 1; i++) {
								row = sheet.createRow(tempRow);
								cell = row.createCell(tempCol);
								if (flag) {
									cell.setCellValue(value.get(i).split("#")[0]);
									cell.setCellStyle(cellgetStyleForFontDatavalue);
									sheet.autoSizeColumn(value.get(i).split("#")[0].length());
									++tempRow;
									// sheet.addMergedRegion(new CellRangeAddress(tempRow-1, tempRow-1, tempCol,
									// tempCol+2));
								}
								row = sheet.createRow(tempRow);
								cell = row.createCell(tempCol);
								cell.setCellValue(value.get(i).split("#")[1]);
								sheet.autoSizeColumn(value.get(i).split("#")[1].length());

								cell = row.createCell(tempCol + 1);
								cell.setCellValue(value.get(i).split("#")[2].equals("null") ? "NA" : value.get(i).split("#")[2]);
								++tempRow;
								flag = false;
							}
						}
						rowNum += 5;
					}
					rowNum = row2 + rowNum;

					new File(jpgFilePath).delete();
					fop.flush();
					fop.close();
				}

			}

			FileOutputStream fileOutputStream = new FileOutputStream(new File(outputPathExcel));
			workbook.write(fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			workbook.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return outputPathExcel;
	}
	
	
	/*private String getFileName(String outputPathExcel) {
		File file = new File(outputPathExcel);
		if (!file.exists()) {
			file.mkdirs();
		}
		outputPathExcel = outputPathExcel + "_" + date + ".xlsx";

		return outputPathExcel;
	}*/

	private int insertimage(int rowNum, byte [] imageBytes, XSSFWorkbook xssfWorkbook, XSSFSheet sheet, String align) {
		Integer size = null;
		try {
			int pictureIdx = xssfWorkbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
			CreationHelper helper = xssfWorkbook.getCreationHelper();
			Drawing<?> drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(0);
			anchor.setCol2(6);
			anchor.setRow1(rowNum);
			anchor.setRow2(rowNum+5);
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			System.out.println(align+" : pict Height : "+pict.getImageDimension().getHeight()+" : "+pict.getImageDimension().getWidth());
			if(pict.getImageDimension().getHeight()<=100) {
				anchor.setCol2(9);
				anchor.setRow2(4);
				pict.resize();
				size = 4;
			}else if(pict.getImageDimension().getHeight()>100 && pict.getImageDimension().getHeight()<=150){
				pict.resize();
				size = 12;
			} else if(pict.getImageDimension().getHeight()>150 && pict.getImageDimension().getHeight()<300) {
				pict.resize();
				size = 18;
			} else if (pict.getImageDimension().getHeight()>=300) {
				pict.resize(1.5);
				size = 24;
			}

		} catch (Exception e) {
		}
		return size.intValue();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ValueObject getSectorMap(List<SectorModel> sectorModelList ) {
		ValueObject valueObject  = new ValueObject();
		Map<String, List<String>> chartMap = new LinkedHashMap<>();
		Map<String, List<String>> legendsMap = new LinkedHashMap<>();
		
		
		for (SectorModel sectorModel : sectorModelList) {
			for (SubSectorModel subsector : sectorModel.getSubSectors()) {
				for (IndicatorGroupModel indicator : subsector.getIndicators()) {

					if (!indicator.getChartsAvailable().get(0).equals("")
							&& indicator.getChartsAvailable().get(0) != null) {
						if (indicator.getChartsAvailable().get(0).equals("card")) {
							chartMap.put(indicator.getChartGroup(),
									Arrays.asList(indicator.getIndicatorName() + "#" + indicator.getIndicatorValue()));
						} else if (indicator.getChartsAvailable().get(0).equals("groupbar") || indicator.getChartsAvailable().get(0).equals("pie")
								|| indicator.getChartsAvailable().get(0).equals("donut")) {
							for (GroupChartDataModel chartData : indicator.getChartData()) {
								chartMap = setInMap(chartData, chartMap, indicator);

								List<LegendModel> legends = chartData.getLegends();
								if (!legends.isEmpty() && legends != null) {
									for (int j = 0; j <= (legends.size() - 1); j++) {
										List listOfString = new ArrayList<>();
										if (legendsMap.containsKey(indicator.getChartGroup())) {
											listOfString.add(
													legends.get(j).getCssClass() + "@#" + legends.get(j).getValue());
											legendsMap.get(indicator.getChartGroup()).addAll(listOfString);
										} else {
											listOfString.add(
													legends.get(j).getCssClass() + "@#" + legends.get(j).getValue());
											legendsMap.put(indicator.getChartGroup(), listOfString);
										}
									}
								}
							}

						} else {
							for (GroupChartDataModel chartData : indicator.getChartData()) {
								chartMap = setInMap(chartData, chartMap, indicator);
							}

						}

					}

				}
			}

		}
		
		valueObject.setChartMap(chartMap);
		valueObject.setLegendsMap(legendsMap);
		return valueObject;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, List<String>> setInMap(GroupChartDataModel chartData, Map<String, List<String>> chartMap,
			IndicatorGroupModel indicator) {
		List listOfString = null;
		for (int i = 0; i <= (chartData.getChartDataValue().size() - 1); i++) {
			for (int j = 0; j <= (chartData.getChartDataValue().get(i).size() - 1); j++) {
				listOfString = new ArrayList<>();
				if (chartMap.containsKey(indicator.getChartGroup())) {
					listOfString.add(chartData.getHeaderIndicatorName() + "#"
							+ chartData.getChartDataValue().get(i).get(j).getAxis() + "#"
							+ chartData.getChartDataValue().get(i).get(j).getValue());
					chartMap.get(indicator.getChartGroup()).addAll(listOfString);
				} else {
					listOfString.add(chartData.getHeaderIndicatorName() + "#"
							+ chartData.getChartDataValue().get(i).get(j).getAxis() + "#"
							+ chartData.getChartDataValue().get(i).get(j).getValue());
					chartMap.put(indicator.getChartGroup(), listOfString);
				}
			}
		}
		return chartMap;
	}

	
			
	
}