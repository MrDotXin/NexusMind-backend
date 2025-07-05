package com.mrdotxin.nexusmind.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrdotxin.nexusmind.annotation.AuthCheck;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.PageRequest;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.constant.CommonConstant;
import com.mrdotxin.nexusmind.constant.UserConstant;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.chatLog.ChatLogQueryRequest;
import com.mrdotxin.nexusmind.model.entity.ChatLog;
import com.mrdotxin.nexusmind.model.entity.ChatSession;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.service.ChatLogService;
import com.mrdotxin.nexusmind.service.ChatSessionService;
import com.mrdotxin.nexusmind.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatLog")
public class ChatLogController {

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private UserService userService;

    @Resource
    private ChatLogService chatLogService;
    /**
     * 分页获取用户列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatLog>> listChatHistoryByPage(@RequestBody ChatLogQueryRequest chatLogQueryRequest, HttpServletRequest request) {
        long current = chatLogQueryRequest.getCurrent();
        long size = chatLogQueryRequest.getPageSize();
        Page<ChatLog> userPage = chatLogService.page(new Page<>(current, size),
                chatLogService.getQueryWrapper(chatLogQueryRequest));
        return ResultUtils.success(userPage);
    }

    @PostMapping("/list/page/{sessionId}")
    public BaseResponse<Page<ChatLog>> listMyChatHistoryByPage(@RequestBody PageRequest pageRequest, @PathVariable("sessionId") Long sessionId, HttpServletRequest httpServletRequest) {
        User user = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);
        ChatLogQueryRequest chatLogQueryRequest = new ChatLogQueryRequest();
        chatLogQueryRequest.setSessionId(sessionId);

        ChatSession chatSession = chatSessionService.getById(sessionId);
        ThrowUtils.throwIf(ObjectUtil.isNull(chatSession), ErrorCode.PARAMS_ERROR, "会话不存在");
        ThrowUtils.throwIf(!user.getId().equals(chatSession.getUserId()), ErrorCode.NO_AUTH_ERROR);

        chatLogQueryRequest.setCurrent(pageRequest.getCurrent());
        chatLogQueryRequest.setPageSize(pageRequest.getPageSize());
        chatLogQueryRequest.setSortField("sequenceId");
        chatLogQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);

        long current = chatLogQueryRequest.getCurrent();
        long size = chatLogQueryRequest.getPageSize();
        Page<ChatLog> userPage = chatLogService.page(new Page<>(current, size),
                chatLogService.getQueryWrapper(chatLogQueryRequest));
        return ResultUtils.success(userPage);
    }

}
