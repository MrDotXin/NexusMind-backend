package com.mrdotxin.nexusmind.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class GolemVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 智能体初始提示词
     */
    private String systemPrompt;

    /**
     * 智能体名称
     */
    private String name;

    /**
     * 智能体介绍
     */
    private String description;

    /**
     * 知识库
     */
    private List<Long> rags;

    /**
     * 智能体标签
     */
    private List<String> tags;

    /**
     * 智能体分类
     */
    private String category;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 用户对象
     */
    private UserVO userVO;

    /**
     * 公开状态
     */
    private boolean isPublic;

        /**
     * 开场白
     */
    private String prologue;



    @Serial
    private static final long serialVersionUID = 1L;
}
