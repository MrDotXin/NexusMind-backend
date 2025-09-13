package com.mrdotxin.nexusmind.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mrdotxin.nexusmind.ai.advisor.CustomLogAdvisor;
import com.mrdotxin.nexusmind.ai.agent.BaseAgent;
import com.mrdotxin.nexusmind.ai.agent.ToolCallStreamChatAgent;
import com.mrdotxin.nexusmind.ai.persistence.ChatMessageStore;
import com.mrdotxin.nexusmind.ai.tool.ToolCenter;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.component.ChatModelSelector;
import com.mrdotxin.nexusmind.component.enums.ChatModelEnum;
import com.mrdotxin.nexusmind.constant.ChatConstant;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.chat.DoChatRequest;
import com.mrdotxin.nexusmind.model.entity.ChatSession;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.service.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Resource
    private ChatMessageStore chatMessageStore;

    @Resource
    private UserService userService;

    @Resource
    private GolemService golemService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    @Qualifier("pgVectorStore")
    private VectorStore vectorStore;

    @Value("${userTextAdvise}")
    private String userTextAdvise;

    @Value("${toolCallingAdvise}")
    private String toolCallingAdvise;

    @Value("${maxAllowedContentLength}")
    private Long maxAllowedContentLength;

    @Resource
    private ToolCallbackProvider mcpCallbackProvider;

    @Resource
    private ToolCenter toolCenter;

    @Resource
    private ChatModelSelector chatModelSelector;

    @Resource
    @Qualifier("mysqlTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Override
    public String doChat(Golem golem, DoChatRequest doChatRequest, User user) {

        ChatClient.ChatClientRequestSpec [] chatClientRequestSpec = new ChatClient.ChatClientRequestSpec[1];
        transactionTemplate.executeWithoutResult( transactionStatus -> {
                    if (ObjectUtil.isNull(doChatRequest.getSessionId()) || doChatRequest.getSessionId() == 0) {
                        ChatSession newedChatSession = newChatSession(user, golem, doChatRequest.getContent());
                        doChatRequest.setSessionId(newedChatSession.getId());

                        if (StrUtil.isNotBlank(golem.getPrologue())) {
                            chatMessageStore.add(doChatRequest.getSessionId(), buildAssistantMessageWithPrologue(golem.getPrologue()));
                        }
                    }

                     chatClientRequestSpec[0] = getCall(golem, doChatRequest, user).user(doChatRequest.getContent());
                }
        );

        ChatResponse chatResponse = chatClientRequestSpec[0].call().chatResponse();

       return ObjectUtil.isNotNull(chatResponse) ? chatResponse.getResult().getOutput().getText() : "无法回答, 请重试";
    }

    @Override
    public SseEmitter doChatAsync(Golem golem, DoChatRequest doChatRequest, User user) {
        return transactionTemplate.execute( transactionStatus -> {
                    if (ObjectUtil.isNull(doChatRequest.getSessionId()) || doChatRequest.getSessionId() == 0) {
                        ChatSession newedChatSession = newChatSession(user, golem, doChatRequest.getContent());
                        doChatRequest.setSessionId(newedChatSession.getId());

                        if (StrUtil.isNotBlank(golem.getPrologue())) {
                            chatMessageStore.add(doChatRequest.getSessionId(), buildAssistantMessageWithPrologue(golem.getPrologue()));
                        }
                    }

                     return buildChatAgent(golem, doChatRequest, user).doChatStream(doChatRequest.getContent());
                }
        );
    }

    private ChatClient.ChatClientRequestSpec getCall(Golem golem, DoChatRequest doChatRequest, User user) {
        ChatClient client = buildChatClient(golem, doChatRequest, user);

        return client.prompt()
                .toolCallbacks(toolCenter.getAllTools())
                .advisors(advisorSpec ->
                        advisorSpec.param(ChatMemory.CONVERSATION_ID, doChatRequest.getSessionId())
                );
    }

    private BaseAgent buildChatAgent(Golem golem, DoChatRequest doChatRequest, User user) {
        String sessionId = doChatRequest.getSessionId().toString();
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMessageStore)
                        .maxMessages(20)
                                .build();


        ToolCallbackProvider tools = toolCenter.getAllTools();

        CustomLogAdvisor customLogAdvisor = new CustomLogAdvisor();
        QuestionAnswerAdvisor answerAdvisor = buildRAGAugmentAdvisor(golem, doChatRequest, user);

        ChatModel chatModel = chatModelSelector.select(doChatRequest.getModel());
        ToolCallStreamChatAgent.Builder chatBuilder = new ToolCallStreamChatAgent.Builder(chatModel, golem);

        return chatBuilder.chatMemory(chatMemory, sessionId)
                .advisor(customLogAdvisor, answerAdvisor)
                .toolCalls(tools)
                .build();
    }

    @Override
    public ChatClient buildChatClient(Golem golem, DoChatRequest doChatRequest, User user) {
        SearchRequest searchRequest = golemService.buildSearchRequest(golem, doChatRequest.getContent(), doChatRequest.getExtraRags(), user);

        ChatClient.Builder builder = ChatClient.builder(chatModelSelector.select(doChatRequest.getModel()));
        if (StrUtil.isNotBlank(golem.getSystemPrompt())) {
            builder.defaultSystem(golem.getSystemPrompt());
        }

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMessageStore)
                                .maxMessages(20)
                                        .build();
        CustomLogAdvisor myLoggerAdvisor = new CustomLogAdvisor();
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .promptTemplate(new PromptTemplate(userTextAdvise))
                .build();

         builder.defaultAdvisors(
                        myLoggerAdvisor,
                        messageChatMemoryAdvisor,
                        questionAnswerAdvisor
                );

        return builder.build();
    }

    public QuestionAnswerAdvisor buildRAGAugmentAdvisor(Golem golem, DoChatRequest doChatRequest, User user) {
        SearchRequest searchRequest = golemService.buildSearchRequest(golem, doChatRequest.getContent(), doChatRequest.getExtraRags(), user);
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .promptTemplate(new PromptTemplate(userTextAdvise))
                .build();
    }

    @Override
    public ChatSession newChatSession(User user, Golem golem, String content) {
        ChatSession chatSession = new ChatSession();
        chatSession.setUserId(user.getId());
        chatSession.setGolemId(golem.getId());
        chatSession.setTitle(content.substring(0, Math.min(content.length(), 20)));

        boolean save = chatSessionService.save(chatSession);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建会话失败!");

        return chatSession;
    }

    @Override
    public ChatSession getOrNewChatSession(User user, Golem golem, String content, Long sessionId) {
        ChatSession chatSession = chatSessionService.getById(sessionId);
        if (ObjectUtil.isNull(chatSession)) {
            chatSession = newChatSession(user, golem, content);
        }

        return chatSession;
    }

    private AssistantMessage buildAssistantMessageWithPrologue(String prologue) {
        return new AssistantMessage(prologue);
    }
}
