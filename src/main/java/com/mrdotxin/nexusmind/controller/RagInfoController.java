package com.mrdotxin.nexusmind.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrdotxin.nexusmind.annotation.AuthCheck;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.constant.UserConstant;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoAddRequest;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoQueryRequest;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoUpdateRequest;
import com.mrdotxin.nexusmind.model.dto.upload.FileUploadRequest;
import com.mrdotxin.nexusmind.model.entity.RagInfo;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.model.enums.FileUploadTypeEnum;
import com.mrdotxin.nexusmind.model.vo.RagInfoVO;
import com.mrdotxin.nexusmind.service.RagInfoService;
import com.mrdotxin.nexusmind.service.RagStorageService;
import com.mrdotxin.nexusmind.service.UserService;
import com.mrdotxin.nexusmind.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagInfoController {

    @Resource
    private RagInfoService ragInfoService;

    @Resource
    private UserService userService;

    @Resource
    private RagStorageService ragStorageService;

    @PostMapping(value = "/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addRag(@RequestBody RagInfoAddRequest ragInfoAddRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(ragInfoAddRequest), ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);
        ragInfoService.addRagInfo(ragInfoAddRequest, user);


        return ResultUtils.success(true);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<RagInfo> uploadRag(@RequestPart("file") MultipartFile file, @RequestParam("appId") Long appId) {
        ThrowUtils.throwIf(ObjectUtil.isNull(file), ErrorCode.PARAMS_ERROR);

        RagInfo ragInfo = ragInfoService.uploadRag(file, appId);

        return ResultUtils.success(ragInfo);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateRag(@RequestBody RagInfoUpdateRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCode.PARAMS_ERROR);

        Long appId = request.getId();
        ThrowUtils.throwIf(!SqlUtils.checkFieldExist(ragInfoService, "id", appId), ErrorCode.PARAMS_ERROR, "不存在的知识库!");

        ragInfoService.updateRag(request);
        return ResultUtils.success(true);
    }

    @PostMapping("/delete/{appId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteRag(@PathVariable("appId") Long appId) {
        ThrowUtils.throwIf(!SqlUtils.validId(appId), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!SqlUtils.checkFieldExist(ragInfoService, "id", appId), ErrorCode.PARAMS_ERROR, "该知识库不存在!");

        ragInfoService.deleteRAGDocumentById(appId);
        return ResultUtils.success(true);
    }

    @PostMapping("/retrieve/{appId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> retrieveRAGDocument(@PathVariable("appId") Long appId, @RequestParam("documentId") Long document) {
        ThrowUtils.throwIf(!SqlUtils.checkFieldExist(ragInfoService, "id", appId), ErrorCode.PARAMS_ERROR, "该知识库不存在!");

        ragInfoService.deleteRAGDocumentByName(appId, document);
        return ResultUtils.success(true);
    }

    @PostMapping("/rag/{ragId}")
    public BaseResponse<RagInfo> getRagInfoById(@PathVariable("ragId") Long ragId) {
        ThrowUtils.throwIf(!SqlUtils.validId(ragId), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!SqlUtils.checkFieldExist(ragInfoService,"id", ragId), ErrorCode.PARAMS_ERROR, "该知识库不存在!");

        RagInfo ragInfo = ragInfoService.getById(ragId);
        ThrowUtils.throwIf(ObjectUtil.isNull(ragInfo), ErrorCode.PARAMS_ERROR, "无法找到相关知识库!");

        return ResultUtils.success(ragInfo);
    }

    @PostMapping("/rag/vo/{ragId}")
    public BaseResponse<RagInfoVO> getRagInfoVOById(@PathVariable("ragId") Long ragId) {
        ThrowUtils.throwIf(!SqlUtils.validId(ragId), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!SqlUtils.checkFieldExist(ragInfoService, "id", ragId), ErrorCode.PARAMS_ERROR, "该知识库不存在!");

        RagInfo ragInfo = ragInfoService.getById(ragId);
        ThrowUtils.throwIf(ObjectUtil.isNull(ragInfo), ErrorCode.PARAMS_ERROR, "无法找到相关知识库!");

        return ResultUtils.success(ragInfoService.getRagInfoVO(ragInfo));
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<RagInfo>> listRagInfoByPage(@RequestBody RagInfoQueryRequest request, HttpServletRequest httpServletRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();

        Long userId = request.getUserId();
        if (SqlUtils.validId(userId)) {
            User user = userService.getLoginUser(httpServletRequest);
            userService.validateIsAdminOrOwner(user, userId);
        }

        Page<RagInfo> userPage = ragInfoService.page(new Page<>(current, size),
                ragInfoService.getQueryWrapper(request));

        return ResultUtils.success(userPage);
    }

    @PostMapping("/list/my/page")
    public BaseResponse<Page<RagInfo>> listMyRagInfoByPage(@RequestBody RagInfoQueryRequest request, HttpServletRequest httpServletRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();

        User user = userService.getLoginUser(httpServletRequest);

        request.setUserId(user.getId());
        Page<RagInfo> userPage = ragInfoService.page(new Page<>(current, size),
                ragInfoService.getQueryWrapper(request));

        return ResultUtils.success(userPage);
    }

    @PostMapping("/list/vo/page")
    public BaseResponse<Page<RagInfoVO>> listRagInfoVOByPage(@RequestBody RagInfoQueryRequest request, HttpServletRequest httpServletRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();

        Long userId = request.getUserId();
        if (SqlUtils.validId(userId)) {
            User user = userService.getLoginUser(httpServletRequest);
            userService.validateIsAdminOrOwner(user, userId);
        }

        Page<RagInfo> userPage = ragInfoService.page(new Page<>(current, size),
                ragInfoService.getQueryWrapper(request));

        return ResultUtils.success(toVOPage(userPage));
    }

    @PostMapping("/list/vo/my/page")
    public BaseResponse<Page<RagInfoVO>> listMyRagInfoVOByPage(@RequestBody RagInfoQueryRequest request, HttpServletRequest httpServletRequest) {
        long current = request.getCurrent();
        long size = request.getPageSize();

        User user = userService.getLoginUser(httpServletRequest);

        request.setUserId(user.getId());
        Page<RagInfo> userPage = ragInfoService.page(new Page<>(current, size),
                ragInfoService.getQueryWrapper(request));

        return ResultUtils.success(toVOPage(userPage));
    }

    @PostMapping("/set/public")
    public BaseResponse<Boolean> setMyRagInfoPublic(@RequestParam("ragId") Long ragId, @RequestParam("isPublic") Boolean isPublic,
                                                     HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        RagInfo ragInfo = ragInfoService.getById(ragId);
        ThrowUtils.throwIf(ObjectUtil.isNotNull(ragInfo), ErrorCode.OPERATION_ERROR, "不存在的智能体!");
        ThrowUtils.throwIf(!user.getId().equals(ragInfo.getUserId()), ErrorCode.NO_AUTH_ERROR);

        boolean check = SqlUtils.setFieldPropertyByFieldName(ragInfoService,"id", ragId, "isPublic", isPublic ? 1 : 0);
        ThrowUtils.throwIf(!check, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    @PostMapping(value = "/upload/avatar/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> uploadRagInfoAvatarByMultipart(@RequestPart("file") MultipartFile file, @RequestParam("appId") Long appId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(file), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isNull(appId), ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);

        String avatar = ragInfoService.uploadRagInfoAvatar(FileUploadTypeEnum.MULTIPART_FILE.getValue(), file, user, ragInfoService.getById(appId));

        return ResultUtils.success(avatar);
    }

    @PostMapping("/upload/avatar/url")
    public BaseResponse<String> uploadRagInfoAvatarByUrl(@RequestParam("url") String fileUrl, @RequestBody FileUploadRequest fileUploadRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(fileUrl), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isNull(fileUploadRequest), ErrorCode.PARAMS_ERROR);

        User user = userService.getLoginUser(httpServletRequest);
        String avatar = ragInfoService.uploadRagInfoAvatar(FileUploadTypeEnum.URL.getValue(), fileUrl, user, ragInfoService.getById(fileUploadRequest.getId()));

        return ResultUtils.success(avatar);
    }

    private Page<RagInfoVO> toVOPage(Page<RagInfo> page) {
        List<RagInfo> records = page.getRecords();
        Page<RagInfoVO> voPage = new Page<>();
        BeanUtil.copyProperties(page, voPage);

        voPage.setRecords(
                records.stream().map(
                        ragInfo -> ragInfoService.getRagInfoVO(ragInfo)
                ).toList()
        );
        return voPage;
    }
}
