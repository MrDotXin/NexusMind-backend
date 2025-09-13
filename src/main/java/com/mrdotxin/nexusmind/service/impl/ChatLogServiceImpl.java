package com.mrdotxin.nexusmind.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.nexusmind.model.dto.chatLog.ChatLogQueryRequest;
import com.mrdotxin.nexusmind.model.entity.ChatLog;
import com.mrdotxin.nexusmind.service.ChatLogService;
import com.mrdotxin.nexusmind.mapper.mysql.ChatLogMapper;
import com.mrdotxin.nexusmind.utils.ChatMessageUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
*/
@Service
public class ChatLogServiceImpl extends ServiceImpl<ChatLogMapper, ChatLog>
    implements ChatLogService {

    @Resource
    @Qualifier("mysqlTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Override
    public QueryWrapper<ChatLog> getQueryWrapper(ChatLogQueryRequest chatLogQueryRequest) {
        // 提取查询参数
        Long id = chatLogQueryRequest.getId();
        Long sessionId = chatLogQueryRequest.getSessionId();
        String messageType = chatLogQueryRequest.getMessageType();
        String text = chatLogQueryRequest.getText();
        Long sequenceId = chatLogQueryRequest.getSequenceId();
        String sortField = chatLogQueryRequest.getSortField();
        String sortOrder = chatLogQueryRequest.getSortOrder();

        // 构建查询条件
        QueryWrapper<ChatLog> queryWrapper = new QueryWrapper<>();

        // 基础字段查询
        queryWrapper.eq(Objects.nonNull(id) && id > 0, "id", id);
        queryWrapper.eq(Objects.nonNull(sessionId) && sessionId > 0, "sessionId", sessionId);
        queryWrapper.eq(StrUtil.isNotBlank(messageType), "messageType", messageType);
        queryWrapper.like(StrUtil.isNotBlank(text), "text", text);
        queryWrapper.eq(Objects.nonNull(sequenceId) && sequenceId >= 0, "sequenceId", sequenceId);

        // 排序处理
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        }

        return queryWrapper;
    }

    @Override
    public List<String> listAllSessionIds() {
        return this.baseMapper.getIds();
    }

    @Override
    public void addMessageList(String sessionId, List<Message> messageList) {
        Long chatId = Long.valueOf(sessionId);

        Long maxSequence = Optional.ofNullable(this.baseMapper.getMaxSequenceId()).orElse(0L);
        AtomicInteger index = new AtomicInteger();
        List<ChatLog> chatLogs = messageList.stream().map(
            message -> {
                index.getAndIncrement();

                ChatLog chatLog = new ChatLog();
                chatLog.setSessionId(chatId);
                chatLog.setMessageType(String.valueOf(message.getMessageType()));
                chatLog.setText(message.getText());
                chatLog.setSequenceId(maxSequence + index.get());

                Map<String, Object> meta = ChatMessageUtil.extractMetaFromMessage(message);
                if (!meta.isEmpty()) {
                    chatLog.setMeta(meta);
                }

                return chatLog;
            }
        ).toList();

        transactionTemplate.executeWithoutResult(
                status -> this.saveBatch(chatLogs, chatLogs.size())
        );
    }

    @Override
    public List<Message> getMessageList(String sessionId) {
        Long chatId = Long.valueOf(sessionId);

        QueryWrapper<ChatLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sessionId", chatId);
        queryWrapper.orderByAsc("sequenceId");

        List<ChatLog> chatLogs = this.list(queryWrapper);
        return chatLogs.stream().map(ChatMessageUtil::fromChatLog).toList();
    }

    @Override
    public void deleteChatLog(String sessionId) {
        Long chatId = Long.valueOf(sessionId);

        QueryWrapper<ChatLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sessionId", chatId);

        this.baseMapper.delete(queryWrapper);
    }
}




