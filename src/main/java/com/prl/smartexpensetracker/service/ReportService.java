package com.prl.smartexpensetracker.service;

import com.prl.smartexpensetracker.dto.ReportSummaryDTO;
import com.prl.smartexpensetracker.entity.Transaction;
import com.prl.smartexpensetracker.entity.User;
import com.prl.smartexpensetracker.exception.ResourceNotFoundException;
import com.prl.smartexpensetracker.repository.TransactionRepository;
import com.prl.smartexpensetracker.repository.UserRepository;
import com.prl.smartexpensetracker.util.BudgetAlertEngine;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetAlertEngine budgetAlertEngine;

    public byte[] generateExcelReport(Long userId) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserUserId(userId);

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Transactions");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Transaction ID");
            headerRow.createCell(1).setCellValue("Amount");
            headerRow.createCell(2).setCellValue("Category");
            headerRow.createCell(3).setCellValue("Description");
            headerRow.createCell(4).setCellValue("Date");

            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

            int rowNum = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(t.getTxnId());
                row.createCell(1).setCellValue(t.getAmount().doubleValue());
                row.createCell(3).setCellValue(t.getDescription());

                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(Timestamp.valueOf(t.getTxnDate().atStartOfDay()));
                dateCell.setCellStyle(dateStyle);
            }

            Sheet summarySheet = workbook.createSheet("Summary");

            BigDecimal monthlyBudget = new BigDecimal("10000");

            ReportSummaryDTO summary = getReportSummary(userId, monthlyBudget);

            summarySheet.createRow(0).createCell(0).setCellValue("Total Spending");
            summarySheet.getRow(0).createCell(1).setCellValue(summary.getTotalSpending().doubleValue());

            summarySheet.createRow(1).createCell(0).setCellValue("Average Transaction");
            summarySheet.getRow(1).createCell(1).setCellValue(summary.getAverageTransaction().doubleValue());

            summarySheet.createRow(2).createCell(0).setCellValue("Transaction Count");
            summarySheet.getRow(2).createCell(1).setCellValue(summary.getTransactionCount());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return out.toByteArray();
        }
    }

    public byte[] generatePdfReport(Long userId) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserUserId(userId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph title = new Paragraph("Expense Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Username: " + user.getUsername(), normalFont));
        document.add(new Paragraph(" "));

        BigDecimal monthlyBudget = new BigDecimal("10000");

        ReportSummaryDTO summary = getReportSummary(userId, monthlyBudget);

        document.add(new Paragraph("Total Spending: ₹" + summary.getTotalSpending(), normalFont));
        document.add(new Paragraph("Average Transaction: ₹" + summary.getAverageTransaction(), normalFont));
        document.add(new Paragraph("Transaction Count: " + summary.getTransactionCount(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        addTableHeader(table, "Transaction ID");
        addTableHeader(table, "Amount");
        addTableHeader(table, "Category");
        addTableHeader(table, "Description");
        addTableHeader(table, "Date");

        for (Transaction t : transactions) {
            table.addCell(String.valueOf(t.getTxnId()));
            table.addCell("₹" + t.getAmount());
            table.addCell(t.getDescription() != null ? t.getDescription() : "N/A");
            table.addCell(t.getTxnDate().toString());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    public ReportSummaryDTO getReportSummary(Long userId, BigDecimal monthlyBudget) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserUserId(userId);

        BigDecimal totalSpending = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTransaction = transactions.isEmpty()
                ? BigDecimal.ZERO
                : totalSpending.divide(BigDecimal.valueOf(transactions.size()), RoundingMode.HALF_UP);

        int transactionCount = transactions.size();

        BigDecimal remainingBudget = budgetAlertEngine.getRemainingBudget(userId, monthlyBudget);
        BigDecimal spendingPercentage = budgetAlertEngine.getSpendingPercentage(userId, monthlyBudget);

        return ReportSummaryDTO.builder()
                .userId(userId)
                .username(user.getUsername())
                .totalSpending(totalSpending)
                .averageTransaction(averageTransaction)
                .transactionCount(transactionCount)
                .monthlyBudget(monthlyBudget)
                .remainingBudget(remainingBudget)
                .spendingPercentage(spendingPercentage)
                .build();
    }

    private void addTableHeader(PdfPTable table, String title) {
        PdfPCell header = new PdfPCell(new Paragraph(title));
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setBorderWidth(2);
        table.addCell(header);
    }
}