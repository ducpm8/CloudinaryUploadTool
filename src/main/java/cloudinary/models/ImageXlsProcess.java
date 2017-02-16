package cloudinary.models;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cloudinary.repositories.ImageObject;

public class ImageXlsProcess {
	
	private ImageObject orderReport;
	
	public Map<String, List<ImageObject>> doPlaceOrder(String fileName)
    {
		
		Map<String, List<ImageObject>> result = new HashMap<String, List<ImageObject>>();
		
		List<ImageObject> orderReportList = new ArrayList<ImageObject>();
		
		List<ImageObject> orderReportFailList = new ArrayList<ImageObject>();
		
        try {
        	
        	String productID_Trim2_SKU = StringUtils.EMPTY;
        	char extention;
        	FileInputStream file;
        	
        	file = new FileInputStream(new File(fileName));
        	extention =  fileName.charAt(fileName.length() - 1);
        	if (extention == 'x') {
        		
        	} else {
        		file = new FileInputStream(new File(fileName));
    			
            	//Get the workbook instance for XLS file 
            	HSSFWorkbook workbook = new HSSFWorkbook(file);
            	
            	HSSFRow rowHS;
            	HSSFCell cellHS;
            	HSSFCell cellHS2;
            	
            	String cellVal = "";
            	
            	for (int i=0; i < workbook.getNumberOfSheets(); i++) {
            	//for (int i=0; i < 1; i++) {
            		//Get first sheet from the workbook
                	HSSFSheet sheet = workbook.getSheetAt(i);
                	
                	for (int j=1; j <= sheet.getLastRowNum(); j++) {
                		orderReport = new ImageObject();
                		rowHS = sheet.getRow(j);
                		if (rowHS != null) {
                			
                			//SKU
	                		cellHS = rowHS.getCell(0);
	                		if (cellHS != null) {
	                			
	                			cellHS.setCellType(Cell.CELL_TYPE_STRING);
	                			productID_Trim2_SKU = cellHS.getStringCellValue();
	                			
		                		if (StringUtils.isEmpty(productID_Trim2_SKU)) {
		                			//Can not obtains SKU in csv input
		                			
		                			orderReport.setSku(StringUtils.EMPTY);
			                        
		                			orderReport.setReason("Can not obtains SKU");
		                			orderReport.setLine(String.valueOf(j+1));
		                			orderReportFailList.add(orderReport);
		                			
		                			continue;
		                		}
		                		
		                		try {
		                			
		                			//Link1
		                			cellHS = rowHS.getCell(4);
			                		
			                		if (cellHS != null) {
			                			cellHS.setCellType(Cell.CELL_TYPE_STRING);
			                			cellVal = "";
				                		cellVal = cellHS.getStringCellValue();
				                		
				                		orderReport.setImage1(cellVal);
			                		} else {
			                			orderReport.setSku(StringUtils.EMPTY);
				                        
			                			orderReport.setReason("Link1 null");
			                			orderReport.setLine(String.valueOf(j+1));
			                			orderReportFailList.add(orderReport);
			                			
			                			continue;
			                			
			                		}
			                		
			                		//Link2
		                			cellHS = rowHS.getCell(5);
			                		
			                		if (cellHS != null) {
			                			cellHS.setCellType(Cell.CELL_TYPE_STRING);
			                			cellVal = "";
				                		cellVal = cellHS.getStringCellValue();
				                		
				                		orderReport.setImage2(cellVal);
			                		} else {
			                			orderReport.setSku(StringUtils.EMPTY);
				                        
			                			orderReport.setReason("Link2 null");
			                			orderReport.setLine(String.valueOf(j+1));
			                			orderReportFailList.add(orderReport);
			                			
			                			continue;
			                			
			                		}
			                		
			                		//Link3
		                			cellHS = rowHS.getCell(6);
			                		
			                		if (cellHS != null) {
			                			cellHS.setCellType(Cell.CELL_TYPE_STRING);
			                			cellVal = "";
				                		cellVal = cellHS.getStringCellValue();
				                		
				                		orderReport.setImage3(cellVal);
			                		} else {
			                			orderReport.setSku(StringUtils.EMPTY);
				                        
			                			orderReport.setReason("Link3 null");
			                			orderReport.setLine(String.valueOf(j+1));
			                			orderReportFailList.add(orderReport);
			                			
			                			continue;
			                			
			                		}
			                		
			                		//Link4
		                			cellHS = rowHS.getCell(7);
			                		
			                		if (cellHS != null) {
			                			cellHS.setCellType(Cell.CELL_TYPE_STRING);
			                			cellVal = "";
				                		cellVal = cellHS.getStringCellValue();
				                		
				                		orderReport.setImage4(cellVal);
			                		} else {
			                			orderReport.setSku(StringUtils.EMPTY);
				                        
			                			orderReport.setReason("Link4 null");
			                			orderReport.setLine(String.valueOf(j+1));
			                			orderReportFailList.add(orderReport);
			                			
			                			continue;
			                			
			                		}
			                		
			                		orderReport.setSku(productID_Trim2_SKU);
			                		
			                        orderReportList.add(orderReport);
			                		
		                		} catch (Exception ex) {
		                			//Error occurred in Selenium
		                			
		                			orderReport.setSku(productID_Trim2_SKU);
		                			
		                			orderReport.setReason("Selenium error occurred" + ex.getMessage());
		                			orderReport.setLine(String.valueOf(j+1));
		                			
		                			orderReportFailList.add(orderReport);
		                			
		                			continue;
		                		}
	                		}
                		}
                	}
            	}
        	}
        	
        	file.close();
            
        } catch(Exception ioe) {
            ioe.printStackTrace();
        } finally {
            result.put("SUCCESS", orderReportList);
            result.put("FAIL", orderReportFailList);
        }
        
        return result;
        
    }
}

