package com.blog.controller;
import com.blog.model.User;
import com.blog.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/posts/excel")
    public void exportPostsToExcel(HttpSession session, 
                                  HttpServletResponse response,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return;
        }

        try {
            byte[] excelData = exportService.exportPostsToExcel(user);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", 
                             "attachment; filename=" + user.getUsername() + "_posts.xlsx");
            response.setContentLength(excelData.length);

            response.getOutputStream().write(excelData);
            response.getOutputStream().flush();

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to export posts to Excel");
        }
    }

    // NEW: Export all posts to CSV
    @GetMapping("/posts/csv")
    public void exportPostsToCSV(HttpSession session, 
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return;
        }

        try {
            String csvData = exportService.exportPostsToCSV(user);

            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", 
                             "attachment; filename=" + user.getUsername() + "_posts.csv");
            response.setContentLength(csvData.getBytes().length);

            response.getWriter().write(csvData);
            response.getWriter().flush();

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to export posts to CSV");
        }
    }

    @GetMapping("/posts/{id}/word")
    public void exportPostToWord(@PathVariable Long id,
                                HttpSession session,
                                HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return;
        }

        try {
            byte[] wordData = exportService.exportPostToWord(id);

            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", 
                             "attachment; filename=post_" + id + ".docx");
            response.setContentLength(wordData.length);

            response.getOutputStream().write(wordData);
            response.getOutputStream().flush();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to export post to Word");
        }
    }

    @GetMapping("/posts/pdf")
    public void exportPostsToPdf(HttpSession session, 
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return;
        }

        try {
            byte[] pdfData = exportService.exportPostsToPdf(user);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", 
                             "attachment; filename=" + user.getUsername() + "_posts.pdf");
            response.setContentLength(pdfData.length);

            response.getOutputStream().write(pdfData);
            response.getOutputStream().flush();

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to export posts to PDF");
        }
    }

    @GetMapping("/posts/{id}/pdf")
    public void exportPostToPdf(@PathVariable Long id,
                                HttpSession session,
                                HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return;
        }

        try {
            byte[] pdfData = exportService.exportPostToPdf(id);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", 
                             "attachment; filename=post_" + id + ".pdf");
            response.setContentLength(pdfData.length);

            response.getOutputStream().write(pdfData);
            response.getOutputStream().flush();

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to export post to PDF");
        }
    }
}