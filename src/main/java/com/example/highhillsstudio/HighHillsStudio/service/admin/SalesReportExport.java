package com.example.highhillsstudio.HighHillsStudio.service.admin;



import com.example.highhillsstudio.HighHillsStudio.dto.admin.SalesReportDto;

// iText imports
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

// Apache POI imports
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

public class SalesReportExport {


    public static void generateExcel(OutputStream os, SalesReportDto dto) throws java.io.IOException {
        try (Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("Sales Report");

            // Bold style for header
            CellStyle bold = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font excelFont = wb.createFont();
            excelFont.setBold(true);
            bold.setFont(excelFont);

            int r = 0;

            // Title
            Row title = sheet.createRow(r++);
            Cell c0 = title.createCell(0);
            c0.setCellValue("Sales Report");
            c0.setCellStyle(bold);

            // Date range
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            Row range = sheet.createRow(r++);
            range.createCell(0).setCellValue("From: " + dto.getStart().format(df));
            range.createCell(1).setCellValue("To: " + dto.getEnd().format(df));

            r++;

            // Summary metrics header
            Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("Metric");
            header.createCell(1).setCellValue("Value");
            header.getCell(0).setCellStyle(bold);
            header.getCell(1).setCellStyle(bold);

            // Summary metrics rows
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Total Orders");
            row.createCell(1).setCellValue(dto.getTotalOrders());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Total Order Amount");
            row.createCell(1).setCellValue(dto.getTotalOrderAmount().doubleValue());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Total Offer Discount");
            row.createCell(1).setCellValue(dto.getTotalOfferDiscount().doubleValue());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Total Coupon Discount");
            row.createCell(1).setCellValue(dto.getTotalCouponDiscount().doubleValue());

            row = sheet.createRow(r++);
            row.createCell(0).setCellValue("Total Final Revenue");
            row.createCell(1).setCellValue(dto.getTotalFinalRevenue().doubleValue());

            r += 2; // blank rows before orders table

            // Orders table header
            Row ordersHeader = sheet.createRow(r++);
            String[] columns = {"OrderCode", "Date", "Gross", "Offer", "Coupon", "Net"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = ordersHeader.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(bold);
            }

            // Orders rows
            for (var order : dto.getOrders()) {
                Row orderRow = sheet.createRow(r++);
                orderRow.createCell(0).setCellValue(order.getOrderCode());
                orderRow.createCell(1).setCellValue(order.getPlacedAt().format(df));
                orderRow.createCell(2).setCellValue(order.getTotalAmount().doubleValue());
                orderRow.createCell(3).setCellValue(order.getOfferDiscount().doubleValue());
                orderRow.createCell(4).setCellValue(order.getCouponDiscount().doubleValue());
                orderRow.createCell(5).setCellValue(order.getFinalAmount().doubleValue());
            }

            // Autosize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            wb.write(os);
        }
    }


//    Generate Excel sales report using Apache POI
//    public static void generateExcel(OutputStream os, SalesReportDto dto) throws java.io.IOException {
//        try (Workbook wb = new XSSFWorkbook()) {
//
//            Sheet sheet = wb.createSheet("Sales Report");
//
//            // Bold style for header
//            CellStyle bold = wb.createCellStyle();
//            org.apache.poi.ss.usermodel.Font excelFont = wb.createFont(); // POI Font
//            excelFont.setBold(true);
//            bold.setFont(excelFont);
//
//            int r = 0;
//
//            // Title
//            Row title = sheet.createRow(r++);
//            Cell c0 = title.createCell(0);
//            c0.setCellValue("Sales Report");
//            c0.setCellStyle(bold);
//
//            // Date range
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//            Row range = sheet.createRow(r++);
//            range.createCell(0).setCellValue("From: " + dto.getStart().format(df));
//            range.createCell(1).setCellValue("To: " + dto.getEnd().format(df));
//
//            r++;
//
//            // Table header
//            Row header = sheet.createRow(r++);
//            header.createCell(0).setCellValue("Metric");
//            header.createCell(1).setCellValue("Value");
//            header.getCell(0).setCellStyle(bold);
//            header.getCell(1).setCellStyle(bold);
//
//            // Metrics rows
//            Row row = sheet.createRow(r++);
//            row.createCell(0).setCellValue("Total Orders");
//            row.createCell(1).setCellValue(dto.getTotalOrders());
//
//            row = sheet.createRow(r++);
//            row.createCell(0).setCellValue("Total Order Amount");
//            row.createCell(1).setCellValue(dto.getTotalOrderAmount().doubleValue());
//
//            row = sheet.createRow(r++);
//            row.createCell(0).setCellValue("Total Offer Discount");
//            row.createCell(1).setCellValue(dto.getTotalOfferDiscount().doubleValue());
//
//            row = sheet.createRow(r++);
//            row.createCell(0).setCellValue("Total Coupon Discount");
//            row.createCell(1).setCellValue(dto.getTotalCouponDiscount().doubleValue());
//
//            row = sheet.createRow(r++);
//            row.createCell(0).setCellValue("Total Final Revenue");
//            row.createCell(1).setCellValue(dto.getTotalFinalRevenue().doubleValue());
//
//            // Autosize columns
//            sheet.autoSizeColumn(0);
//            sheet.autoSizeColumn(1);
//
//            wb.write(os);
//        }
//    }

