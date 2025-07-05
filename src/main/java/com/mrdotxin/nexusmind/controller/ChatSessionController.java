package com.mrdotxin.nexusmind.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrdotxin.nexusmind.annotation.AuthCheck;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.PageRequest;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.constant.UserConstant;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.ChatSession.ChatSessionQueryRequest;
import com.mrdotxin.nexusmind.model.entity.ChatSession;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.service.ChatSessionService;
import com.mrdotxin.nexusmind.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatSession")
public class ChatSessionController {

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private UserService userService;

    /**
     * 分页获取用户列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatSession>> listChatHistoryByPage(@RequestBody ChatSessionQueryRequest ChatSessionQueryRequest, HttpServletRequest request) {
        long current = ChatSessionQueryRequest.getCurrent();
        long size = ChatSessionQueryRequest.getPageSize();
        Page<ChatSession> userPage = chatSessionService.page(new Page<>(current, size),
                chatSessionService.getQueryWrapper(ChatSessionQueryRequest));
        return ResultUtils.success(userPage);
    }

    @PostMapping("/list/page/my")
    public BaseResponse<Page<ChatSession>> listChatHistoryByPage(@RequestBody PageRequest pageRequest, HttpServletRequest httpServletRequest) {
        User user = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);
        ChatSessionQueryRequest chatSessionQueryRequest = new ChatSessionQueryRequest();
        chatSessionQueryRequest.setUserId(user.getId());
        chatSessionQueryRequest.setCurrent(pageRequest.getCurrent());
        chatSessionQueryRequest.setPageSize(pageRequest.getPageSize());
        chatSessionQueryRequest.setSortField(pageRequest.getSortField());
        chatSessionQueryRequest.setSortOrder(pageRequest.getSortOrder());

        long current = chatSessionQueryRequest.getCurrent();
        long size = chatSessionQueryRequest.getPageSize();
        Page<ChatSession> userPage = chatSessionService.page(new Page<>(current, size),
                chatSessionService.getQueryWrapper(chatSessionQueryRequest));
        return ResultUtils.success(userPage);
    }
}
