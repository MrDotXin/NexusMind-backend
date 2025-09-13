package com.mrdotxin.nexusmind.ai.agent;

import com.mrdotxin.nexusmind.ai.enums.AgentStatusEnum;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Data
public abstract class BaseAgent {

    private AgentStatusEnum agentStatusEnum;

    public void terminate() {
        agentStatusEnum = AgentStatusEnum.FINISHED;
    }

    public SseEmitter doChatStream(String userPrompt) {
        return new SseEmitter();
    };

    public String doChatCall(String userPrompt) {
        return "";
    }
}
