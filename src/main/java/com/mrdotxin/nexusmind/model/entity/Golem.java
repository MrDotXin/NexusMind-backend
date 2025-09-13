package com.mrdotxin.nexusmind.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.mrdotxin.nexusmind.config.database.mysql.type.LongListHandler;
import com.mrdotxin.nexusmind.config.database.mysql.type.StringListHandler;
import lombok.Data;
import org.w3c.dom.stylesheets.LinkStyle;

/**
 * 智能体
 */
@Data
@TableName(value ="golem", autoResultMap = true)
public class Golem implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 智能体初始提示词
     */
    @TableField(value = "systemPrompt")
    private String systemPrompt;

    /**
     * 智能体名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 智能体介绍
     */
    @TableField(value = "description")
    private String description;

    /**
     * 知识库
     */
    @TableField(value = "rags", typeHandler = LongListHandler.class)
    private List<Long> rags;

    /**
     * 智能体标签
     */
    @TableField(value = "tags", typeHandler = StringListHandler.class)
    private List<String> tags;

    /**
     * 智能体分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 点赞数
     */
    @TableField(value = "likes")
    private Long likes;

    /**
     * 创建用户
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 公开状态
     */
    @TableField(value = "isPublic")
    private Boolean isPublic;

    /**
     * 开场白
     */
    @TableField(value = "prologue")
    private String prologue;

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