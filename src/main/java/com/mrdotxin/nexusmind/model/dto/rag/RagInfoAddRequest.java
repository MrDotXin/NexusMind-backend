package com.mrdotxin.nexusmind.model.dto.rag;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class RagInfoAddRequest implements Serializable {

    /**
     * 知识库标题
     */
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
     * 知识库的分类
     */
    private String category;

    /**
     * 知识库的价格
     */
    private Double price;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    @Serial
    private static final long serialVersionUID = 1L;
}
