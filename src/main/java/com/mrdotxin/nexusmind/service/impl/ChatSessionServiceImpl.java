package com.mrdotxin.nexusmind.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.nexusmind.model.dto.ChatSession.ChatSessionQueryRequest;
import com.mrdotxin.nexusmind.model.entity.ChatSession;
import com.mrdotxin.nexusmind.service.ChatSessionService;
import com.mrdotxin.nexusmind.mapper.mysql.ChatSessionMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Administrator
* @description 针对表【chatsession(智能体会话)】的数据库操作Service实现
* @createDate 2025-06-19 21:25:32
*/
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService{

    @Override
    public QueryWrapper<ChatSession> getQueryWrapper(ChatSessionQueryRequest chatSessionQueryRequest) {
        // 提取查询参数
        Long id = chatSessionQueryRequest.getId();
        Long userId = chatSessionQueryRequest.getUserId();
        Long golemId = chatSessionQueryRequest.getGolemId();
        String title = chatSessionQueryRequest.getTitle();
        String sortField = chatSessionQueryRequest.getSortField();
        String sortOrder = chatSessionQueryRequest.getSortOrder();

        // 构建查询条件
        QueryWrapper<ChatSession> queryWrapper = new QueryWrapper<>();

        // 基础字段查询
        queryWrapper.eq(Objects.nonNull(id) && id > 0, "id", id);
        queryWrapper.eq(Objects.nonNull(userId) && userId > 0, "userId", userId);
        queryWrapper.eq(Objects.nonNull(golemId) && golemId > 0, "golemId", golemId);

        // 排序处理
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        }

        return queryWrapper;
    }
}




