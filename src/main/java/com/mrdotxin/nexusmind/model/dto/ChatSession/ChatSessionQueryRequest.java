package com.mrdotxin.nexusmind.model.dto.ChatSession;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.mrdotxin.nexusmind.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSessionQueryRequest extends PageRequest implements Serializable {

     /**
     * id
     */
    private Long id;

    /**
     * 关联的用户Id
     */
    private Long userId;

    /**
     * 对于智能体Id
     */
    private Long golemId;

    /**
     * 这个会话的名称
     */
    private String title;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
