package com.mrdotxin.nexusmind.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mrdotxin.nexusmind.ai.advisor.CustomLogAdvisor;
import com.mrdotxin.nexusmind.ai.agent.ToolCallStreamChatAgent;
import com.mrdotxin.nexusmind.ai.persistence.ChatMessageStore;
import com.mrdotxin.nexusmind.ai.tool.ToolCenter;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.component.ChatModelSelector;
import com.mrdotxin.nexusmind.component.enums.ChatModelEnum;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.chat.DoChatRequest;
import com.mrdotxin.nexusmind.model.dto.chat.DoChatResponse;
import com.mrdotxin.nexusmind.model.entity.ChatSession;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.service.ChatService;
import com.mrdotxin.nexusmind.service.ChatSessionService;
import com.mrdotxin.nexusmind.service.GolemService;
import com.mrdotxin.nexusmind.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController("/chat")
public class ChatController {

    @Resource
    private UserService userService;

    @Resource
    private GolemService golemService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ChatService chatService;

    @Resource
    @Qualifier("pgVectorStore")
    private VectorStore pgVectorStore;

    @Resource
    private ChatModelSelector chatModelSelector;

    @Value("${maxAllowedContentLength}")
    private Long maxAllowedContentLength;

    @PostMapping("/golem/{golemId}")
    public BaseResponse<DoChatResponse> doGolemChat(@PathVariable("golemId") Long golemId, @RequestBody DoChatRequest doChatRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(doChatRequest), ErrorCode.PARAMS_ERROR, "请求体为空!");

        Golem golem = golemService.getById(golemId);    
        User user = userService.getLoginUser(httpServletRequest);

        validatorChatRequestParam(golem, user, doChatRequest);

        String result = chatService.doChat(golem, doChatRequest, user);
        ThrowUtils.throwIf(StrUtil.isBlank(result), ErrorCode.OPERATION_ERROR, "无法回复，请重试");

        DoChatResponse doChatResponse = new DoChatResponse();
        doChatResponse.setContent(result);
        doChatResponse.setSessionId(doChatRequest.getSessionId());
        return ResultUtils.success(doChatResponse);
    }

    @PostMapping(value = "/golem/async/{golemId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doGolemChatAsync(@PathVariable("golemId") Long golemId, @RequestBody DoChatRequest doChatRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(doChatRequest), ErrorCode.PARAMS_ERROR, "请求体为空!");

        Golem golem = golemService.getById(golemId);
        User user = userService.getLoginUser(httpServletRequest);
        validatorChatRequestParam(golem, user, doChatRequest);

        return chatService.doChatAsync(golem, doChatRequest, user);
    }

    private void validatorChatRequestParam(Golem golem, User user, DoChatRequest doChatRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(golem), ErrorCode.OPERATION_ERROR, "请求的智能体不存在!");

        String content = doChatRequest.getContent();
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "消息为空");

        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);

        if (ObjectUtil.isNotNull(doChatRequest.getSessionId()) && doChatRequest.getSessionId() > 0) {
            ChatSession chatSession = chatSessionService.getById(doChatRequest.getSessionId());
            ThrowUtils.throwIf(ObjectUtil.isNull(chatSession), ErrorCode.PARAMS_ERROR, "不存在的会话!");
            ThrowUtils.throwIf(!golem.getId().equals(chatSession.getGolemId()), ErrorCode.PARAMS_ERROR, "不存在的会话!");
        }
    }

    @GetMapping(
            value = "manus/async/mock/{userId}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter doManusAgentMock(@RequestParam("prompt") String prompt, @PathVariable("userId") Long userId) {
        String[] responses = {
                "你好，我是AI助手",
                "你刚才说的是: " + prompt,
                "这是第" + (1) + "条回复",
                "当前会话ID: " + 1,
                "流式传输测试完成"
            };

        SseEmitter sseEmitter = new SseEmitter();
        CompletableFuture.runAsync(() -> {
            try {
                for (String response : responses) {
                    Thread.sleep(2000);
                    sseEmitter.send(response);
                }
                sseEmitter.complete();
                } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
        });

        return sseEmitter;
    }

    @PostMapping(
        value = "golem/async/mock/{golemId}/{userId}",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<BaseResponse<ChatResponse>> mockChatStream(
        @PathVariable Long golemId,
        @PathVariable Long userId,
        @RequestBody DoChatRequest request
    ) {
        // 验证参数 (示例)
        if (golemId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Invalid ID");
        }

        // 模拟流式响应
        return Flux.interval(Duration.ofMillis(3000)) // 每300ms发送一个消息
            .map(sequence -> {
                // 模拟AI思考过程
                String[] responses = {
                    "你好，我是AI助手",
                    "你刚才说的是: " + request.getContent(),
                    "这是第" + (sequence + 1) + "条回复",
                    "当前会话ID: " + request.getSessionId(),
                    "流式传输测试完成"
                };

                return ResultUtils.success(new ChatResponse(
                        List.of(new Generation(
                                        new AssistantMessage(responses[(int) (sequence % responses.length)])
                                )
                        )));
            })
            .take(5) // 只发送5条消息后结束
            .delayElements(Duration.ofMillis(1000)); // 每条消息延迟100ms
    }

    @Resource
    private ToolCenter toolCenter;

    @Resource
    private ChatMessageStore chatMessageStore;
    @PostMapping("/mock/test/chat/stream")
    public SseEmitter mockChatStream(String text, HttpServletRequest httpServletRequest) {
        Golem golem = golemService.getById(2L);

        User user = userService.getLoginUser(httpServletRequest);
        ChatSession chatSession = chatService.newChatSession(user, golem, text);

        ChatModel chatModel = chatModelSelector.select(ChatModelEnum.QWEN.getValue());
        ToolCallStreamChatAgent.Builder chatBuilder = new ToolCallStreamChatAgent.Builder(
                chatModel,
                golem
        );

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMessageStore)
                        .maxMessages(20)
                                .build();


        ToolCallbackProvider tools = toolCenter.getAllTools();

        CustomLogAdvisor customLogAdvisor = new CustomLogAdvisor();

        ToolCallStreamChatAgent agent = chatBuilder
                .chatMemory(chatMemory, chatSession.getId().toString())
                .advisor(customLogAdvisor)
                .toolCalls(tools)
                .build();

        return agent.doChatStream(text);
    }
}
