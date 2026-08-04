package com.example.demo.service;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Service
public class CsvExportService {

    public byte[] generateCsvReport(List<String> headers, List<Map<String, String>> dataRows) {
        try (StringWriter sw = new StringWriter();
             CSVWriter writer = new CSVWriter(sw)) {
             
            if (headers != null && !headers.isEmpty()) {
                String[] headerArr = headers.toArray(new String[0]);
                writer.writeNext(headerArr);
            }
            
            if (dataRows != null && headers != null) {
                for (Map<String, String> rowMap : dataRows) {
                    String[] rowArr = new String[headers.size()];
                    for (int i = 0; i < headers.size(); i++) {
                        rowArr[i] = rowMap.getOrDefault(headers.get(i), "");
                    }
                    writer.writeNext(rowArr);
                }
            }
            
            writer.flush();
            return sw.toString().getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV report", e);
        }
    }
}
