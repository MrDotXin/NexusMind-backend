package com.mrdotxin.nexusmind.ai.agent.obsolete;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mrdotxin.nexusmind.ai.persistence.ChatMessageStore;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.mrdotxin.nexusmind.ai.enums.AgentStatusEnum;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public abstract class BaseAgent {

    private String agentName;

    private String systemPrompt;
    private String nextPrompt;

    private AgentStatusEnum status = AgentStatusEnum.IDLE;

    private int maxStep = 10;
    private int currentStep = 0;

    private ChatModel chatModel;

    private List<Advisor> advisors;

    private ChatMessageStore chatMessageStore;

    private Long currentSession;

    private SseEmitter currentSseEmitter;

    public BaseAgent(ChatMessageStore chatMessageStore) {
        this.chatMessageStore = chatMessageStore;
    }

    public ChatClient getChatClient() {
        return ChatClient.builder(chatModel).defaultAdvisors(advisors).build();
    }

    public String run(String userPrompt, Long sessionId) {
        if (ObjectUtils.isEmpty(sessionId)) {
            throw new RuntimeException("sessionId and golem information must not be null");
        }

        if (this.status != AgentStatusEnum.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.status.getValue());
        }

        if (StrUtil.isBlank(this.systemPrompt)) {
            throw new RuntimeException("Cannot run agent with empty system prompt");
        }

        this.status = AgentStatusEnum.RUNNING;

        chatMessageStore.add(sessionId, new UserMessage(userPrompt));

        List<String> results = new ArrayList<>();
        try {
            currentStep = 0;
            while (currentStep < maxStep && this.status != AgentStatusEnum.FINISHED) {
                currentStep ++;

                getInfo();

                String stepResult = step(sessionId);
                String result = "Step " + currentStep + ": " + stepResult;

                results.add(result);
            }

            if (currentStep >= maxStep &&  this.status != AgentStatusEnum.FINISHED) {
                status = AgentStatusEnum.FINISHED;
                currentStep = 0;
                results.add("Terminated: Reached max step " + maxStep);
            }

            this.status = AgentStatusEnum.IDLE;
            return String.join("\n", results);
        } catch (Exception e) {
            this.status = AgentStatusEnum.ERROR;
            log.error(e.getMessage(), e);
            return "执行错误 ! " + e.getMessage();
        } finally {
            this.cleanup();
        }
    }

    private void getInfo() {
        log.info("Current step: {}", currentStep);
    }

    public SseEmitter runStream(String userPrompt, Long sessionId) {

        SseEmitter emitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            try {
                if (ObjectUtils.isEmpty(sessionId)) {
                    emitter.send("sessionId and golem information must not be null");
                    emitter.complete();
                    return;
                }

                if (this.status != AgentStatusEnum.IDLE) {
                    emitter.send("Cannot run agent from state: " + this.status.getValue());
                    emitter.complete();
                    return;
                }

                if (StrUtil.isBlank(this.systemPrompt)) {
                    emitter.send("Cannot run agent with empty system prompt");
                    emitter.complete();
                    return;
                }

                this.status = AgentStatusEnum.RUNNING;
                chatMessageStore.add(sessionId, new UserMessage(userPrompt));
                try {
                    currentStep = 0;
                    while (currentStep < maxStep && this.status == AgentStatusEnum.RUNNING) {
                        currentStep ++;
                        getInfo();

                        String stepResult = stepStream(sessionId, emitter);
                        String result = "Step " + currentStep + ": " + stepResult;

                        log.info(result);
                    }

                    if (currentStep >= maxStep &&  this.status != AgentStatusEnum.FINISHED) {
                        status = AgentStatusEnum.FINISHED;
                        currentStep = 0;
                        emitter.send("Terminated: Reached max step " + maxStep);
                    }

                    emitter.complete();
                } catch (Exception e) {
                    this.status = AgentStatusEnum.ERROR;
                    log.error(e.getMessage());

                    try {
                        emitter.send("执行错误 " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    cleanup();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        emitter.onTimeout(() -> {
            if (this.status == AgentStatusEnum.RUNNING) {
                this.status = AgentStatusEnum.FINISHED;
            }
            this.cleanup();
            log.info("SSE connect completed");
        });

        return emitter;
    }



    protected abstract String step(Long sessionId);

    protected abstract String stepStream(Long sessionId, SseEmitter emitter);

    protected void cleanup() {};
}
