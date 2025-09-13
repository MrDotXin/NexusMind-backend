package com.mrdotxin.nexusmind.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.nexusmind.ai.persistence.VectorSlicer;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.constant.CommonConstant;
import com.mrdotxin.nexusmind.constant.FileConstant;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoAddRequest;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoQueryRequest;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoUpdateRequest;
import com.mrdotxin.nexusmind.model.entity.RagInfo;
import com.mrdotxin.nexusmind.model.entity.RagStorage;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.model.entity.UserRagSubscription;
import com.mrdotxin.nexusmind.model.vo.RagInfoVO;
import com.mrdotxin.nexusmind.model.vo.UserVO;
import com.mrdotxin.nexusmind.service.*;
import com.mrdotxin.nexusmind.mapper.mysql.RagInfoMapper;
import com.mrdotxin.nexusmind.upload.FileManager;
import com.mrdotxin.nexusmind.utils.SqlUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
*/
@Slf4j
@Service
public class RagInfoServiceImpl extends ServiceImpl<RagInfoMapper, RagInfo>
    implements RagInfoService{

    @Resource
    @Qualifier("pgVectorStore")
    private VectorStore pgVectorStore;

    @Resource
    private UserService userService;

    @Resource
    private RagStorageService ragStorageService;

    @Resource
    private UserRagSubscriptionService userRagSubscriptionService;

    @Resource
    @Qualifier("mysqlTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Resource
    private FileManager fileManager;

    @Resource
    private LikesService likesService;

    @Override
    public QueryWrapper<RagInfo> getQueryWrapper(RagInfoQueryRequest ragInfoQueryRequest) {
        // 提取查询参数
        Long id = ragInfoQueryRequest.getId();
        Long userId = ragInfoQueryRequest.getUserId();
        String avatar = ragInfoQueryRequest.getAvatar();
        String title = ragInfoQueryRequest.getTitle();
        String description = ragInfoQueryRequest.getDescription();
        List<String> tags = ragInfoQueryRequest.getTags();
        String category = ragInfoQueryRequest.getCategory();
        String searchText = ragInfoQueryRequest.getSearchText();
        String sortField = ragInfoQueryRequest.getSortField();
        String sortOrder = ragInfoQueryRequest.getSortOrder();

        // 构建查询条件
        QueryWrapper<RagInfo> queryWrapper = new QueryWrapper<>();

        // 基础字段查询
        queryWrapper.eq(Objects.nonNull(id) && id > 0, "id", id);
        queryWrapper.like(StrUtil.isNotBlank(avatar), "avatar", avatar);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(description), "description", description);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);

        if (ObjectUtil.isNotNull(userId) && userId > 0) {
            queryWrapper.and(q -> q.eq("userId", userId)
                    .or().inSql("id", "select ragId from userRagSubscription where userId = " + userId)
            );
        }

        // 标签查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", tag);
            }
        }

        // 搜索文本（支持多字段模糊匹配）
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(q -> q.like("title", searchText)
                    .or().like("description", searchText)
                    .or().like("category", searchText));
        }

        // 排序处理
        if (StrUtil.isNotBlank(sortField)) {
            SqlUtils.orderBy(queryWrapper, sortField, sortOrder);
        }
        
        return queryWrapper;
    }

    @Override
    public RagInfo uploadRag(MultipartFile multipartFile, Long appId) {
        String suffix = validateRagDocument(multipartFile);
        RagInfo ragInfo = this.getById(appId);
        ThrowUtils.throwIf(ObjectUtil.isNull(ragInfo), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!modifySourceNumbersBy(appId, 1), ErrorCode.OPERATION_ERROR, "元数据更新失败!");

        File file = toTempFile(multipartFile);

        List<Document> documentList = VectorSlicer.doTextSplitter(VectorSlicer.doSlice(file, suffix));
        String version = String.format("%s/%s", DateUtil.now(), multipartFile.getOriginalFilename());
        documentList.forEach(document -> {
                document.getMetadata().put("appId", ragInfo.getId());
                document.getMetadata().put("version", version);
            }
        );
        RagStorage ragStorage = new RagStorage();
        ragStorage.setRagId(ragInfo.getId());
        ragStorage.setTitle(multipartFile.getOriginalFilename());
        ragStorage.setName(version);
        ragStorage.setType(FileUtil.getSuffix(multipartFile.getOriginalFilename()));
        ragStorage.setSize((int) FileUtil.size(file));
        ragStorage.setSlice(documentList.size());
        ragStorageService.save(ragStorage);

        pgVectorStore.add(documentList);


        return ragInfo;
    }

    @Override
    public void addRagInfo(RagInfoAddRequest ragInfoAddRequest, User user) {
        validateRagInfoDTO(ragInfoAddRequest);

        RagInfo ragInfo = new RagInfo();
        BeanUtil.copyProperties(ragInfoAddRequest, ragInfo);

        ragInfo.setUserId(user.getId());

        boolean save = this.save(ragInfo);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "无法添加知识库! 检查是否有内容为空或者重复");
    }

    @Override
    public void updateRag(RagInfoUpdateRequest request) {
        validateRagInfoDTO(request);

        RagInfo ragInfo = buildRagInfoWithDTO(request);

        boolean save = this.updateById(ragInfo);

        ThrowUtils.throwIf(save, ErrorCode.OPERATION_ERROR, "无法保存!");
    }

    @Override
    public void validateRagInfoDTO(Object dtoRequest) {
        RagInfo ragInfo = new RagInfo();
        BeanUtil.copyProperties(dtoRequest, ragInfo);

        if (ObjectUtils.anyNull(ragInfo.getTitle(), ragInfo.getDescription())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题或者简介不能为空");
        }

        if (ragInfo.getTitle().length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长! 不能超过128个字符");
        }

        if (ragInfo.getDescription().length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "简介过长! 不能超过1000个字符");
        }
    }

    @Override
    public RagInfo buildRagInfoWithDTO(RagInfoUpdateRequest request) {
        RagInfo ragInfo = new RagInfo();
        BeanUtil.copyProperties(request, ragInfo);

        return ragInfo;
    }

    @Override
    public void deleteRAGDocumentByName(Long appId, Long documentId) {
        FilterExpressionBuilder filterExpressionBuilder =  new FilterExpressionBuilder();
        RagStorage ragStorage = ragStorageService.getById(documentId);

        if(!ragStorage.getRagId().equals(appId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库与文档不匹配!");
        }

        boolean remove = ragStorageService.removeById(ragStorage.getId())
                &&
        modifySourceNumbersBy(appId, -1);

        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR, "删除失败!");

        pgVectorStore.delete(
                filterExpressionBuilder.and(
                    filterExpressionBuilder.eq("appId", appId),
                    filterExpressionBuilder.eq("version", ragStorage.getName())
                ).build()
        );
    }

    @Override
    public void deleteRAGDocumentById(Long appId) {
        RagInfo ragInfo = this.getById(appId);
        ThrowUtils.throwIf(ObjectUtil.isNull(ragInfo), ErrorCode.PARAMS_ERROR, "知识库不存在!");

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            boolean remove = this.removeById(appId);
            if (ragInfo.getLikes() > 0) {
                remove = remove & likesService.removeLikedBy(appId, "ragInfo");
            }

            if (ragInfo.getSources() > 0) {
                remove = remove & ragStorageService.removeStoragesByRagId(appId);
            }

            if (ragInfo.getSubscriptions() > 0) {
                remove = remove & ragStorageService.removeStoragesByRagId(appId);
            }

            ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR, "无法删除这个知识库!");
        });

        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        pgVectorStore.delete(filterExpressionBuilder.eq("appId", appId).build());
    }

    @Override
    public RagInfoVO getRagInfoVO(RagInfo ragInfoList) {
        Long userId = ragInfoList.getUserId();

        RagInfoVO ragInfoVO = new RagInfoVO();
        BeanUtil.copyProperties(ragInfoList, ragInfoVO);

        if (ObjectUtil.isNotNull(userId) && userId > 0) {
            UserVO user = userService.getUserVO(userService.getById(userId));
            ragInfoVO.setUser(user);
        }

        return ragInfoVO;
    }

    @Override
    public String uploadRagInfoAvatar(String policy, Object source, User user, RagInfo rag) {
        ThrowUtils.throwIf(ObjectUtil.isNull(rag), ErrorCode.PARAMS_ERROR, "该知识库不存在");
        ThrowUtils.throwIf(!user.getId().equals(rag.getUserId()), ErrorCode.NO_AUTH_ERROR);
        String avatar = fileManager.uploadFileRaw(policy, source, FileConstant.UPLOAD_FILE_PATH + "/ragInfo", 10 * 1024 * 1024L);

        String oldPic = rag.getAvatar();
        boolean update = this.lambdaUpdate()
                .eq(RagInfo::getId, rag.getId())
                .set(RagInfo::getAvatar, avatar)
                .update();

        if (!update) {
            fileManager.removeObjectByUrlIfExists(avatar);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法上传该头像!");
        } else {
            if (oldPic.contains(FileConstant.UPLOAD_FILE_PATH)) {
                fileManager.removeObjectByUrlIfExists(oldPic);
            }

        }

        return avatar;
    }


    private String validateRagDocument(MultipartFile file) {
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        ThrowUtils.throwIf(StrUtil.isEmpty(fileType), ErrorCode.PARAMS_ERROR);

        String type = fileType.substring(fileType.lastIndexOf('/') + 1).toLowerCase();

        final List<String> allowedPrefix = Arrays.asList("json", "md", "pdf", "txt", "plain");
        if (!allowedPrefix.contains(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式错误");
        }

        return type;
    }

    private File toTempFile(MultipartFile multipartFile) {
        String name = multipartFile.getOriginalFilename();

        try {
            File file = File.createTempFile("tmp/" + name, null);
            multipartFile.transferTo(file);

            return file;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    private boolean modifySourceNumbersBy(Long appId, Integer integer) {
        return this.lambdaUpdate()
                .eq(RagInfo::getId, appId)
                .setSql(String.format("sources = sources + %d", integer))
                .update();
    }
}




