package com.mrdotxin.nexusmind.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrdotxin.nexusmind.common.BaseResponse;
import com.mrdotxin.nexusmind.common.ResultUtils;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoQueryRequest;
import com.mrdotxin.nexusmind.model.dto.ragStorage.RagStorageQueryRequest;
import com.mrdotxin.nexusmind.model.entity.RagInfo;
import com.mrdotxin.nexusmind.model.entity.RagStorage;
import com.mrdotxin.nexusmind.model.vo.RagInfoVO;
import com.mrdotxin.nexusmind.service.RagStorageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ragStorage")
public class RagStorageController {

    @Resource
    private RagStorageService ragStorageService;

    @PostMapping("/list/page")
    public BaseResponse<Page<RagStorage>> listRagStorageByPage(@RequestBody RagStorageQueryRequest request) {
        long current = request.getCurrent();
        long size = request.getPageSize();

        Page<RagStorage> userPage = ragStorageService.page(new Page<>(current, size),
                ragStorageService.getQueryWrapper(request));

        return ResultUtils.success(userPage);
    }
}
