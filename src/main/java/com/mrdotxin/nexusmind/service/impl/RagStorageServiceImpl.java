package com.mrdotxin.nexusmind.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.nexusmind.model.dto.ragStorage.RagStorageQueryRequest;
import com.mrdotxin.nexusmind.model.entity.RagStorage;
import com.mrdotxin.nexusmind.service.RagStorageService;
import com.mrdotxin.nexusmind.mapper.mysql.RagStorageMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Administrator
* @description 针对表【ragstorage(知识库信息表)】的数据库操作Service实现
* @createDate 2025-06-26 23:00:22
*/
@Service
public class RagStorageServiceImpl extends ServiceImpl<RagStorageMapper, RagStorage>
    implements RagStorageService{

    @Override
    public QueryWrapper<RagStorage> getQueryWrapper(RagStorageQueryRequest request) {
        // 提取查询参数
        Long id = request.getId();
        Long ragId = request.getRagId();
        String title = request.getTitle();
        String name = request.getName();
        String type = request.getType();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        // 构建查询条件
        QueryWrapper<RagStorage> queryWrapper = new QueryWrapper<>();

        // 基础字段查询
        queryWrapper.eq(Objects.nonNull(id) && id > 0, "id", id);
        queryWrapper.eq(Objects.nonNull(ragId) && ragId > 0, "ragId", ragId);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.eq(StrUtil.isNotBlank(type), "type", type);

        // 排序处理
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        }

        return queryWrapper;
    }

    @Override
    public boolean removeStoragesByRagId(Long appId) {
        LambdaQueryWrapper<RagStorage> ragStorageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ragStorageLambdaQueryWrapper.eq(RagStorage::getRagId, appId);

        return remove(ragStorageLambdaQueryWrapper);
    }
}




