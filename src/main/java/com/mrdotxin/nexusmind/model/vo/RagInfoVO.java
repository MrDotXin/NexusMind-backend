package com.mrdotxin.nexusmind.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 知识库信息表
 */
@Data
@TableName(value ="RagInfo")
public class RagInfoVO implements Serializable {
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
    private String description;

    /**
     * 知识库标签
     */
    private List<String> tags;

    /**
     * 知识库价格
     */
    private Double price;

    /**
     * 创建者ID
     */
    private Long userId;

    /**
     * 知识库的分类
     */
    private String category;

    /**
     * 知识库的点赞数量
     */
    private Long likes;

    /**
     * 知识库的点赞数量
     */
    private Integer sources;

    /**
     * 知识库的点赞数量
     */
    private Boolean isPublic;

    /**
     * 知识库的订阅数量
     */
    private Long subscriptions;

    /**
     * 知识库作者信息
     */
    private UserVO user;

}