package com.mrdotxin.nexusmind.model.dto.likes;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class DoLikeRequest implements Serializable {

    /**
     * 目标对象
     */
    private String target;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 目标Id
     */
    private Long targetId;
}
