package com.mrdotxin.nexusmind.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum FileUploadTypeEnum {
    URL("url", "url"),
    MULTIPART_FILE("multipart", "multipart");

    private final String text;

    private final String value;

    FileUploadTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static FileUploadTypeEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (FileUploadTypeEnum fileUploadTypeEnum : FileUploadTypeEnum.values()) {
            if (fileUploadTypeEnum.value.equals(value)) {
                return fileUploadTypeEnum;
            }
        }
        return null;
    }
}
