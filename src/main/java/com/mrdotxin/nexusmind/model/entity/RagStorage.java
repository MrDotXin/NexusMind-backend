package com.mrdotxin.nexusmind.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 知识库信息表
 * @TableName ragstorage
 */
@TableName(value ="ragstorage")
@Data
public class RagStorage implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 知识库id
     */
    @TableField(value = "ragId")
    private Long ragId;

    /**
     * 数据集的标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 数据集的名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 数据集的media type
     */
    @TableField(value = "type")
    private String type;

   /**
     * 文件大小
     */
    @TableField(value = "size")
    private Integer size;

   /**
     * 总切片数量
     */
    @TableField(value = "slice")
    private Integer slice;

   /**
     * 平均切片大小
     */
    @TableField(value = "averageSlice")
    private Integer averageSlice;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableField(value = "isDelete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}