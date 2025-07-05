package com.mrdotxin.nexusmind.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

/**
 * 用户点赞
 */
@Data
@TableName(value ="likes")
public class Likes implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标对象
     */
    @TableField(value = "target")
    private String target;

    /**
     * 用户Id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 目标Id
     */
    @TableField(value = "targetId")
    private Long targetId;

    /**
     * 点赞时间
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


    /**
     * 通过参数获取到具体的Entity, 此方法为通用方法
     * @param userId 发出喜欢的用户
     * @param target 目标(可以是评论、智能体、工作流、知识库)
     * @param targetId 目标Id
     *
     */
    public static Likes buildLikes(Long userId, String target, Long targetId) {
        Likes likes = new Likes();

        likes.setUserId(userId);
        likes.setTarget(target);
        likes.setTargetId(targetId);

        return likes;
    }
}