package com.mrdotxin.nexusmind.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

/**
 * 会话记录
 */
@Data
@TableName(value ="chatLog")
@AllArgsConstructor
@NoArgsConstructor
public class ChatLog implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话Id
     */
    @TableField(value = "sessionId")
    private Long sessionId;

    /**
     * 消息类型
     */
    @TableField(value = "messageType")
    private String messageType;

    /**
     * 内容
     */
    @TableField(value = "text")
    private String text;


    /**
     * 会话Id
     */
    @TableField(value = "sequenceId")
    private Long sequenceId;

    /**
     * 元数据
     */
    @TableField(value = "meta")
    private Map<String, Object> meta;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableField(value = "isDelete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}