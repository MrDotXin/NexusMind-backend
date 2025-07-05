package com.mrdotxin.nexusmind.ai.tool.component;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.mrdotxin.nexusmind.constant.FileConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*

    根据传入的内容生成 PDF 文档
    */
public class PDFGenerationTool {
    private static final Logger logger = LoggerFactory.getLogger(PDFGenerationTool.class);

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
        @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
        @ToolParam(description = "Content to be included in the PDF") String content
    ) {
        try {
            // 使用 Path 类处理文件路径
            Path fileDir = Paths.get (FileConstant.FILE_SAVE_DIR, "pdf");
            Path filePath = fileDir.resolve (fileName + ".pdf");
            logger.info("尝试生成 PDF 文件: {}", filePath);
            // 确保目录存在
            Files.createDirectories (fileDir);
            // 检查目录是否成功创建
            if (!Files.exists (fileDir)) {
                throw new IOException ("无法创建目录:" + fileDir);
            }
            // 检查目录是否可写
            if (!Files.isWritable (fileDir)) {
                throw new IOException ("目录不可写:" + fileDir);
            }
            // 创建 PdfWriter 和 PdfDocument 对象
            try (
                    PdfWriter writer = new PdfWriter (filePath.toString ());
                    PdfDocument pdf = new PdfDocument (writer);
                    Document document = new Document (pdf)
            ) {
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont ("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont (font);
                // 创建段落
                Paragraph paragraph = new Paragraph (content);
                // 添加段落并关闭文档
                document.add (paragraph);
                logger.info("PDF 生成成功: {}", filePath);
                return "PDF generated successfully to:" + filePath;
            }
        } catch (IOException e) {
            logger.error ("生成 PDF 时发生错误", e);
            throw new RuntimeException ("生成 PDF 失败:" + e.getMessage (), e);
        }
    }
}