package com.mrdotxin.nexusmind.component;

import cn.hutool.core.util.StrUtil;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.component.enums.ChatModelEnum;
import com.mrdotxin.nexusmind.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChatModelSelector {
    @Resource
    @Qualifier("openAiChatModel")
    private ChatModel openaiChatModel;

    @Resource
    @Qualifier("dashscopeChatModel")
    private ChatModel dashscopeChatModel;

    @Resource
    @Qualifier("ollamaChatModel")
    private ChatModel ollamaChatModel;

    public ChatModel select(String chatModel) {
        if (StrUtil.isBlank(chatModel)) {
            return dashscopeChatModel;
        }

        if (chatModel.equals(ChatModelEnum.QWEN.getValue())) {
            return dashscopeChatModel;
        } else if (chatModel.equals(ChatModelEnum.OLLAMA.getValue())) {
            return ollamaChatModel;
        } else if (chatModel.equals(ChatModelEnum.OPENAI.getValue())) {
            return openaiChatModel;
        }

        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不存在的模型名称!");
    }
}
