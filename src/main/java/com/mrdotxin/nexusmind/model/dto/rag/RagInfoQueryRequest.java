package com.mrdotxin.nexusmind.model.dto.rag;

import com.mrdotxin.nexusmind.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RagInfoQueryRequest extends PageRequest implements Serializable {
    /**
     * 知识库的默认展示图片
     */
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 知识库的默认展示图片
     */
    private String avatar;

    /**
     */
    private String searchText;

    /**
     * 知识库描述
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


    @Serial
    private static final long serialVersionUID = 1L;
}
