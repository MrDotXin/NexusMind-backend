package com.mrdotxin.nexusmind.upload.impl;

import cn.hutool.core.io.FileUtil;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.upload.FileUploaderTemplate;
import com.mrdotxin.nexusmind.upload.oss.OSService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class MultipartFileUploader extends FileUploaderTemplate {

    @Resource
    private OSService osService;

    public void transferToTempFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    /**
     *
     * @param inputSource
     * @return 返回对应文件类型
     */
    @Override
    public String validPicture(Object inputSource) {
        ThrowUtils.throwIf(Objects.isNull(inputSource), ErrorCode.PARAMS_ERROR, "文件不能为空");

        MultipartFile multipartFile = (MultipartFile) inputSource;
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;

        ThrowUtils.throwIf(fileSize > 10 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过10M");

        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");

        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");

        return fileSuffix;
    }
}
