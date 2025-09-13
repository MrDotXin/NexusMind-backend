package com.mrdotxin.nexusmind.ai.persistence;

import com.mrdotxin.nexusmind.service.ChatLogService;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMessageStore implements ChatMemoryRepository {

    @Resource
    private ChatLogService chatLogService;

    public void add(String sessionId, List<Message> messages) {
        chatLogService.addMessageList(sessionId, messages);
    }
    public void add(String sessionId, Message message) {chatLogService.addMessageList(sessionId, List.of(message));}
    public void add(Long sessionId, Message message) {chatLogService.addMessageList(String.valueOf(sessionId), List.of(message));}
    public List<Message> get(String sessionId) {
            return chatLogService.getMessageList(sessionId);
    }
    public void clear(String sessionId) {
        chatLogService.deleteChatLog(sessionId);
    }

    @NotNull
    @Override
    public List<String> findConversationIds() {
        return chatLogService.listAllSessionIds();
    }

    @NotNull
    @Override
    public List<Message> findByConversationId(@NotNull String conversationId) {
        return get(conversationId);
    }

    @Override
    public void saveAll(@NotNull String conversationId, @NotNull List<Message> messages) {
        deleteByConversationId(conversationId);
        add(conversationId, messages);
    }

    @Override
    public void deleteByConversationId(@NotNull String conversationId) {
        clear(conversationId);
    }
}
