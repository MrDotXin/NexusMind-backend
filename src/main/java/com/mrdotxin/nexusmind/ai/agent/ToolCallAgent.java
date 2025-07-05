package com.mrdotxin.nexusmind.ai.agent;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.mrdotxin.nexusmind.ai.advisor.CustomLogAdvisor;
import com.mrdotxin.nexusmind.ai.persistence.ChatMessageStore;
import com.mrdotxin.nexusmind.ai.tool.ToolCenter;
import com.mrdotxin.nexusmind.ai.tool.component.TerminateTool;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.ai.enums.AgentStatusEnum;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent{

    // 保存了工具调用信息的响应

    private ChatResponse toolCallChatResponse;

    private ToolCallingManager toolCallingManager;

    // 禁用工具调用机制, 自己维护上下文
    private ChatOptions chatOptions;

    private ToolCenter toolCenter;

    private ToolCallbackProvider  mcpToolCallbackProvider;

    private TerminateTool specialTool;

    public ToolCallAgent(ToolCenter toolCenter, ToolCallbackProvider mcpTools, ChatMessageStore chatMessageStore) {
        super(chatMessageStore);
        this.toolCenter = toolCenter;
        this.mcpToolCallbackProvider = mcpTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.specialTool = new TerminateTool(this);
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true).build();

    }

    /**
     * 通过推理来判断是否需要调用哪一些工具
     * @return 是否需要继续
     */
    @Override
    boolean think(Long sessionId) {
        ChatMessageStore chatMessageStore = getChatMessageStore();
        if (StrUtil.isNotBlank(getNextPrompt())) {
            UserMessage userMessage = new UserMessage(getNextPrompt());
            chatMessageStore.add(sessionId, userMessage);
        }
        List<Message> messages = chatMessageStore.get(String.valueOf(sessionId), -1);
        Prompt prompt = new Prompt(messages, chatOptions);

        try {
            ChatResponse chatResponse = getChatClientRequestSpec(prompt).call().chatResponse();

            this.toolCallChatResponse = chatResponse;
            if (ObjectUtil.isNull(chatResponse)) {
                throw new RuntimeException("返回结果有误! ");
            }

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info("{}的思考{}", getAgentName(), result);
            log.info("{}选择了{}个工具来使用", getAgentName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s, 参数 %s",
                            toolCall.name(),
                            toolCall.arguments())
                    ).collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCallList.isEmpty()) {
                chatMessageStore.add(sessionId, assistantMessage);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}的思考过程出现了问题 {}", getAgentName(), e.getMessage());
            chatMessageStore.add(
                    sessionId,
                    new AssistantMessage("处理时遇到错误: " + e.getMessage())
            );

            return false;
        }
    }

    @Override
    String act(Long sessionId) {
        return executeToolCallsAndSave(this.toolCallChatResponse, sessionId, null);
    }

    @Override
    String actStream(Long sessionId, SseEmitter sseEmitter) {
         return executeToolCallsAndSave(this.toolCallChatResponse, sessionId, sseEmitter);
    }

    /**
     * 通过推理来判断是否需要调用哪一些工具
     * @return 是否需要继续
     */
    @Override
    boolean thinkStream(Long sessionId, SseEmitter sseEmitter) {
        ChatMessageStore chatMessageStore = getChatMessageStore();
        if (StrUtil.isNotBlank(getNextPrompt())) {
            UserMessage userMessage = new UserMessage(getNextPrompt());
            chatMessageStore.add(sessionId, userMessage);
        }
        List<Message> messages = chatMessageStore.get(String.valueOf(sessionId), -1);
        Prompt prompt = new Prompt(messages, chatOptions);

        try {
            ChatResponse chatResponse = getChatClientRequestSpec(prompt).call().chatResponse();

            this.toolCallChatResponse = chatResponse;
            if (ObjectUtil.isNull(chatResponse)) {
                throw new RuntimeException("返回结果有误! ");
            }

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            String result = assistantMessage.getText();

            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info("{}的思考{}", getAgentName(), result);
            log.info("{}选择了{}个工具来使用", getAgentName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s, 参数 %s",
                            toolCall.name(),
                            toolCall.arguments())
                    ).collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCallList.isEmpty()) {
                chatMessageStore.add(sessionId, assistantMessage);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}的思考过程出现了问题 {}", getAgentName(), e.getMessage());
            chatMessageStore.add(
                    sessionId,
                    new AssistantMessage("处理时遇到错误: " + e.getMessage())
            );

            return false;
        }
    }

    private String executeToolCallsAndSave(ChatResponse chatResponse, Long sessionId, SseEmitter sseEmitter) {
        ChatMessageStore chatMessageStore = getChatMessageStore();

        if (!chatResponse.hasToolCalls()) {
            return "没有工具可以调用";
        }

        Prompt prompt = new Prompt(new ArrayList<>(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
        List<Message> history = toolExecutionResult.conversationHistory();

        Message toolMessage      = history.getLast(); history.removeLast();
        Message assistantMessage = history.getLast(); history.removeLast();

        if (ObjectUtil.isNotNull(sseEmitter)) {
            try {
                sseEmitter.send(ResultUtils.success(assistantMessage));
                sseEmitter.send(ResultUtils.success(toolMessage));
            } catch (IOException e) {
                log.info("无法发送成功! {}", e.getMessage());
            }
        }

        chatMessageStore.add(sessionId, assistantMessage);
        chatMessageStore.add(sessionId, toolMessage);

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolMessage;
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成它的任务! 结果: " + response.responseData())
                .collect(Collectors.joining("\n"));

        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                        .anyMatch(result -> "doTerminate".equals(result.name()));
        if (terminateToolCalled) {
            setStatus(AgentStatusEnum.FINISHED);
        }

        log.info(results);
        return results;
    }

    private ChatClient.ChatClientRequestSpec getChatClientRequestSpec(Prompt prompt) {
        return  getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .advisors(getAdvisors().toArray(Advisor[]::new))
                    .tools(toolCenter.getAllTools()) // 本地通用工具
                    .tools(mcpToolCallbackProvider)    // 远程MCP工具
                    .tools(specialTool);
    }
}
