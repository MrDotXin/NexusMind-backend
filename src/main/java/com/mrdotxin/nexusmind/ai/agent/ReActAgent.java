package com.mrdotxin.nexusmind.ai.agent;

import com.mrdotxin.nexusmind.ai.persistence.ChatMessageStore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent{

    abstract boolean think(Long sessionId);

    abstract boolean thinkStream(Long sessionId, SseEmitter sseEmitter);

    abstract String act(Long sessionId);

    abstract String actStream(Long sessionId, SseEmitter sseEmitter);

    public ReActAgent(ChatMessageStore chatMessageStore) {
        super(chatMessageStore);
    }

    @Override
    protected String step(Long sessionId) {

        boolean should_act = this.think(sessionId);
        if (!should_act) {
            return "Thinking complete - no action needed";
        }

        return this.act(sessionId);
    }

    @Override
    protected String stepStream(Long sessionId, SseEmitter sseEmitter) {
        boolean should_act = this.thinkStream(sessionId, sseEmitter);
        if (!should_act) {
            return "Thinking complete - no action needed";
        }

        return this.actStream(sessionId, sseEmitter);
    }



}
