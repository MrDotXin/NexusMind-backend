package com.mrdotxin.nexusmind.controller;

import cn.hutool.core.util.ObjectUtil;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.component.MybatisServiceSelector;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.likes.DoLikeRequest;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.service.LikesService;
import com.mrdotxin.nexusmind.service.UserService;
import com.mrdotxin.nexusmind.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
public class LikesController {

    @Resource
    private MybatisServiceSelector mybatisServiceSelector;

    @Resource
    private UserService userService;

    @Resource
    private LikesService likesService;

    @PostMapping("/do/like")
    public BaseResponse<Boolean> doLike(@RequestBody DoLikeRequest doLikeRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(doLikeRequest), ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!user.getId().equals(doLikeRequest.getUserId()), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!SqlUtils.checkFieldExist(mybatisServiceSelector.from(doLikeRequest.getTarget()), "id", doLikeRequest.getTargetId()), ErrorCode.PARAMS_ERROR, "点赞目标不存在");

        likesService.like(doLikeRequest);

        return ResultUtils.success(true);
    }

    @PostMapping("/cancel/like")
    public BaseResponse<Boolean> cancelLike(@RequestBody DoLikeRequest doLikeRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(doLikeRequest), ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!user.getId().equals(doLikeRequest.getUserId()), ErrorCode.PARAMS_ERROR);

        likesService.unlike(doLikeRequest);

        return ResultUtils.success(true);
    }

    @PostMapping("/is/like")
    public BaseResponse<Boolean> isLike(@RequestBody DoLikeRequest doLikeRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(doLikeRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(likesService.hasLiked(doLikeRequest));
    }
}
