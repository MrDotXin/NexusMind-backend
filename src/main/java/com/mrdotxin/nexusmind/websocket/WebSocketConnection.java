package com.mrdotxin.nexusmind.websocket;

import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * WebSocket服务
 * 处理实时消息推送
 */
@Slf4j
@Component
@Getter
@ServerEndpoint("/ws/{userId}")
public class WebSocketConnection {

    private static WebSocketService service;
    /**
     * 用于存储每个用户的WebSocketService实例
     * 以userId为key，WebSocketService实例为value
     */
    private static final Map<Long, WebSocketConnection> clients = new ConcurrentHashMap<>();

    /**
     * 管理员角色ID的集合
     */
    private static final Integer ADMIN_ROLE_ID = 1;  // 假设管理员角色ID为1

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前会话
     */
    private Session session;

    @Resource
    public void setWebSocketService(WebSocketService webService) {
        service = webService;
    }

    public static Boolean existsUser(Long userId) {
        return clients.containsKey(userId);
    }

    public static void closeConnection(Long userId) {
        WebSocketConnection webSocketService = clients.get(userId);

        try {
            if (webSocketService.session != null && webSocketService.session.isOpen()) {
                webSocketService.session.close();
                log.info("已主动关闭用户{}的WebSocket连接", webSocketService.userId);
            }
        } catch (IOException e) {
            log.error("关闭用户{}的WebSocket连接时出错", webSocketService.userId, e);
        }
    }
    /**
     * 连接建立时调用
     */
    @OnOpen
    public void onOpen(@PathParam("userId") Long userId, Session session) {
        this.userId = userId;
        this.session = session;
        clients.put(userId, this);
        log.info("WebSocket连接建立，用户ID：{}", userId);
    }

    /**
     * 连接关闭时调用
     */
    @OnClose
    public void onClose() {
        clients.remove(this.userId);
        log.info("WebSocket连接关闭，用户ID：{}", userId);
    }

    /**
     * 收到客户端消息时调用
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到用户{}的消息: {}", this.userId, message);
    }

    /**
     * 连接发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，用户ID：{}，错误信息：{}", this.userId, error.getMessage());
    }

    public static void sendWithTimeout(Session session, String message, Long timeoutMillis) throws IOException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> future = executorService.submit(() -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前在线用户数量
     * 
     * @return 在线用户数
     */
    public static int getOnlineCount() {
        return clients.size();
    }

    public static WebSocketConnection getById(Long userId) { return clients.get(userId); }

    public static Map<Long, WebSocketConnection> getMap() { return clients; }
}