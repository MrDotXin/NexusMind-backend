package com.mrdotxin.nexusmind.ai.model;

import com.mrdotxin.nexusmind.ai.enums.ChatStreamTypeEnum;
import lombok.Data;

@Data
public class ChatStreamResponse {

    /**
     * 发送的数据类型
     */
    private String type;

    /**
     * 数据的内容
     */
    private String content;


    public static ChatStreamResponse text(String content) {
        ChatStreamResponse chatStreamResponse = new ChatStreamResponse();
        chatStreamResponse.setContent(content);
        chatStreamResponse.setType(ChatStreamTypeEnum.TEXT.getValue());

        return chatStreamResponse;
    }

    public static ChatStreamResponse error(String content) {
        ChatStreamResponse chatStreamResponse = new ChatStreamResponse();
        chatStreamResponse.setContent(content);
        chatStreamResponse.setType(ChatStreamTypeEnum.ERROR.getValue());

        return chatStreamResponse;
    }

    public static ChatStreamResponse toolCall(String content) {
        ChatStreamResponse chatStreamResponse = new ChatStreamResponse();
        chatStreamResponse.setContent(content);
        chatStreamResponse.setType(ChatStreamTypeEnum.TOOL_CALL.getValue());

        return chatStreamResponse;
    }

    public static ChatStreamResponse toolResult(String content) {
        ChatStreamResponse chatStreamResponse = new ChatStreamResponse();
        chatStreamResponse.setContent(content);
        chatStreamResponse.setType(ChatStreamTypeEnum.TOOL_RESULT.getValue());

        return chatStreamResponse;
    }

    public static ChatStreamResponse complete() {
        ChatStreamResponse chatStreamResponse = new ChatStreamResponse();
        chatStreamResponse.setType(ChatStreamTypeEnum.COMPLETE.getValue());

        return chatStreamResponse;
    }
}
