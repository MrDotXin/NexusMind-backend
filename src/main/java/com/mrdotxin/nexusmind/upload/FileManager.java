package com.mrdotxin.nexusmind.upload;

import cn.hutool.core.collection.CollUtil;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.model.dto.file.UploadFileResult;
import com.mrdotxin.nexusmind.model.enums.FileUploadTypeEnum;
import com.mrdotxin.nexusmind.upload.impl.MultipartFileUploader;
import com.mrdotxin.nexusmind.upload.impl.UrlFileUploader;
import com.mrdotxin.nexusmind.upload.oss.OSService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


// 对upload功能再抽象, 最后合成一个总的服务类
@Slf4j
@Service
public class FileManager {

    @Resource
    private OSService osService;

    @Resource
    private UrlFileUploader urlFileUploader;

    @Resource
    private MultipartFileUploader multipartFileUploader;

    public UploadFileResult uploadFile(String policy, Object inputSource, String uploadFilePath, Long limitSize) {
        return from(policy).uploadFile(inputSource, uploadFilePath, limitSize);
    }

    public String uploadFileRaw(String policy, Object inputSource, String uploadFilePath, Long limitSize) {
        return from(policy).uploadFileRaw(inputSource, uploadFilePath, limitSize);
    }

    public FileUploaderTemplate from(String policy) {
        if (policy.equals(FileUploadTypeEnum.MULTIPART_FILE.getValue())) {
            return multipartFileUploader;
        }

        if (policy.equals(FileUploadTypeEnum.URL.getValue())) {
            return urlFileUploader;
        }

        throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误的上传类型!");
    }

    public final void removeObjectByUrl(String url) {
        osService.removeObjectByUrl(url);
    }

    public final void removeObjectBatchByUrl(List<String> strings) {
        if (CollUtil.isNotEmpty(strings)) {
            osService.removeObjectBatchByUrl(strings);
        }
    }

    public final void removeObjectByUrlIfExists(String url) {
        osService.removeObjectByUrlIfExists(url);
    }

}
