package com.example.demo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    public byte[] generatePdfReport(String reportTitle, List<String> headers, List<Map<String, String>> dataRows, Map<String, String> summary) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            HeaderFooter footer = new HeaderFooter(new Phrase("Page "), true);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.setFooter(footer);
            
            document.open();

            // Company Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph companyTitle = new Paragraph("Enterprise UPI Simulator", titleFont);
            companyTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(companyTitle);

            // Report Title
            Font reportTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph reportTitlePara = new Paragraph(reportTitle, reportTitleFont);
            reportTitlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(reportTitlePara);

            // Generated Timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Paragraph timestampPara = new Paragraph("Generated at: " + LocalDateTime.now().format(formatter));
            timestampPara.setAlignment(Element.ALIGN_RIGHT);
            timestampPara.setSpacingAfter(10);
            document.add(timestampPara);

            // Summary Section
            if (summary != null && !summary.isEmpty()) {
                document.add(new Paragraph("Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                PdfPTable summaryTable = new PdfPTable(2);
                summaryTable.setWidthPercentage(50);
                summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                summaryTable.setSpacingBefore(5);
                summaryTable.setSpacingAfter(15);
                
                for (Map.Entry<String, String> entry : summary.entrySet()) {
                    summaryTable.addCell(new Phrase(entry.getKey(), FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                    summaryTable.addCell(new Phrase(entry.getValue()));
                }
                document.add(summaryTable);
            }

            // Detailed Table
            if (headers != null && !headers.isEmpty() && dataRows != null) {
                PdfPTable dataTable = new PdfPTable(headers.size());
                dataTable.setWidthPercentage(100);
                dataTable.setSpacingBefore(10);
                
                // Headers
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                    dataTable.addCell(cell);
                }
                
                // Data Rows
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                for (Map<String, String> row : dataRows) {
                    for (String header : headers) {
                        PdfPCell cell = new PdfPCell(new Phrase(row.getOrDefault(header, ""), dataFont));
                        dataTable.addCell(cell);
                    }
                }
                
                document.add(dataTable);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }
}
