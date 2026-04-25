package com.prl.smartexpensetracker.service;

import com.prl.smartexpensetracker.dto.ReportSummaryDTO;
import com.prl.smartexpensetracker.entity.Transaction;
import com.prl.smartexpensetracker.entity.User;
import com.prl.smartexpensetracker.exception.ResourceNotFoundException;
import com.prl.smartexpensetracker.repository.TransactionRepository;
import com.prl.smartexpensetracker.repository.UserRepository;
import com.prl.smartexpensetracker.util.BudgetAlertEngine;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetAlertEngine budgetAlertEngine;

    /**
     * Generate Excel report for user transactions.
     *
     * @param userId User ID
     * @return Byte array of Excel file
     */
    public byte[] generateExcelReport(Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Transaction ID");
            headerRow.createCell(1).setCellValue("Amount");
            headerRow.createCell(2).setCellValue("Category");
            headerRow.createCell(3).setCellValue("Description");
            headerRow.createCell(4).setCellValue("Date");

            // Populate data rows
            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getTxnId());
                row.createCell(1).setCellValue(transaction.getAmount().doubleValue());
                row.createCell(2).setCellValue(transaction.getCategory().getCategoryName());
                row.createCell(3).setCellValue(transaction.getDescription());
                row.createCell(4).setCellValue(transaction.getTxnDate().toString());
            }

            // Add summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            ReportSummaryDTO summary = getReportSummary(userId, new BigDecimal("10000"));

            summarySheet.createRow(0).createCell(0).setCellValue("Total Spending");
            summarySheet.getRow(0).createCell(1).setCellValue(summary.getTotalSpending().doubleValue());

            summarySheet.createRow(1).createCell(0).setCellValue("Average Transaction");
            summarySheet.getRow(1).createCell(1).setCellValue(summary.getAverageTransaction().doubleValue());

            summarySheet.createRow(2).createCell(0).setCellValue("Transaction Count");
            summarySheet.getRow(2).createCell(1).setCellValue(summary.getTransactionCount());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generate PDF report for user transactions.
     *
     * @param userId User ID
     * @return Byte array of PDF file
     */
    public byte[] generatePdfReport(Long userId) throws DocumentException, IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Title
        Paragraph title = new Paragraph("Expense Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // User Info
        Paragraph userInfo = new Paragraph("Username: " + user.getUsername(),
                FontFactory.getFont(FontFactory.HELVETICA, 12));
        document.add(userInfo);

        // Summary
        ReportSummaryDTO summary = getReportSummary(userId, new BigDecimal("10000"));
        document.add(new Paragraph("Total Spending: $" + summary.getTotalSpending(),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(new Paragraph("Average Transaction: $" + summary.getAverageTransaction(),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(new Paragraph("Transaction Count: " + summary.getTransactionCount(),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(new Paragraph(" "));

        // Transactions Table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        // Header cells
        addTableHeader(table, "Transaction ID");
        addTableHeader(table, "Amount");
        addTableHeader(table, "Category");
        addTableHeader(table, "Description");
        addTableHeader(table, "Date");

        // Data rows
        for (Transaction transaction : transactions) {
            table.addCell(String.valueOf(transaction.getTxnId()));
            table.addCell("$" + transaction.getAmount());
            table.addCell(transaction.getCategory().getCategoryName());
            table.addCell(transaction.getDescription() != null ? transaction.getDescription() : "N/A");
            table.addCell(transaction.getTxnDate().toString());
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    /**
     * Get report summary for a user.
     *
     * @param userId        User ID
     * @param monthlyBudget Monthly budget limit
     * @return Report summary DTO
     */
    public ReportSummaryDTO getReportSummary(Long userId, BigDecimal monthlyBudget) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal totalSpending = transactionRepository.getTotalAmountByUser(userId);
        BigDecimal averageTransaction = transactionRepository.getAverageAmountByUser(userId);
        int transactionCount = transactionRepository.findByUserId(userId).size();

        BigDecimal remainingBudget = budgetAlertEngine.getRemainingBudget(userId, monthlyBudget);
        BigDecimal spendingPercentage = budgetAlertEngine.getSpendingPercentage(userId, monthlyBudget);

        return ReportSummaryDTO.builder()
                .userId(userId)
                .username(user.getUsername())
                .totalSpending(totalSpending != null ? totalSpending : BigDecimal.ZERO)
                .averageTransaction(averageTransaction != null ? averageTransaction : BigDecimal.ZERO)
                .transactionCount(transactionCount)
                .monthlyBudget(monthlyBudget)
                .remainingBudget(remainingBudget)
                .spendingPercentage(spendingPercentage)
                .build();
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell(new Phrase(headerTitle));
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setBorderWidth(2);
        table.addCell(header);
    }
}
