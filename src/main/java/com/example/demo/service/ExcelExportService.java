package com.example.demo.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public byte[] generateExcelReport(String reportTitle, List<String> headers, List<Map<String, String>> dataRows, Map<String, String> summary) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // 1. Summary Sheet
            if (summary != null && !summary.isEmpty()) {
                Sheet summarySheet = workbook.createSheet("Summary");
                CellStyle boldStyle = workbook.createCellStyle();
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                boldStyle.setFont(boldFont);
                
                Row titleRow = summarySheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(reportTitle + " - Summary");
                titleCell.setCellStyle(boldStyle);
                
                int rowIdx = 2;
                for (Map.Entry<String, String> entry : summary.entrySet()) {
                    Row row = summarySheet.createRow(rowIdx++);
                    Cell keyCell = row.createCell(0);
                    keyCell.setCellValue(entry.getKey());
                    keyCell.setCellStyle(boldStyle);
                    
                    Cell valCell = row.createCell(1);
                    valCell.setCellValue(entry.getValue());
                }
                summarySheet.autoSizeColumn(0);
                summarySheet.autoSizeColumn(1);
            }

            // 2. Detailed Sheet
            if (headers != null && !headers.isEmpty() && dataRows != null) {
                Sheet dataSheet = workbook.createSheet("Details");
                
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // Create headers
                Row headerRow = dataSheet.createRow(0);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(headerStyle);
                }

                // Create data rows
                int rowIdx = 1;
                for (Map<String, String> rowMap : dataRows) {
                    Row row = dataSheet.createRow(rowIdx++);
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(rowMap.getOrDefault(headers.get(i), ""));
                    }
                }

                // Auto-size columns
                for (int i = 0; i < headers.size(); i++) {
                    dataSheet.autoSizeColumn(i);
                }
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
