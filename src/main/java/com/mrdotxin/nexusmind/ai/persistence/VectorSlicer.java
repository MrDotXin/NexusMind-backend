package com.mrdotxin.nexusmind.ai.persistence;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;


public class VectorSlicer {

    public interface SliceStrategy {
        List<Document> doSlice(Resource resource);
    }

    static class PdfSliceStrategy implements SliceStrategy {

        @Override
        public List<Document> doSlice(Resource resource) {
            PdfDocumentReaderConfig readerConfig = PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                            .withNumberOfTopTextLinesToDelete(0)
                            .build())
                    .withPagesPerDocument(1)
                    .build();
            PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource, readerConfig);

            return pagePdfDocumentReader.read();
        }
    }

    static class JsonSliceStrategy implements SliceStrategy {

        @Override
        public List<Document> doSlice(Resource resource) {
            JsonReader jsonReader = new JsonReader(resource);
            return jsonReader.read();
        }
    }

    static class MarkdownSliceStrategy implements SliceStrategy {

        @Override
        public List<Document> doSlice(Resource resource) {
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(true)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("appId", 1)
                .build();

            MarkdownDocumentReader documentParser = new MarkdownDocumentReader(resource, config);
            return documentParser.read();
        }
    }

    static class TextSliceStrategy implements SliceStrategy {

        @Override
        public List<Document> doSlice(Resource resource) {
            TextReader textReader = new TextReader(resource);
            return textReader.read();
        }
    }

    public static SliceStrategy from(String type) {
        return switch (type) {
            case "pdf" -> new PdfSliceStrategy();
            case "json" -> new JsonSliceStrategy();
            case "markdown", "md" -> new MarkdownSliceStrategy();
            case "text", "plain", "txt" -> new TextSliceStrategy();
            default -> throw new IllegalArgumentException("Unsupported slice strategy type: " + type);
        };

    }

    public static List<Document> doSlice(File file, String type) {
        SliceStrategy sliceStrategy = VectorSlicer.from(type);
        Resource resource = new FileSystemResource(file);
        return sliceStrategy.doSlice(resource);
    }

    public static List<Document> doTextSplitter(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(1500, 400, 10, 5000, true);
        return splitter.apply(documents);
    }
}
