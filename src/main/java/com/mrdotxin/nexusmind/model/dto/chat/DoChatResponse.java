package com.mrdotxin.nexusmind.model.dto.chat;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DoChatResponse implements Serializable {

    private String content;

    private Long sessionId;


    @Serial
    private static final long serialVersionUID = 1L;
}
