package com.mrdotxin.nexusmind.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrdotxin.nexusmind.model.dto.ChatSession.ChatSessionQueryRequest;
import com.mrdotxin.nexusmind.model.entity.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【chatsession(智能体会话)】的数据库操作Service
* @createDate 2025-06-19 21:25:32
*/
public interface ChatSessionService extends IService<ChatSession> {

    QueryWrapper<ChatSession> getQueryWrapper(ChatSessionQueryRequest chatSessionQueryRequest);
}
