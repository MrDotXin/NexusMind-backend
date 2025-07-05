package com.mrdotxin.nexusmind.model.dto.chatLog;

import com.mrdotxin.nexusmind.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatLogQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 会话Id
     */
    private Long sessionId;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 内容
     */
    private String text;


    /**
     * 会话Id
     */
    private Long sequenceId;

    @Serial
    private static final long serialVersionUID = 1L;
}
