package com.mrdotxin.nexusmind.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class WebSocketService {

    @Resource
    private UserService userService;

    /**
     * 发送消息给指定用户
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void sendMessageToUser(Long userId, WebSocketMessage message, boolean persistent) {
        WebSocketConnection connection = WebSocketConnection.getById(userId);

        if (ObjectUtil.isNotNull(connection)) {
            try {
                connection.getSession().getBasicRemote().sendText(JSONUtil.toJsonStr(message));
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法发送");
            }
        }
    }

    /**
     * 发送消息给所有管理员
     *
     * @param message 消息内容
     */
    public void sendMessageToAll(WebSocketMessage message, boolean persistent) {
        if (!persistent) {
            Set<Long> userIds = WebSocketConnection.getMap().keySet();
            userIds.forEach(id -> {
                this.sendMessageToUser(id, message, false);
            });
        } else {
            List<Long> userIds = userService.listUserIdAll();
            userIds.forEach(id -> {
                this.sendMessageToUser(id, message, true);
            });
        }
    }

        /**
     * 发送消息给所有管理员
     *
     * @param message 消息内容
     */
    public void sendMessageToAllAdmins(WebSocketMessage message, boolean persistent) {
        List<Long> userIds = userService.listAdminId();
        userIds.forEach(id -> {
            this.sendMessageToUser(id, message, persistent);
        });
    }

}
