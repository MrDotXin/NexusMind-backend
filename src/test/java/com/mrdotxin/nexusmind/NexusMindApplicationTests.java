package com.mrdotxin.nexusmind;

import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduSearchService;
import com.mrdotxin.nexusmind.ai.advisor.CustomLogAdvisor;
import com.mrdotxin.nexusmind.ai.tool.component.PDFGenerationTool;
import com.mrdotxin.nexusmind.ai.tool.component.SearchTools;
import com.mrdotxin.nexusmind.model.entity.ChatLog;
import com.mrdotxin.nexusmind.service.ChatLogService;
import com.mrdotxin.nexusmind.utils.ChatMessageUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest
class NexusMindApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(NexusMindApplicationTests.class);
    @Resource(name = "ollamaChatModel")
    private ChatModel scopeChatModel;

    @Resource(name = "dashscopeChatModel")
    private ChatModel deepseekChatModel;

    @Resource(name = "ollamaEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Test
    void tryChat() {

        ChatClient build = ChatClient.builder(deepseekChatModel)
                .defaultSystem("你是一个资深JOJO厨, 你最专业的就是比较不同替身的战力排行并从各种刁钻角度进行战力比对")
                .build();
        String content = build.prompt().user("替身Gold Experience Requiem 和 替身Wonder Of U哪一个更胜一筹，为什么？").call().content();
        System.out.println(content);
    }

    @Test
    void tryMemorableChat() {

        ChatClient build = ChatClient.builder(deepseekChatModel)
                .build();
        String content = build.prompt().user("替身Gold Experience Requiem 和 替身Wonder Of U哪一个更胜一筹，为什么？").call().content();
        System.out.println(content);
        String content2 = build.prompt().user("那第六部的普奇教父在这两个能力面前岂不是纯路边一条?").call().content();
        System.out.println(content2);
    }


    @Test
    void tryStructuredChat() {

        ChatClient build = ChatClient.builder(deepseekChatModel)
                .defaultSystem("你是一个资深JOJO厨, 你最专业的就是比较不同替身的战力排行并从各种刁钻角度进行战力比对")
                .defaultAdvisors(
                        new CustomLogAdvisor()
                )
                .build();

        record OutputRecorder(String title, String content) {};
        OutputRecorder entity = build.prompt().user("替身Gold Experience Requiem 和 替身Wonder Of U哪一个更胜一筹，为什么？")
                .call()
                .entity(OutputRecorder.class);
        System.out.println(entity);
    }

    @Value("classpath:static/dataset/恋爱常见问题和回答 - 单身篇.md")
    private org.springframework.core.io.Resource dataset;

    private VectorStore getVectorStore() {
        // 载入
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(true)
                .withIncludeBlockquote(false)
                .build();

        MarkdownDocumentReader documentParser = new MarkdownDocumentReader(dataset, config);
        List<Document> documents = documentParser.get();


        // 文本分割
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> documentList = textSplitter.apply(documents);

        // 向量数据库
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(documentList);

        return vectorStore;
    }

    @Test
    void tryRAG() {
        // 载入
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(true)
                .withIncludeBlockquote(false)
                .build();

        MarkdownDocumentReader documentParser = new MarkdownDocumentReader(dataset, config);
        List<Document> documents = documentParser.get();


        // 文本分割
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> documentList = textSplitter.apply(documents);

        // 向量数据库
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(documentList);

        // 查询增强
        ChatClient client = ChatClient.builder(scopeChatModel)
                .defaultSystem("你是一个资深Java库作者，擅长从源码级别解析开源库的用法")
                .build();
        String message = "Kryo库是什么？怎么使用?\n";
        ChatResponse chatResponse = client
                .prompt()
                .user(message)
                .advisors(new CustomLogAdvisor())
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .chatResponse();
        if (chatResponse != null) {
            String content = chatResponse.getResult().getOutput().getText();
            log.info("content {}", content);
        }
    }

    @Test
    void TestSearchFromVectorStore() {
        VectorStore vectorStore = getVectorStore();
        SearchRequest searchRequest = SearchRequest.builder()
                .query("怎么挽回爱情呢?")
                .topK(2)
                .similarityThreshold(0.3)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        System.out.println(documents);
    }

    @Resource
    private VectorStore pgVectorStore;

    @Test
    void testPgVectorStore() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(true)
                    .withIncludeBlockquote(false)
                    .withAdditionalMetadata("appId", 2)
                    .build();

            MarkdownDocumentReader documentParser = new MarkdownDocumentReader(dataset, config);
            List<Document> documents = documentParser.get();

            // 文本分割
            TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                    .withChunkSize(1500)
                    .build();

            List<Document> documentList = textSplitter.apply(documents);

            pgVectorStore.add(documentList);
    }

    @Resource
    private BaiduSearchService baiduSearchService;

    @Test
    void testToolCalling() {
        ChatClient client = ChatClient.builder(scopeChatModel)
            .defaultTools(new SearchTools(baiduSearchService))
            .build();

        String message = "上海今天天气怎么样?\n";
        ChatResponse chatResponse = client
                .prompt()
                .user(message)
                .advisors(new CustomLogAdvisor())
                .call()
                .chatResponse();

        System.out.println(chatResponse);
    }


    @Test
    void testThridpartToolCall() {
        BaiduSearchService.Response resp = baiduSearchService.apply(new BaiduSearchService.Request("上海今日天气", 10));
        log.info("results: {}", resp.results());
    }

    @Test
    void testPdf() {

        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        pdfGenerationTool.generatePDF("1", "你好");
    }

    @Test
    void testDownload() {
        try {
            // 尝试直接执行 where npx 命令查看系统能找到的 npx
            Process whereProcess = new ProcessBuilder("cmd", "/c", "where", "npx").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(whereProcess.getInputStream()));
            String line;
            System.out.println("npx 可执行文件位置:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 然后尝试执行你的命令
            ProcessBuilder pb = new ProcessBuilder("npx.cmd", "-y", "@amap/amap-maps-mcp-server");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出
            BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = processReader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("进程退出码: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Resource
    private ChatLogService chatLogService;
    @Test
    void testMapSaveAndRead() {
        ChatLog chatLog = new ChatLog();
        chatLog.setSessionId(1L);
        chatLog.setSequenceId(1L);
        chatLog.setText("");
        chatLog.setMessageType(MessageType.TOOL.getValue());
        Map<String, Object> metas = new HashMap<>();
        List<ToolResponseMessage.ToolResponse> responses = new ArrayList<>();
        responses.add(new ToolResponseMessage.ToolResponse("1", "2", "AAAA"));
        metas.put("responses", responses);
        chatLog.setMeta(metas);

        boolean save = chatLogService.save(chatLog);
        Long id = chatLog.getId();

        ChatLog another = chatLogService.getById(id);

        ToolResponseMessage message = (ToolResponseMessage) ChatMessageUtil.fromChatLog(chatLog);

    }

    @Test
    void testChatLogRead() {

        ChatLog another = chatLogService.getById(280);

        ToolResponseMessage message = (ToolResponseMessage) ChatMessageUtil.fromChatLog(another);
    }

    @Test
    void testBaiduSearch() {
        BaiduSearchService.Request request = new BaiduSearchService.Request("JO JO", 10);
        BaiduSearchService.Response apply = baiduSearchService.apply(request);
        apply.results();
    }
}
