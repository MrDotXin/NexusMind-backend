package com.mrdotxin.nexusmind.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * WebSocket消息数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage implements Serializable {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 目标ID
     * 可能是用户ID、楼栋ID等，根据消息类型决定
     */
    private Long targetId;

    /**
     * 关联业务数据ID
     */
    private Long businessId;


    private Boolean urgent;
} 