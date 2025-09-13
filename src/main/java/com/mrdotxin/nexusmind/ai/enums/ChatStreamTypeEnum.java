package com.mrdotxin.nexusmind.ai.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ChatStreamTypeEnum {
    TEXT("文本", "text"),
    COMPLETE("完成传输", "complete"),
    TOOL_CALL("工具调用", "tool_call"),
    TOOL_RESULT("工具调用结果", "tool_result"),
    ERROR("错误", "error");

    private final String text;

    private final String value;

    ChatStreamTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     */
    public static ChatStreamTypeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ChatStreamTypeEnum anEnum : ChatStreamTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
