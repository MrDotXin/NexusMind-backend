package com.mrdotxin.nexusmind.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrdotxin.nexusmind.model.dto.ragStorage.RagStorageQueryRequest;
import com.mrdotxin.nexusmind.model.entity.RagStorage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【ragstorage(知识库信息表)】的数据库操作Service
* @createDate 2025-06-26 23:00:22
*/
public interface RagStorageService extends IService<RagStorage> {

    QueryWrapper<RagStorage> getQueryWrapper(RagStorageQueryRequest request);

    boolean removeStoragesByRagId(Long appId);
}
