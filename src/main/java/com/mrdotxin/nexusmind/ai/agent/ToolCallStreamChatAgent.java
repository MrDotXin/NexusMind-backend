package com.mrdotxin.nexusmind.ai.agent;

import cn.hutool.core.util.StrUtil;
import com.mrdotxin.nexusmind.ai.advisor.ChatMemoryAppendAdvisor;
import com.mrdotxin.nexusmind.ai.enums.AgentStatusEnum;
import com.mrdotxin.nexusmind.ai.model.ChatStreamResponse;
import com.mrdotxin.nexusmind.ai.tool.component.TerminateTool;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.mrdotxin.nexusmind.utils.ChatMessageUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.*;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Setter
public class ToolCallStreamChatAgent extends BaseAgent {

    private ChatClient.Builder clientBuilder;

    private ToolCallingChatOptions callingChatOptions;

    private InMemoryChatMemoryRepository temporaryChatMemoryStore;

    private ChatMemory chatMemory;

    private String conversationId;

    private final String NEXT_STEP_PROMPT_HINT = """
        Analyze tool results and context. Then:
        1. Explain key outcomes concisely
        2. Decide next action:
           - If final answer: call `doTerminate`
           - If more steps: choose ONE tool
           - If error: call `doTerminate` with reason
        """;
    private final String NEXT_STEP_PROMPT = """
            ```````````````TOOL CALL RESULT CONTEXT`````````````
               %s
            ````````````````````````````````````````````````````
            %s
            """;
    public ToolCallStreamChatAgent() {}

    public static Builder builder(ChatModel chatModel, Golem golem) {
        return new Builder(chatModel, golem);
    }

    @Override
    public SseEmitter doChatStream(String userPrompt) {
//        return mockStream(userPrompt);
        final ToolCallingManager toolCallingManager = DefaultToolCallingManager.builder().build();
        SseEmitter sseEmitter = new SseEmitter(300000L);

        ChatClient.ChatClientRequestSpec chatClientRequestSpecInit = clientBuilder.build().prompt().user(userPrompt);
        Flux<ChatClientResponse> responseFlux = chatClientRequestSpecInit
                .stream()
                .chatClientResponse()
                .expand(response -> {
                    ChatResponse chatResponse = response.chatResponse();
                    if (chatResponse != null) {
                        if (chatResponse.getResult() != null) {
                            if (chatResponse.getResult().getOutput() != null) {
                                String aiContent = chatResponse.getResult().getOutput().getText();
                                pushMessage(sseEmitter, aiContent);

                                if (!chatResponse.getResult().getOutput().getToolCalls().isEmpty()) {
                                    List<AssistantMessage.ToolCall> toolCalls = chatResponse.getResult().getOutput().getToolCalls();
                                    pushToolCallMessage(sseEmitter, toolCalls);
                                    Prompt prompt = new Prompt(Collections.emptyList(), this.callingChatOptions);

                                    return executeToolsAndRestoreStream(toolCallingManager, prompt, chatResponse);
                                }
                            }
                        }
                    }

                    return Flux.empty();
                });

        responseFlux.doOnError(e -> {
            pushErrorMessage(sseEmitter, e.getMessage());
            sseEmitter.completeWithError(e);
        }).doOnComplete(()-> {
            try {
                sseEmitter.send(ChatStreamResponse.complete());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<Message> store = temporaryChatMemoryStore.findByConversationId(conversationId);
            List<Message> arrangeConversion = arrangeConversion(store);

            chatMemory.add(conversationId, arrangeConversion);

            sseEmitter.complete();
        }).subscribe();

        return sseEmitter;
        
    }

    private Flux<ChatClientResponse> executeToolsAndRestoreStream(ToolCallingManager toolCallingManager, Prompt prompt, ChatResponse chatResponse) {
        return getAgentStatusEnum().equals(AgentStatusEnum.FINISHED) ?
                Flux.empty() :
                Mono.fromCallable(() -> toolCallingManager.executeToolCalls(prompt, chatResponse))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMapMany(toolResult -> {

                        if (getAgentStatusEnum().equals(AgentStatusEnum.FINISHED)) {
                            return Flux.empty();
                        }

                        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolResult.conversationHistory().getLast();
                        String toolCallResult = getToolCallResult(toolResponseMessage.getResponses());

                        String nextPrompt = String.format(NEXT_STEP_PROMPT, toolCallResult, NEXT_STEP_PROMPT_HINT);
                        ChatClient.ChatClientRequestSpec nextStepSpec =
                                clientBuilder.build()
                                        .prompt()
                                        .user(nextPrompt);

                        return nextStepSpec.stream().chatClientResponse();
                    });
    }


    private void pushToolCallMessage(SseEmitter sseEmitter, List<AssistantMessage.ToolCall> toolCallResult) {
        toolCallResult.forEach(toolCall -> {
            String name = toolCall.name();
            String args = toolCall.arguments();
            String type = toolCall.type();
            String callFormat = String.format("%s,%s,%s", name, args, type);

            try {
                sseEmitter.send(ChatStreamResponse.toolCall(callFormat));
            } catch(Exception ignored) {}
        });
    }

    private void pushToolCallResultMessage(SseEmitter sseEmitter, List<ToolResponseMessage.ToolResponse> toolResponseList) {
        toolResponseList.forEach(toolCall -> {
            String name = toolCall.name();
            String responseData = toolCall.responseData();
            String callFormat = String.format("%s,%s", name, responseData);

            try {
                sseEmitter.send(ChatStreamResponse.toolCall(callFormat));
            } catch(Exception ignored) {}
        });
    }

    private void pushMessage(SseEmitter sseEmitter, String msg) {
        try {
            sseEmitter.send(ChatStreamResponse.text(msg));
        } catch(Exception ignored) {}
    }

    private void pushErrorMessage(SseEmitter sseEmitter, String msg) {
        try {
            sseEmitter.send(ChatStreamResponse.error(msg));
        } catch(Exception ignored) {}
    }

    private String getToolCallResult(List<ToolResponseMessage.ToolResponse> toolResponseList) {
        return toolResponseList
                .stream()
                .map(response -> response.name() + " :" + response.responseData())
                .collect(Collectors.joining("\n"));
    }

    private List<Message> arrangeConversion(List<Message> store) {
        UserMessage userMessage = null;
        AssistantMessage assistantMessage = null;
        ToolResponseMessage toolResponseMessage = null;
        // 合并对话流程
        for (Message message : store) {
            if (message instanceof UserMessage) {
                if (userMessage == null) {
                    userMessage = (UserMessage) message;
                }
            }

            if (message instanceof AssistantMessage) {
                if (assistantMessage == null) {
                    assistantMessage = (AssistantMessage) message;
                } else {
                    assistantMessage = ChatMessageUtil.mergeAssistantMessages(assistantMessage, (AssistantMessage) message);
                }
            }

            if (message instanceof ToolResponseMessage) {
                if (toolResponseMessage == null) {
                    toolResponseMessage = (ToolResponseMessage) message;
                } else {
                    toolResponseMessage = ChatMessageUtil.mergeToolResponseMessages(toolResponseMessage, (ToolResponseMessage) message);
                }
            }
        }

        List<Message> result = new ArrayList<>();
        if (userMessage != null) { result.add(userMessage); }
        if (assistantMessage != null) { result.add(assistantMessage);}
        if (toolResponseMessage != null) { result.add(toolResponseMessage); }

        return result;
    }

    private void executeToolCallAndSave(ChatResponse chatResponse, ToolCallingManager toolCallingManager) {
        Prompt prompt = new Prompt(Collections.emptyList(), null);
        ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, chatResponse);
    }

