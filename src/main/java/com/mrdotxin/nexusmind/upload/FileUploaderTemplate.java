package com.mrdotxin.nexusmind.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.file.UploadFileResult;
import com.mrdotxin.nexusmind.upload.oss.OSService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Slf4j
public abstract class FileUploaderTemplate {

    @Resource
    private OSService osService;

    public final UploadFileResult uploadFile(Object inputSource, String uploadFilePath, Long limitSize) {
        String suffix = validPicture(inputSource);
        String uuid = RandomUtil.randomString(16);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("%s/%s", uploadFilePath, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            transferToTempFile(inputSource, file);

            if (limitSize >= 0) {
                ThrowUtils.throwIf(FileUtil.size(file) > limitSize, ErrorCode.OPERATION_ERROR, "空间超限! 无法保存个人空间");
            }

            return osService.uploadWithInfo(uploadPath, file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            deleteTempFile(file);
        }
    }

    public final String uploadFileRaw(Object inputSource, String uploadFilePath, Long limitSize) {
        String suffix = validPicture(inputSource);
        String uuid = RandomUtil.randomString(16);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("%s/%s", uploadFilePath, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            transferToTempFile(inputSource, file);

            if (limitSize >= 0) {
                ThrowUtils.throwIf(FileUtil.size(file) > limitSize, ErrorCode.OPERATION_ERROR, "空间超限! 无法保存个人空间");
            }

            return osService.upload(uploadPath, file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            deleteTempFile(file);
        }
    }

    protected abstract String validPicture(Object multipartFile);

    protected abstract void transferToTempFile(Object inputSource, File file) throws IOException;

    public final void deleteTempFile(File file) {
         if (Objects.nonNull(file)) {
             boolean result = file.delete();
             if (!result) {
                 log.error("file delete error, filePath = {}", file.getAbsolutePath());
             }
         }
    }
}


