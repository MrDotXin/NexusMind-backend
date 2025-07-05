package com.mrdotxin.nexusmind.component;


import com.baomidou.mybatisplus.extension.service.IService;
import com.mrdotxin.nexusmind.service.*;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MybatisServiceSelector {

    @Resource private UserService userService;
    @Resource private ChatLogService chatLogService;
    @Resource private ChatSessionService chatSessionService;
    @Resource private RagInfoService ragInfoService;
    @Resource private RagStorageService ragStorageService;
    @Resource private UserRagSubscriptionService userRagSubscriptionService;
    @Resource private GolemService golemService;
    @Resource private LikesService likesService;

    public IService<?> from(String tableName) {
        return switch(tableName) {
            case "user" -> userService;
            case "chatLog" -> chatLogService;
            case "chatSession" -> chatSessionService;
            case "ragInfo" -> ragInfoService;
            case "ragStorage" -> ragStorageService;
            case "userRagSubscription" -> userRagSubscriptionService;
            case "golem" -> golemService;
            case "likes" -> likesService;
            default -> null;
        };
    }
}
