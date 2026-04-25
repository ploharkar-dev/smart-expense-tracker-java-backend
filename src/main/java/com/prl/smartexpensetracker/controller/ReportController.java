package com.prl.smartexpensetracker.controller;

import com.prl.smartexpensetracker.dto.ReportSummaryDTO;
import com.prl.smartexpensetracker.service.ReportService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestParam Long userId) throws IOException {
        byte[] excelFile = reportService.generateExcelReport(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "expense_report_" + userId + ".xlsx");

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(@RequestParam Long userId) throws IOException, DocumentException {
        byte[] pdfFile = reportService.generatePdfReport(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "expense_report_" + userId + ".pdf");

        return new ResponseEntity<>(pdfFile, headers, HttpStatus.OK);
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<ReportSummaryDTO> getReportSummary(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10000") BigDecimal monthlyBudget) {
        ReportSummaryDTO summary = reportService.getReportSummary(userId, monthlyBudget);
        return ResponseEntity.ok(summary);
    }
}
