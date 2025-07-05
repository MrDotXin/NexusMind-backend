package com.mrdotxin.nexusmind.model.dto.upload;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileUploadRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /*
     * 可以手动设置照片名称
     */
    private String fileName;

    @Serial
    private static final long serialVersionUID = 1L;
}
