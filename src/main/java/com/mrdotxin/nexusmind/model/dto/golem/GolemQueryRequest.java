package com.mrdotxin.nexusmind.model.dto.golem;

import com.mrdotxin.nexusmind.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class GolemQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String name;

    private String category;

    private Long userId;

    private List<String> tags;

    @Serial
    private static final long serialVersionUID = 1L;
}