package com.mrdotxin.nexusmind.component.enums;

import com.mrdotxin.nexusmind.model.enums.UserRoleEnum;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public enum ChatModelEnum {
    QWEN("qwen", "qwen-max-latest"),
    OLLAMA("ollama", "llama3.2:latest"),
    OPENAI("openai", "gpt-4o-mini");

    private final String text;

    private final String value;

    ChatModelEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ChatModelEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ChatModelEnum anEnum : ChatModelEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
