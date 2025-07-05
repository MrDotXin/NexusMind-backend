package com.mrdotxin.nexusmind.ai.persistence;

import com.mrdotxin.nexusmind.service.ChatLogService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMessageStore implements ChatMemory {

    @Resource
    private ChatLogService chatLogService;

    @Override
    public void add(String sessionId, List<Message> messages) {
        chatLogService.addMessageList(sessionId, messages);
    }

    public void add(String sessionId, Message message) {chatLogService.addMessageList(sessionId, List.of(message));}
    public void add(Long sessionId, Message message) {chatLogService.addMessageList(String.valueOf(sessionId), List.of(message));}

    @Override
    public List<Message> get(String sessionId, int lastN) {
            return chatLogService.getMessageList(sessionId, lastN);
    }

    @Override
    public void clear(String sessionId) {
        chatLogService.deleteChatLog(sessionId);
    }
}
