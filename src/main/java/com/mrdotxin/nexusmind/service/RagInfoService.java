package com.mrdotxin.nexusmind.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoAddRequest;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoQueryRequest;
import com.mrdotxin.nexusmind.model.dto.rag.RagInfoUpdateRequest;
import com.mrdotxin.nexusmind.model.entity.RagInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.model.vo.RagInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author Administrator
* @description 针对表【raginfo(知识库信息表)】的数据库操作Service
* @createDate 2025-06-24 09:45:07
*/
public interface RagInfoService extends IService<RagInfo> {

    /**
     * s
     * @param file 文件名
     * @param appId 这个参数为0表示新建知识库
     */
    RagInfo uploadRag(MultipartFile file, Long appId);

    void addRagInfo(RagInfoAddRequest ragInfoAddRequest, User user);

    void updateRag(RagInfoUpdateRequest request);

    void validateRagInfoDTO(Object dtoRequest);

    RagInfo buildRagInfoWithDTO(RagInfoUpdateRequest request);

    /**
     *只删除某一类知识库, 防止被投毒
     */
    void deleteRAGDocumentByName(Long appId, Long document);

    /**
     *
     * @param appId 知识库标识符, 会删除整个知识库
     */
    void deleteRAGDocumentById(Long appId);

    RagInfoVO getRagInfoVO(RagInfo ragInfoList);

    String uploadRagInfoAvatar(String policy, Object source, User user, RagInfo golem);


    QueryWrapper<RagInfo> getQueryWrapper(RagInfoQueryRequest queryRequest);
}
