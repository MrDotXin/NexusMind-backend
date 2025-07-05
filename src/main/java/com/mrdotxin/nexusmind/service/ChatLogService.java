package com.mrdotxin.nexusmind.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrdotxin.nexusmind.model.dto.chat.DoChatRequest;
import com.mrdotxin.nexusmind.model.dto.chatLog.ChatLogQueryRequest;
import com.mrdotxin.nexusmind.model.entity.ChatLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mrdotxin.nexusmind.model.entity.User;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
* @author Administrator
* @description 针对表【chatlog(会话记录)】的数据库操作Service
* @createDate 2025-06-19 21:25:18
*/
public interface ChatLogService extends IService<ChatLog> {

    /**
     *
     * @param sessionId
     * @param messageList
     */
    void addMessageList(String sessionId, List<Message> messageList);

    /**
     *
     * @param sessionId
     * @param lastN
     * @return
     */
    List<Message> getMessageList(String sessionId, int lastN);

    /**
     *
     * @param sessionId
     */
    void deleteChatLog(String sessionId);

    QueryWrapper<ChatLog> getQueryWrapper(ChatLogQueryRequest chatLogQueryRequest);

}
