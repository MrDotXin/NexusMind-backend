package com.mrdotxin.nexusmind.service;

import com.mrdotxin.nexusmind.model.dto.chat.DoChatRequest;
import com.mrdotxin.nexusmind.model.entity.ChatLog;
import com.mrdotxin.nexusmind.model.entity.ChatSession;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.mrdotxin.nexusmind.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * @param golem
     * @param doChatRequest
     * @param user
     * @return
     */
    String doChat(Golem golem, DoChatRequest doChatRequest, User user);

    /**
     * @param golem
     * @param doChatRequest
     * @param user
     * @return
     */
    SseEmitter doChatAsync(Golem golem, DoChatRequest doChatRequest, User user);

    ChatClient buildChatClient(Golem golem, DoChatRequest doChatRequest, User user);

    ChatSession newChatSession(User user, Golem golem, String content);

    ChatSession getOrNewChatSession(User user, Golem golem, String content, Long sessionId);
}