    /**
     * Generate PDF sales report using iText 5
     */
//    public static void generatePdf(OutputStream os, SalesReportDto dto) throws java.io.IOException {
//        Document document = new Document();
//        try {
//            PdfWriter.getInstance(document, os);
//            document.open();
//
//            // Header Font
//            Font headerFont = new Font(FontFamily.HELVETICA, 16, Font.BOLD);
//
//            // Title
//            document.add(new Paragraph("Sales Report", headerFont));
//
//            // Date range
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//            document.add(new Paragraph("From: " + dto.getStart().format(df)));
//            document.add(new Paragraph("To: " + dto.getEnd().format(df)));
//
//            document.add(Chunk.NEWLINE);
//
//            // Metrics
//            document.add(new Paragraph("Total Orders: " + dto.getTotalOrders()));
//            document.add(new Paragraph("Total Order Amount: " + dto.getTotalOrderAmount()));
//            document.add(new Paragraph("Total Offer Discount: " + dto.getTotalOfferDiscount()));
//            document.add(new Paragraph("Total Coupon Discount: " + dto.getTotalCouponDiscount()));
//            document.add(new Paragraph("Total Final Revenue: " + dto.getTotalFinalRevenue()));
//
//        } catch (DocumentException e) {
//            throw new java.io.IOException(e);
//        } finally {
//            document.close();
//        }
//    }
//}
    public static void generatePdf(OutputStream os, SalesReportDto dto) throws java.io.IOException {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, os);
            document.open();

            // Fonts
            Font headerFont = new Font(FontFamily.HELVETICA, 16, Font.BOLD);
            Font boldFont = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(FontFamily.HELVETICA, 12);

            // Title
            document.add(new Paragraph("Sales Report", headerFont));
            document.add(Chunk.NEWLINE);

            // Date Range
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            document.add(new Paragraph("From: " + dto.getStart().format(df), normalFont));
            document.add(new Paragraph("To: " + dto.getEnd().format(df), normalFont));
            document.add(Chunk.NEWLINE);

            // Summary Metrics
            document.add(new Paragraph("Total Orders: " + dto.getTotalOrders(), normalFont));
            document.add(new Paragraph("Total Order Amount: ₹" + dto.getTotalOrderAmount(), normalFont));
            document.add(new Paragraph("Total Offer Discount: ₹" + dto.getTotalOfferDiscount(), normalFont));
            document.add(new Paragraph("Total Coupon Discount: ₹" + dto.getTotalCouponDiscount(), normalFont));
            document.add(new Paragraph("Total Final Revenue: ₹" + dto.getTotalFinalRevenue(), normalFont));
            document.add(Chunk.NEWLINE);

            // Orders Table
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(6); // 6 columns
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 2f, 2f, 2f, 2f});

            // Table Header
            String[] headers = {"OrderCode", "Date", "Gross", "Offer", "Coupon", "Net"};
            for (String h : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new Paragraph(h, boldFont));
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Rows
            for (var order : dto.getOrders()) {
                table.addCell(new Paragraph(order.getOrderCode(), normalFont));
                table.addCell(new Paragraph(order.getPlacedAt().format(df), normalFont));
                table.addCell(new Paragraph(order.getTotalAmount().toString(), normalFont));
                table.addCell(new Paragraph(order.getOfferDiscount().toString(), normalFont));
                table.addCell(new Paragraph(order.getCouponDiscount().toString(), normalFont));
                table.addCell(new Paragraph(order.getFinalAmount().toString(), normalFont));
            }

            document.add(table);

        } catch (DocumentException e) {
            throw new java.io.IOException(e);
        } finally {
            document.close();
        }
    }
}




