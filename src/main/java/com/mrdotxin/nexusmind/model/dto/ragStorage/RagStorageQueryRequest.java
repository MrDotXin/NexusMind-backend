package com.mrdotxin.nexusmind.model.dto.ragStorage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.mrdotxin.nexusmind.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class RagStorageQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 知识库id
     */
    private Long ragId;

    /**
     * 数据集的标题
     */
    private String title;

    /**
     * 数据集的名称
     */
    private String name;

    /**
     * 数据集的media type
     */
    private String type;

    @Serial
    private static final long serialVersionUID = 1L;
}
