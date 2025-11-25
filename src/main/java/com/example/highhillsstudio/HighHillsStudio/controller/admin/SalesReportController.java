package com.example.highhillsstudio.HighHillsStudio.controller.admin;


import com.example.highhillsstudio.HighHillsStudio.dto.admin.SalesReportDto;
import com.example.highhillsstudio.HighHillsStudio.service.admin.SalesReportExport;
import com.example.highhillsstudio.HighHillsStudio.service.admin.SalesReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/sales")
@RequiredArgsConstructor
public class SalesReportController {

    private final SalesReportService reportService;

    @GetMapping
    public String showPage() {
        return "admin/sales/list";
    }



    @GetMapping("/generate")
    public String generate(
            @RequestParam String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        var range = reportService.computeRange(filter, startDate, endDate);
        SalesReportDto dto = reportService.generateReportPaginated(range[0], range[1], page, size);

        model.addAttribute("report", dto);
        model.addAttribute("filter", filter);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/sales/list";
    }



    @GetMapping("/download/excel")
    public void downloadExcel(
            @RequestParam String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        var range = reportService.computeRange(filter, startDate, endDate);
        SalesReportDto dto = reportService.generateReport(range[0], range[1]);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.xlsx");
        SalesReportExport.generateExcel(response.getOutputStream(), dto);
    }

    @GetMapping("/download/pdf")
    public void downloadPdf(
            @RequestParam String filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        var range = reportService.computeRange(filter, startDate, endDate);
        SalesReportDto dto = reportService.generateReport(range[0], range[1]);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=sales_report.pdf");
        SalesReportExport.generatePdf(response.getOutputStream(), dto);
    }

}
