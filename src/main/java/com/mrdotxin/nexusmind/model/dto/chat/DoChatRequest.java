package com.mrdotxin.nexusmind.model.dto.chat;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class DoChatRequest implements Serializable {
    /**
     * 对应的会话Id
     */
    private Long sessionId;

    /**
     * 问答内容
     */
    private String content;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 额外的知识库
     */
    private List<Long> extraRags;

    @Serial
    private static final long serialVersionUID = 1L;
}
