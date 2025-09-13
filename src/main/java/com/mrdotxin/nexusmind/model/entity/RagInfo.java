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

import com.mrdotxin.nexusmind.config.database.mysql.type.StringListHandler;
import lombok.Data;

/**
 * 知识库信息表
 */
@Data
@TableName(value ="RagInfo", autoResultMap = true)
public class RagInfo implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 知识库的默认展示图片
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 知识库描述
     */
    @TableField(value = "title")
    private String title;

    /**
     * 知识库描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 知识库标签
     */
    @TableField(value = "tags", typeHandler = StringListHandler.class)
    private List<String> tags;

    /**
     * 知识库价格
     */
    @TableField(value = "price")
    private Double price;

    /**
     * 创建者ID
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 知识库的分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 知识库的点赞数量
     */
    @TableField(value = "likes")
    private Long likes;

    /**
     * 知识库的点赞数量
     */
    @TableField(value = "sources")
    private Integer sources;

    /**
     * 知识库的订阅数量
     */
    @TableField(value = "subscriptions")
    private Long subscriptions;

    /**
     * 知识库的公开状态
     */
    @TableField(value = "isPublic")
    private Boolean isPublic;

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