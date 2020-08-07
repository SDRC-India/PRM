package org.sdrc.fani.util;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class FANIUtil {

	public static XSSFCellStyle getStyleForLeftMiddle(XSSFWorkbook workbook) {
		
		XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
		styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
		styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
		styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(9, 64, 134)));
		styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
		return styleForLeftMiddle;
	}
public static XSSFCellStyle getStyleForLeftMiddle1(XSSFWorkbook workbook) {
		
		XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
		styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
		styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
		styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(58, 116, 185)));
		styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
		return styleForLeftMiddle;
	}
public static XSSFCellStyle getStyleForLeftMiddle2(XSSFWorkbook workbook) {
	
	XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
	styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
	styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
	styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(187, 187, 187)));
	styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
	return styleForLeftMiddle;
}
public static XSSFCellStyle getStyleForLeftMiddle3(XSSFWorkbook workbook) {
	
	XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
	styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
	styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
	styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(103, 172, 244)));
	styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
	return styleForLeftMiddle;
}

public static XSSFCellStyle getStyleForLeftMiddle4(XSSFWorkbook workbook) {
	XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
	styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
	styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
	styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(98, 97, 194)));
	styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
	return styleForLeftMiddle;
}
public static XSSFCellStyle getStyleForLeftMiddle5(XSSFWorkbook workbook) {XSSFCellStyle styleForLeftMiddle = workbook.createCellStyle();
styleForLeftMiddle.setVerticalAlignment(VerticalAlignment.CENTER);
styleForLeftMiddle.setAlignment(HorizontalAlignment.CENTER);
styleForLeftMiddle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
styleForLeftMiddle.setFillForegroundColor(new XSSFColor(new java.awt.Color(166, 204, 230)));
styleForLeftMiddle.setFont(getStyleForFont(workbook).getFont());
return styleForLeftMiddle;}
	
	
	public static XSSFCellStyle getStyleForFont(XSSFWorkbook workbook) {
		
		//Create a new font and alter it.
       XSSFFont font = workbook.createFont();
       font.setFontHeightInPoints((short) 12);
       font.setBold(true);
		
		XSSFCellStyle tyleForWrapFont = workbook.createCellStyle();
		tyleForWrapFont.setFont(font);
		
		return tyleForWrapFont;
	}
	
public static XSSFCellStyle getStyleForSectorFont(XSSFWorkbook workbook) {
		
		//Create a new font and alter it.
       XSSFFont font = workbook.createFont();
       font.setFontHeightInPoints((short) 15);
       font.setBold(true);
		
		XSSFCellStyle tyleForWrapFont = workbook.createCellStyle();
		tyleForWrapFont.setFont(font);
		
		return tyleForWrapFont;
	}
	
	
public static XSSFCellStyle getStyleForFontDatavalue(XSSFWorkbook workbook) {
	
	//Create a new font and alter it.
   XSSFFont font = workbook.createFont();
   font.setFontHeightInPoints((short) 12);
   font.setBold(true);
  
	XSSFCellStyle tyleForWrapFont = workbook.createCellStyle();
	tyleForWrapFont.setFont(font);
	tyleForWrapFont.setAlignment(HorizontalAlignment.LEFT);
	tyleForWrapFont.setVerticalAlignment(VerticalAlignment.CENTER);
	return tyleForWrapFont;
}


}
