package com.mrdotxin.nexusmind.ai.advisor;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 只追加 prompt 但不保存历史记录的聊天记忆顾问
 */
public final class ChatMemoryAppendAdvisor implements BaseChatMemoryAdvisor {
    private final ChatMemory chatMemory;
    private final String defaultConversationId;
    @Getter
    private final int order;
    private final Scheduler scheduler;

    private ChatMemoryAppendAdvisor(ChatMemory chatMemory, String defaultConversationId, int order,
            Scheduler scheduler) {
        Assert.notNull(chatMemory, "chatMemory cannot be null");
        Assert.hasText(defaultConversationId, "defaultConversationId cannot be null or empty");
        Assert.notNull(scheduler, "scheduler cannot be null");
        this.chatMemory = chatMemory;
        this.defaultConversationId = defaultConversationId;
        this.order = order;
        this.scheduler = scheduler;
    }

    @NotNull
    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @NotNull
    public ChatClientRequest before(ChatClientRequest chatClientRequest, @NotNull AdvisorChain advisorChain) {
        String conversationId = this.getConversationId(chatClientRequest.context(), this.defaultConversationId);
        List<Message> memoryMessages = this.chatMemory.get(conversationId);
        List<Message> processedMessages = new ArrayList<>(memoryMessages);
        processedMessages.addAll(chatClientRequest.prompt().getInstructions());
        // 移除保存用户消息的逻辑，只追加 prompt
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().mutate().messages(processedMessages).build()).build();
    }

    @NotNull
    public ChatClientResponse after(@NotNull ChatClientResponse chatClientResponse, @NotNull AdvisorChain advisorChain) {
        // 移除保存助手消息的逻辑，不保存任何历史记录
        return chatClientResponse;
    }

    @NotNull
    public Flux<ChatClientResponse> adviseStream(@NotNull ChatClientRequest chatClientRequest,
                                                 @NotNull StreamAdvisorChain streamAdvisorChain) {
        Scheduler scheduler = this.getScheduler();
        Mono<ChatClientRequest> var10000 = Mono.just(chatClientRequest).publishOn(scheduler).map((request) -> {
            return this.before(request, streamAdvisorChain);
        });
        Objects.requireNonNull(streamAdvisorChain);
        return var10000.flatMapMany(streamAdvisorChain::nextStream).transform((flux) -> {
            return (new ChatClientMessageAggregator()).aggregateChatClientResponse(flux, (response) -> {
                this.after(response, streamAdvisorChain);
            });
        });
    }

    public static Builder builder(ChatMemory chatMemory) {
        return new Builder(chatMemory);
    }

    public static final class Builder {
        private String conversationId = "default";
        private int order = -2147482648;
        private Scheduler scheduler;
        private final ChatMemory chatMemory;

        private Builder(ChatMemory chatMemory) {
            this.scheduler = BaseAdvisor.DEFAULT_SCHEDULER;
            this.chatMemory = chatMemory;
        }

        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public ChatMemoryAppendAdvisor build() {
            return new ChatMemoryAppendAdvisor(this.chatMemory, this.conversationId, this.order, this.scheduler);
        }
    }
}