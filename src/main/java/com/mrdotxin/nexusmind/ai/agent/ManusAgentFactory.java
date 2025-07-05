package com.mrdotxin.nexusmind.ai.agent;

import com.mrdotxin.nexusmind.ai.persistence.ChatMessageStore;
import com.mrdotxin.nexusmind.ai.tool.ToolCenter;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManusAgentFactory {

    @Resource
    private ToolCenter toolCenter;

    @Resource
    private ToolCallbackProvider mcpToolCallbackProvider;

    @Resource
    @Qualifier("dashscopeChatModel")
    private ChatModel dashscopeChatModel;

    @Resource
    private ChatMessageStore chatMessageStore;

    public ManusAgent newManusAgent() {

        return new ManusAgent(
                toolCenter,
                mcpToolCallbackProvider,
                dashscopeChatModel,
                chatMessageStore
        );
    }

    public static class Builder {
        private final ManusAgent manusAgent;

        public Builder(ManusAgent manusAgent) {
            this.manusAgent = manusAgent;
        }

        public ManusAgent build() {
            return this.manusAgent;
        }

        public Builder toolCenter(ToolCenter toolCenter) {
            this.manusAgent.setToolCenter(toolCenter);
            return this;
        }

        public Builder MCPToolsProvider(ToolCallbackProvider mcpToolCallbackProvider) {
            this.manusAgent.setMcpToolCallbackProvider(mcpToolCallbackProvider);
            return this;
        }

        public Builder chatModel(ChatModel model) {
            this.manusAgent.setChatModel(model);
            return this;
        }

        public Builder chatOptions(ChatOptions options) {
            this.manusAgent.setChatOptions(options);
            return this;
        }

        public Builder advisor(Advisor...advisor) {
            List<Advisor> manusAgentAdvisors = this.manusAgent.getAdvisors();
            manusAgentAdvisors.addAll(List.of(advisor));
            return this;
        }
    }

    public Builder builder() {
        return new Builder(
                // 传入初始条件
                new ManusAgent(
                        toolCenter,
                        mcpToolCallbackProvider,
                        dashscopeChatModel,
                        chatMessageStore
                )
        );
    }
}
