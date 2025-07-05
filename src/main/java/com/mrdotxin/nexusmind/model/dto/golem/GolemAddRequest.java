package com.mrdotxin.nexusmind.model.dto.golem;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class GolemAddRequest implements Serializable {

    private String systemPrompt;

    private String name;

    private String description;

    private List<String> tags;

    private String category;

    private String avatar;

    @Serial
    private static final long serialVersionUID = 1L;
}
