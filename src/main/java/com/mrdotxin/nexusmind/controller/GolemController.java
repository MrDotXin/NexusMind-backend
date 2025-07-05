package com.mrdotxin.nexusmind.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrdotxin.nexusmind.annotation.AuthCheck;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.DeleteRequest;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.constant.UserConstant;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.golem.GolemAddRequest;
import com.mrdotxin.nexusmind.model.dto.golem.GolemQueryRequest;
import com.mrdotxin.nexusmind.model.dto.golem.GolemUpdateRequest;
import com.mrdotxin.nexusmind.model.dto.upload.FileUploadRequest;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.mrdotxin.nexusmind.model.entity.RagInfo;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.model.enums.FileUploadTypeEnum;
import com.mrdotxin.nexusmind.model.vo.GolemVO;
import com.mrdotxin.nexusmind.service.GolemService;
import com.mrdotxin.nexusmind.service.LikesService;
import com.mrdotxin.nexusmind.service.RagInfoService;
import com.mrdotxin.nexusmind.service.UserService;
import com.mrdotxin.nexusmind.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/golem")
public class GolemController {
    @Resource
    private GolemService golemService;

    @Resource
    private UserService userService;

    @Resource
    private LikesService likesService;

    @Resource
    private RagInfoService ragInfoService;


    //// base

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addGolem(@RequestBody GolemAddRequest request, HttpServletRequest httpServletRequest) {
        Golem golem = new Golem();
        BeanUtils.copyProperties(request, golem);

        User user = userService.getLoginUser(httpServletRequest);
        golem.setUserId(user.getId());

        boolean result = golemService.save(golem);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(golem.getId());
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteGolem(@RequestBody DeleteRequest deleteRequest) {
        boolean b = golemService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGolem(@RequestBody GolemUpdateRequest request) {
        Golem golem = new Golem();
        BeanUtils.copyProperties(request, golem);
        boolean result = golemService.updateById(golem);
        return ResultUtils.success(result);
    }

    @GetMapping("/get")
    public BaseResponse<Golem> getGolemById(long id, HttpServletRequest request) {
        Golem golem = golemService.getById(id);
        ThrowUtils.throwIf(golem == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(golemService.getById(id));
    }

    @GetMapping("/get/vo")
    public BaseResponse<GolemVO> getGolemVOById(long id, HttpServletRequest request) {
        return ResultUtils.success(golemService.getGolemVO(id));
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<Golem>> listGolemByPage(@RequestBody GolemQueryRequest request,
                                                     HttpServletRequest httpRequest) {

        QueryWrapper<Golem> queryWrapper = golemService.getQueryWrapper(request);
        queryWrapper.eq("isPublic", 1);

        Page<Golem> page = golemService.page(new Page<>(request.getCurrent(), request.getPageSize()),
                queryWrapper);

        return ResultUtils.success(page);
    }

    @PostMapping("/list/my/page")
    public BaseResponse<Page<Golem>> listMyGolemByPage(@RequestBody GolemQueryRequest request,
                                                     HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR);

        request.setUserId(user.getId());
        Page<Golem> page = golemService.page(new Page<>(request.getCurrent(), request.getPageSize()),
                golemService.getQueryWrapper(request));

        return ResultUtils.success(page);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GolemVO>> listGolemVOByPage(@RequestBody GolemQueryRequest request,
                                                     HttpServletRequest httpRequest) {
        Page<Golem> page = golemService.page(new Page<>(request.getCurrent(), request.getPageSize()),
                golemService.getQueryWrapper(request));

        return ResultUtils.success(toVOPage(page));
    }

    @PostMapping("/set/public")
    public BaseResponse<Boolean> setMyGolemPublic(@RequestParam Long golemId, @RequestParam Boolean isPublic,
                                                     HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        Golem golem = golemService.getById(golemId);
        ThrowUtils.throwIf(ObjectUtil.isNull(golem), ErrorCode.OPERATION_ERROR, "不存在的智能体!");
        ThrowUtils.throwIf(!user.getId().equals(golem.getUserId()), ErrorCode.NO_AUTH_ERROR);

        boolean check = SqlUtils.setFieldPropertyByFieldName(golemService, "id", golemId, "isPublic", isPublic ? 1 : 0);
        ThrowUtils.throwIf(!check, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    /// extensions

    @PostMapping("/bind/rags")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> bindGolemWithRagId(@RequestParam("golemId") Long golemId, @RequestParam("ragId") Long ragId) {
        Golem golem = golemService.getById(golemId);
        RagInfo ragInfo = ragInfoService.getById(ragId);

        ThrowUtils.throwIf(ObjectUtils.anyNull(golem, ragInfo), ErrorCode.PARAMS_ERROR, "找不到对应资源!");

        List<Long> rags = golem.getRags();
        ThrowUtils.throwIf(rags.contains(ragId), ErrorCode.OPERATION_ERROR, "该知识库已被绑定!");

        rags.add(ragId);

        boolean updated = golemService.lambdaUpdate()
                .eq(Golem::getId, golemId)
                .set(Golem::getRags, rags)
                .update();
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    @PostMapping("/unbind/rags")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> removeRagFromGolem(@RequestParam("golemId") Long golemId, @RequestParam("ragId") Long ragId) {
        Golem golem = golemService.getById(golemId);
        RagInfo ragInfo = ragInfoService.getById(ragId);

        ThrowUtils.throwIf(ObjectUtils.anyNull(golem, ragInfo), ErrorCode.PARAMS_ERROR, "找不到对应资源!");

        List<Long> rags = golem.getRags();
        ThrowUtils.throwIf(!rags.contains(ragId), ErrorCode.OPERATION_ERROR, "该知识库未被绑定!");

        rags.remove(ragId);

        boolean updated = golemService.lambdaUpdate()
                .eq(Golem::getId, golemId)
                .set(Golem::getRags, rags)
                .update();
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    @PostMapping(value = "/upload/avatar/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> uploadGolemAvatarByMultipart(@RequestPart("file") MultipartFile file, @RequestParam("golem") Long golemId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(file), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isNull(golemId) || golemId == 0, ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);

        String avatar = golemService.uploadGolemAvatar(FileUploadTypeEnum.MULTIPART_FILE.getValue(), file, user, golemService.getById(golemId));

        return ResultUtils.success(avatar);
    }

    @PostMapping("/upload/avatar/url")
    public BaseResponse<String> uploadGolemAvatarByUrl(@RequestParam("url") String fileUrl, @RequestBody FileUploadRequest fileUploadRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(fileUrl), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isNull(fileUploadRequest), ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);
        String avatar = golemService.uploadGolemAvatar(FileUploadTypeEnum.URL.getValue(), fileUrl, user, golemService.getById(fileUploadRequest.getId()));

        return ResultUtils.success(avatar);
    }

    private Page<GolemVO> toVOPage(Page<Golem> page) {
        List<Golem> records = page.getRecords();
        Page<GolemVO> voPage = new Page<>();
        BeanUtil.copyProperties(page, voPage);

        voPage.setRecords(
                records.stream().map(
                        golem -> golemService.getGolemVO(golem)
                ).toList()
        );
        return voPage;
    }

}
