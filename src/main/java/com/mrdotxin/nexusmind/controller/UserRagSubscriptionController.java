package com.mrdotxin.nexusmind.controller;


import cn.hutool.core.util.ObjectUtil;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.entity.RagInfo;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.model.entity.UserRagSubscription;
import com.mrdotxin.nexusmind.service.RagInfoService;
import com.mrdotxin.nexusmind.service.UserRagSubscriptionService;
import com.mrdotxin.nexusmind.service.UserService;
import com.mrdotxin.nexusmind.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userRagSubscription")
public class UserRagSubscriptionController {

    @Resource
    private UserRagSubscriptionService userRagSubscriptionService;

    @Resource
    private UserService userService;

    @Resource
    private RagInfoService ragInfoService;

    @PostMapping("/subscribe")
    public BaseResponse<Boolean> subscribeRag(@RequestParam("ragId") Long ragId, HttpServletRequest httpServletRequest) {
        Long userId = getAndCheckLoginUserId(httpServletRequest);

        boolean checked = SqlUtils.checkFieldExist(ragInfoService, "id", ragId);
        ThrowUtils.throwIf(!checked, ErrorCode.PARAMS_ERROR, "知识库不存在");

        UserRagSubscription userRagSubscription = new UserRagSubscription();
        userRagSubscription.setUserId(userId);
        userRagSubscription.setRagId(ragId);
        userRagSubscription.setPaid(0.0);


        boolean save = userRagSubscriptionService.save(userRagSubscription)
            &&
        SqlUtils.setFieldSqlByFieldName(ragInfoService, "id", ragId, "subscriptions", "subscriptions = subscriptions + 1");

        ThrowUtils.throwIf(!save, ErrorCode.PARAMS_ERROR, "订阅失败, 你或许已经订阅过了");

        return ResultUtils.success(true);
    }

    @PostMapping("/pay")
    public BaseResponse<Boolean> payRag( @RequestParam("ragId") Long ragId, HttpServletRequest httpServletRequest) {
        Long userId = getAndCheckLoginUserId(httpServletRequest);
        User user = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!user.getId().equals(userId), ErrorCode.NO_AUTH_ERROR);


        RagInfo ragInfo = ragInfoService.getById(ragId);
        ThrowUtils.throwIf(ObjectUtil.isNull(ragInfo), ErrorCode.PARAMS_ERROR, "知识库不存在");

        UserRagSubscription userRagSubscription = new UserRagSubscription();
        userRagSubscription.setUserId(userId);
        userRagSubscription.setRagId(ragId);
        userRagSubscription.setPaid(ragInfo.getPrice());

        boolean save = userRagSubscriptionService.save(userRagSubscription);
        ThrowUtils.throwIf(!save, ErrorCode.PARAMS_ERROR, "订阅失败, 你或许已经订阅过了");

        return ResultUtils.success(true);
    }

    @PostMapping("/check/subscribed")
    public BaseResponse<Boolean> checkSubscribed(@RequestParam("ragId") Long ragId, HttpServletRequest httpServletRequest) {
        Long userId = getAndCheckLoginUserId(httpServletRequest);

        Long count = userRagSubscriptionService.lambdaQuery()
                .eq(UserRagSubscription::getUserId, userId)
                .eq(UserRagSubscription::getRagId, ragId)
                .count();

        return ResultUtils.success(count > 0);
    }


    private Long getAndCheckLoginUserId(HttpServletRequest httpServletRequest) {
        User user = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.PARAMS_ERROR);
        return user.getId();
    }
}
