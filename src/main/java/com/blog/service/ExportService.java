package com.blog.service;

import com.blog.model.Post;
import com.blog.model.User;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportService {
    
    @Autowired
    private PostService postService;
    
    public byte[] exportPostsToExcel(User author) throws IOException {
        List<Post> posts = postService.getUserPosts(author);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Blog Posts");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Title", "Summary", "Tags", "Published", "Created Date", "Word Count"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Fill data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowNum = 1;
            
            for (Post post : posts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(post.getId());
                row.createCell(1).setCellValue(post.getTitle());
                row.createCell(2).setCellValue(post.getSummary() != null ? post.getSummary() : "");
                row.createCell(3).setCellValue(post.getTags() != null ? post.getTags() : "");
                row.createCell(4).setCellValue(post.getIsPublished() ? "Yes" : "No");
                row.createCell(5).setCellValue(post.getCreatedAt().format(formatter));
                
                // Calculate word count
                String cleanContent = post.getContent().replaceAll("<[^>]*>", "").trim();
                int wordCount = cleanContent.isEmpty() ? 0 : cleanContent.split("\\s+").length;
                row.createCell(6).setCellValue(wordCount);
            }
            
            // Auto-resize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    // Enhanced CSV export using OpenCSV library
    public String exportPostsToCSV(User author) throws IOException {
        List<Post> posts = postService.getUserPosts(author);
        StringWriter stringWriter = new StringWriter();
        
        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            // Write header
            String[] header = {"ID", "Title", "Summary", "Tags", "Published", "Created Date", "Updated Date", "Word Count", "Character Count"};
            csvWriter.writeNext(header);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            // Write data rows
            for (Post post : posts) {
                String cleanContent = post.getContent().replaceAll("<[^>]*>", "").trim();
                int wordCount = cleanContent.isEmpty() ? 0 : cleanContent.split("\\s+").length;
                int charCount = cleanContent.length();
                
                String[] row = {
                    post.getId().toString(),
                    post.getTitle(),
                    post.getSummary() != null ? post.getSummary() : "",
                    post.getTags() != null ? post.getTags() : "",
                    post.getIsPublished() ? "Yes" : "No",
                    post.getCreatedAt().format(formatter),
                    post.getUpdatedAt().format(formatter),
                    String.valueOf(wordCount),
                    String.valueOf(charCount)
                };
                csvWriter.writeNext(row);
            }
        }
        
        return stringWriter.toString();
    }
    
    public byte[] exportPostToWord(Long postId) throws IOException {
        Post post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        try (XWPFDocument document = new XWPFDocument()) {
            // Title
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(post.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            
            // Author and date
            XWPFParagraph metaParagraph = document.createParagraph();
            XWPFRun metaRun = metaParagraph.createRun();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            metaRun.setText("By " + post.getAuthor().getFullName() + 
                           " | " + post.getCreatedAt().format(formatter));
            metaRun.setItalic(true);
            
            // Add spacing
            document.createParagraph();
            
            // Summary
            if (post.getSummary() != null && !post.getSummary().isEmpty()) {
                XWPFParagraph summaryParagraph = document.createParagraph();
                XWPFRun summaryRun = summaryParagraph.createRun();
                summaryRun.setText("Summary: " + post.getSummary());
                summaryRun.setItalic(true);
                document.createParagraph(); // Add spacing
            }
            
            // Content (strip HTML tags for Word export)
            XWPFParagraph contentParagraph = document.createParagraph();
            XWPFRun contentRun = contentParagraph.createRun();
            String cleanContent = post.getContent().replaceAll("<[^>]*>", "");
            contentRun.setText(cleanContent);
            
            // Tags
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                document.createParagraph();
                XWPFParagraph tagsParagraph = document.createParagraph();
                XWPFRun tagsRun = tagsParagraph.createRun();
                tagsRun.setText("Tags: " + post.getTags());
                tagsRun.setItalic(true);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportPostsToPdf(User author) throws IOException {
        List<Post> posts = postService.getUserPosts(author);
        StringBuilder html = new StringBuilder();
        
        // Enhanced HTML structure with better styling
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: 'Arial', sans-serif; margin: 40px; line-height: 1.6; color: #333; }");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append(".post { margin-bottom: 40px; page-break-inside: avoid; }");
        html.append(".meta { color: #7f8c8d; font-size: 14px; margin-bottom: 15px; }");
        html.append(".tags { background: #ecf0f1; padding: 8px; border-radius: 4px; margin-top: 10px; }");
        html.append(".summary { background: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 15px 0; font-style: italic; }");
        html.append("hr { border: none; height: 2px; background: #bdc3c7; margin: 30px 0; }");
        html.append("</style></head><body>");
        
        html.append("<h1>Blog Posts Collection - ").append(author.getFullName()).append("</h1>");
        html.append("<p class='meta'>Generated on: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"))).append("</p>");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        for (Post post : posts) {
            html.append("<div class='post'>");
            html.append("<h2>").append(escapeHtml(post.getTitle())).append("</h2>");
            html.append("<p class='meta'><strong>By:</strong> ").append(escapeHtml(post.getAuthor().getFullName()))
                .append(" | <strong>Created:</strong> ").append(post.getCreatedAt().format(formatter))
                .append(" | <strong>Status:</strong> ").append(post.getIsPublished() ? "Published" : "Draft").append("</p>");
            
            if (post.getSummary() != null && !post.getSummary().trim().isEmpty()) {
                html.append("<div class='summary'><strong>Summary:</strong> ").append(escapeHtml(post.getSummary())).append("</div>");
            }
            
            html.append("<div class='content'>").append(post.getContent()).append("</div>");
            
            if (post.getTags() != null && !post.getTags().trim().isEmpty()) {
                html.append("<div class='tags'><strong>Tags:</strong> ").append(escapeHtml(post.getTags())).append("</div>");
            }
            html.append("</div><hr>");
        }

        html.append("</body></html>");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConverterProperties properties = new ConverterProperties();
        HtmlConverter.convertToPdf(html.toString(), baos, properties);
        return baos.toByteArray();
    }

    public byte[] exportPostToPdf(Long postId) throws IOException {
        Post post = postService.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: 'Arial', sans-serif; margin: 40px; line-height: 1.6; color: #333; }");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }");
        html.append(".meta { color: #7f8c8d; font-size: 14px; margin-bottom: 20px; }");
        html.append(".summary { background: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 15px 0; font-style: italic; }");
        html.append(".tags { background: #ecf0f1; padding: 8px; border-radius: 4px; margin-top: 20px; }");
        html.append("</style></head><body>");
        
        html.append("<h1>").append(escapeHtml(post.getTitle())).append("</h1>");
        html.append("<p class='meta'><strong>By:</strong> ").append(escapeHtml(post.getAuthor().getFullName()))
            .append(" | <strong>Date:</strong> ").append(post.getCreatedAt().format(formatter)).append("</p>");
        
        if (post.getSummary() != null && !post.getSummary().trim().isEmpty()) {
            html.append("<div class='summary'><strong>Summary:</strong> ").append(escapeHtml(post.getSummary())).append("</div>");
        }
        
        html.append("<div class='content'>").append(post.getContent()).append("</div>");
        
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            html.append("<div class='tags'><strong>Tags:</strong> ").append(escapeHtml(post.getTags())).append("</div>");
        }
        html.append("</body></html>");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ConverterProperties properties = new ConverterProperties();
        HtmlConverter.convertToPdf(html.toString(), baos, properties);
        return baos.toByteArray();
    }
    
    // Helper method to escape HTML characters
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
}