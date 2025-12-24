package com.subcycle.service;

import com.subcycle.dto.SubscriptionResponse;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 導出訂閱數據為 Excel
     */
    public byte[] exportSubscriptionsToExcel(List<SubscriptionResponse> subscriptions, String username)
            throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("訂閱列表");

        // 創建樣式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle priceStyle = workbook.createCellStyle();
        // priceStyle.setDataFormat(workbook.createDataFormat().getFormat("\"$\"#,##0.00"));

        // 標題行
        Row titleRow = sheet.createRow(0);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("訂閱列表報告");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        // 信息行
        Row infoRow = sheet.createRow(1);
        infoRow.createCell(0).setCellValue("用戶: " + username);
        infoRow.createCell(3).setCellValue("匯出日期: " + java.time.LocalDate.now().format(DATE_FORMATTER));

        // 表頭
        Row headerRow = sheet.createRow(3);
        String[] headers = { "名稱", "類別", "金額(NT$)", "週期", "下次付款日" };
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 數據行
        int rowNum = 4;

        for (SubscriptionResponse sub : subscriptions) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(sub.getName());
            row.createCell(1).setCellValue(sub.getCategory() != null ? sub.getCategory().getName() : "");
            if (sub.getPrice() != null) {
                org.apache.poi.ss.usermodel.Cell priceCell = row.createCell(2);
                priceCell.setCellValue(sub.getPrice().doubleValue());
                priceCell.setCellStyle(priceStyle);
            } else {
                row.createCell(2).setCellValue("");
            }
            row.createCell(3).setCellValue(formatBillingCycle(sub.getBillingCycle()));

            if (sub.getNextPaymentDate() != null) {
                row.createCell(4).setCellValue(sub.getNextPaymentDate().format(DATE_FORMATTER));
            } else {
                row.createCell(4).setCellValue("-");
            }
        }

        // 自動調整列寬
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            int paddedWidth = Math.min(currentWidth + 2048, 255 * 256);
            sheet.setColumnWidth(i, paddedWidth);
        }

        // 寫入到 ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    private String formatBillingCycle(String cycle) {
        if (cycle == null) {
            return "";
        }
        return switch (cycle.toLowerCase()) {
            case "daily" -> "每日";
            case "weekly" -> "每週";
            case "monthly" -> "每月";
            case "quarterly" -> "每季";
            case "yearly" -> "每年";
            default -> cycle;
        };
    }

}