    private SseEmitter mockStream(String prompt) {
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
                    Thread.sleep(1000);
                    sseEmitter.send(ChatStreamResponse.text(response));
                    sseEmitter.send(ChatStreamResponse.toolCall(response));
                }
                sseEmitter.complete();
                } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
        });

        return sseEmitter;
    }

    public static class Builder {
        ChatClient.Builder clientBuilder;
        List<ToolCallback> toolCallbacks;

        ToolCallingChatOptions.Builder callingChatOptionsBuilder;

        InMemoryChatMemoryRepository temporaryChatMemoryStore = null;
        ToolCallStreamChatAgent agent;

        public Builder(ChatModel chatModel, Golem golem) {
            agent = new ToolCallStreamChatAgent();
            agent.setAgentStatusEnum(AgentStatusEnum.IDLE);

            clientBuilder = ChatClient.builder(chatModel);
            toolCallbacks = new ArrayList<>();
            if (StrUtil.isNotBlank(golem.getSystemPrompt())) {
                clientBuilder.defaultSystem(golem.getSystemPrompt());
            }

            callingChatOptionsBuilder = new DefaultToolCallingChatOptions.Builder().internalToolExecutionEnabled(false);

        }

        public ToolCallStreamChatAgent build() {
            toolCallbacks.addAll(List.of(MethodToolCallbackProvider.builder().toolObjects(new TerminateTool(agent)).build().getToolCallbacks()));
            ToolCallingChatOptions callingChatOptions = callingChatOptionsBuilder.toolCallbacks(toolCallbacks).build();
            clientBuilder.defaultOptions(callingChatOptions).build();

            agent.setCallingChatOptions(callingChatOptions);
            agent.setClientBuilder(clientBuilder);
            agent.setTemporaryChatMemoryStore(temporaryChatMemoryStore);

            return agent;
        }

        public Builder toolCalls(ToolCallbackProvider toolCallbackProvider) {
            toolCallbacks.addAll(List.of(toolCallbackProvider.getToolCallbacks()));

            return this;
        }

        public Builder advisor(Advisor... advisors) {
            clientBuilder.defaultAdvisors(advisors);

            return this;
        }

        public Builder advisor(Consumer<ChatClient.AdvisorSpec> consumer) {
            clientBuilder.defaultAdvisors(consumer);

            return this;
        }

        public Builder chatMemory(ChatMemory chatMemory, String conversionId) {
            ChatMemoryAppendAdvisor chatMemoryAppendAdvisor = ChatMemoryAppendAdvisor.builder(chatMemory).order(2).build();

            this.temporaryChatMemoryStore = new InMemoryChatMemoryRepository();
            MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(
                    MessageWindowChatMemory.builder()
                            .chatMemoryRepository(temporaryChatMemoryStore)
                            .maxMessages(20)
                            .build()
            ).order(1).build();
            clientBuilder.defaultAdvisors(chatMemoryAppendAdvisor, messageChatMemoryAdvisor);
            clientBuilder.defaultAdvisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversionId));
            agent.setChatMemory(chatMemory);
            agent.setConversationId(conversionId);

            return this;
        }
    }


}
