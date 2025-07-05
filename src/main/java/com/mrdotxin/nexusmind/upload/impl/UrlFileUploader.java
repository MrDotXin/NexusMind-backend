package com.mrdotxin.nexusmind.upload.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.upload.FileUploaderTemplate;
import com.mrdotxin.nexusmind.upload.oss.OSService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class UrlFileUploader extends FileUploaderTemplate {

    @Resource
    private OSService osService;

    @Value("${limit.img}")
    private Long maxImageUploadSize;

    public void transferToTempFile(Object inputSource, File file) throws IOException {
        String url = (String) inputSource;
        HttpUtil.downloadFile(url, file);
    }
    /**
     * 对于url 请求, 校验请求头
     * @param inputSource String
     */
    @Override
    public String validPicture(Object inputSource) {
        ThrowUtils.throwIf(Objects.isNull(inputSource), ErrorCode.PARAMS_ERROR, "文件不能为空");
        String url = (String) inputSource;

        ThrowUtils.throwIf(!(url.startsWith("http://") || url.startsWith("https://")), ErrorCode.PARAMS_ERROR);

        try { new URL(url); } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
        }

        try ( HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, url).execute()) {

            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw  new BusinessException(ErrorCode.PARAMS_ERROR, "无法获取图片信息");
            }

            String contentType = httpResponse.header("Content-Type");

            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType), ErrorCode.FORBIDDEN_ERROR, "禁止的文件类型!");
            }
            String contentLength = httpResponse.header("Content-Length");

            if (StrUtil.isNotBlank(contentLength)) {
                long length = Long.parseLong(contentLength);
                final long ONE_M = 1024 * 1024;
                final long limit = maxImageUploadSize * ONE_M;
                ThrowUtils.throwIf(limit < length, ErrorCode.FORBIDDEN_ERROR, "上传文件太大! 不得超过 " + limit + "MB");
            }

            return contentType.substring(6);
        } catch (Exception e) {
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
