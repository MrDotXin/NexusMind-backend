package com.mrdotxin.nexusmind.utils;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.model.entity.ChatLog;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.model.Media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageUtil {
    private static final String MEDIA_LIST_KEY = "medias";
    private static final String TOOL_CALL_LIST_KEY = "toolCalls";
    private static final String RESPONSE_LIST_KEY = "responses";
    private static final String META_KEY = "meta";

    public static Message fromChatLog(ChatLog chatLog) {
        String type = chatLog.getMessageType();

        if (type.equals(MessageType.USER.getValue())) {

            return toUserMessage(chatLog);
        } else if (type.equals(MessageType.SYSTEM.getValue())) {

            return toSystemMessage(chatLog);
        } else if (type.equals(MessageType.ASSISTANT.getValue())) {

            return toAssistantMessage(chatLog);
        } else if (type.equals(MessageType.TOOL.getValue())) {

            return toToolResponseMessage(chatLog);
        }

        throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的消息类型");
    }

    public static Map<String, Object> extractMetaFromMessage(Message message) {
        if (message instanceof UserMessage) {

            return extractMetaDataFromUserMessage((UserMessage) message);
        } else if (message instanceof SystemMessage) {

            return extractMetaDataFromSystemMessage((SystemMessage) message);
        } else if (message instanceof AssistantMessage) {

            return extractMetaDataFromAssistantMessage((AssistantMessage) message);
        } else if (message instanceof ToolResponseMessage) {

            return extractMetaDataFromToolResponseMessage((ToolResponseMessage) message);
        }

        throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的消息类型");
    }

    private static Map<String, Object> extractMetaDataFromUserMessage(UserMessage userMessage) {
        Map<String, Object> data = new HashMap<>();
        if (!userMessage.getMetadata().isEmpty()) {
            data.put(META_KEY, userMessage.getMetadata());
        }

        if (!userMessage.getMedia().isEmpty()) {
            data.put(MEDIA_LIST_KEY, userMessage.getMedia());
        }

        return data;
    }

    private static Map<String, Object> extractMetaDataFromSystemMessage(SystemMessage systemMessage) {
        return Map.of();
    }

    private static Map<String, Object> extractMetaDataFromAssistantMessage(AssistantMessage assistantMessage) {
        Map<String, Object> data = new HashMap<>();
        if (!assistantMessage.getMetadata().isEmpty()) {
            data.put(META_KEY, assistantMessage.getMetadata());
        }

        if (!assistantMessage.getToolCalls().isEmpty()) {
            data.put(TOOL_CALL_LIST_KEY, assistantMessage.getToolCalls());
        }

        if (!assistantMessage.getMedia().isEmpty()) {
            data.put(MEDIA_LIST_KEY, assistantMessage.getMedia());
        }

        return data;
    }

    private static Map<String, Object> extractMetaDataFromToolResponseMessage(ToolResponseMessage toolResponseMessage) {
        Map<String, Object> data = new HashMap<>();
        if (!toolResponseMessage.getMetadata().isEmpty()) {
            data.put(META_KEY, toolResponseMessage.getMetadata());
        }

        if (!toolResponseMessage.getResponses().isEmpty()) {
            data.put(RESPONSE_LIST_KEY, toolResponseMessage.getResponses());
        }

        return data;
    }

    private static UserMessage toUserMessage(ChatLog chatLog) {
        Map<String, Object> meta = chatLog.getMeta();
        Map<String, Object> metaData = Map.of();
        List<Media> mediaList = new ArrayList<>();
        if (meta.containsKey(META_KEY)) {
            metaData = TypeForceConverter.convert(meta.get(META_KEY));
        }

        if (meta.containsKey(MEDIA_LIST_KEY)) {
            mediaList = TypeForceConverter.convert(meta.get(MEDIA_LIST_KEY), new TypeReference<List<Media>>() {});
        }

        return new UserMessage(chatLog.getText(), mediaList, metaData);
    }

    private static SystemMessage toSystemMessage(ChatLog chatLog) {
        return new SystemMessage(chatLog.getText());
    }

    private static AssistantMessage toAssistantMessage(ChatLog chatLog) {
        Map<String, Object> meta = chatLog.getMeta();
        List<Media> mediaList = new ArrayList<>();
        List<AssistantMessage.ToolCall> toolCallList = new ArrayList<>();
        Map<String, Object> properties = Map.of();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        if (meta.containsKey(MEDIA_LIST_KEY)) {
            mediaList = TypeForceConverter.<List<Media>>convert(meta.get(MEDIA_LIST_KEY), new TypeReference<List<Media>>() {});
        }
        if (meta.containsKey(TOOL_CALL_LIST_KEY)) {
            toolCallList = TypeForceConverter.convert(meta.get(TOOL_CALL_LIST_KEY), new TypeReference<List<AssistantMessage.ToolCall>>(){});;
        }
        if (meta.containsKey(META_KEY)) {
            properties = TypeForceConverter.convert(meta.get(META_KEY));;
        }

        return new AssistantMessage(chatLog.getText(), properties, toolCallList, mediaList);
    }

    private static ToolResponseMessage toToolResponseMessage(ChatLog chatLog) {
        Map<String, Object> meta = chatLog.getMeta();
        Map<String, Object> metaData = Map.of();
        List<ToolResponseMessage.ToolResponse> toolResponseList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        if (meta.containsKey(RESPONSE_LIST_KEY)) {
            toolResponseList = TypeForceConverter.convert(meta.get(RESPONSE_LIST_KEY), new TypeReference<List<ToolResponseMessage.ToolResponse>>() {});
        }

        if (meta.containsKey(META_KEY)) {
            metaData = TypeForceConverter.convert(meta.get(META_KEY));
        }

        return new ToolResponseMessage(toolResponseList, metaData);
    }

    private boolean checkIsListOfType(Object object, Class<?> type) {
        if (object instanceof List<?> objectList) {
            return objectList.stream().allMatch(o -> o.equals(type));
        }
        return false;
    }

    private boolean checkIsMapOfType(Object object, Class<?> type) {
        if (object instanceof Map<?, ?> objectList) {
            return objectList.values().stream().allMatch(o -> o.equals(type));
        }

        return false;
    }

    static class TypeForceConverter {
        private static ObjectMapper objectMapper = null;

        public static ObjectMapper  getConverter() {
            if (ObjectUtil.isNull(objectMapper)) {
                objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            }

            return objectMapper;
        }

        public static <T> T  convert(Object object, TypeReference<T> type) {
            if (ObjectUtil.isNull(objectMapper)) {
                objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            }

            return objectMapper.convertValue(object, type);
        }

                public static <T> T  convert(Object object) {
            if (ObjectUtil.isNull(objectMapper)) {
                objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            }

            return objectMapper.convertValue(object, new TypeReference<T>() {});
        }
    }
}
