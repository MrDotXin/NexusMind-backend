package com.mrdotxin.nexusmind.upload.oss.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.config.oss.CosConfig;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.model.dto.file.UploadFileResult;
import com.mrdotxin.nexusmind.upload.oss.OSService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.qcloud.cos.utils.IOUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CosClientService implements OSService {

    @Resource
    private CosConfig cosConfig;

    @Resource
    private COSClient cosClient;

    @Override
    public String upload(String fileKey, File file) {
        try {
            cosClient.putObject(cosConfig.getBucketName(), fileKey, file);

            return cosConfig.getHost() + fileKey;

        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public UploadFileResult uploadRawWithInfo(String fileKey, File file) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    fileKey,
                    file
            );

            PicOperations picOperations = new PicOperations();
            picOperations.setIsPicInfo(1);

            putObjectRequest.setPicOperations(picOperations);

            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            return buildResult(fileKey, file, null, null, imageInfo);
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public UploadFileResult uploadWithInfo(String fileKey, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucketName(), fileKey, file);
        // 设置生成缩略图和压缩图
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> rules = new ArrayList<>();
        if (!fileKey.endsWith(".webp")) {
            rules.add(getWebpPicOperations(fileKey));
        }

        if (FileUtil.size(file) > 100 * 1024) {
            rules.add(getThumbnailPicOperations(fileKey));
        }

        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);

        // 处理生成结果
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

        ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
        List<CIObject> objects = processResults.getObjectList();
        if (CollUtil.isNotEmpty(objects)) {
            CIObject ciObject = objects.get(0);
            CIObject thumbnailObject = ciObject;
            if (objects.size() > 1) {
                thumbnailObject = objects.get(1);
            }

            return buildResult(fileKey, file, ciObject, thumbnailObject, imageInfo);
        }

        return buildResult(fileKey, file, null, null, imageInfo);
    }

    private PicOperations.Rule getWebpPicOperations(String fileKey) {
        String webKey = FileUtil.mainName(fileKey) + ".webp";
        PicOperations.Rule compressionRule = new PicOperations.Rule();
        compressionRule.setRule("imageMogr2/format/webp");
        compressionRule.setBucket(cosConfig.getBucketName());
        compressionRule.setFileId(webKey);

        return compressionRule;
    }

    private PicOperations.Rule getThumbnailPicOperations(String fileKey) {
        String thumbnailKey = FileUtil.mainName(fileKey) + "_thumbnail." + FileUtil.getSuffix(fileKey);
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setFileId(thumbnailKey);
        rule.setBucket(cosConfig.getBucketName());
        rule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 512, 512));
        return rule;
    }

    public UploadFileResult buildResult(String fileKey, File file, CIObject compressed, CIObject thumbnail, ImageInfo rawImage) {
        UploadFileResult uploadFileResult = new UploadFileResult();

        int picWidth = ObjectUtil.isNull(compressed) ? rawImage.getWidth() : compressed.getWidth();
        int picHeight = ObjectUtil.isNull(compressed) ? rawImage.getHeight() : compressed.getHeight();
        String picFormat = ObjectUtil.isNull(compressed) ? rawImage.getFormat() : compressed.getFormat();

        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadFileResult.setPicWidth(picWidth);
        uploadFileResult.setPicHeight(picHeight);
        uploadFileResult.setPicScale(picScale);
        uploadFileResult.setPicFormat(picFormat);
        uploadFileResult.setPicSize(ObjectUtil.isNull(compressed) ? FileUtil.size(file) : compressed.getSize().longValue());
        uploadFileResult.setUrl(cosConfig.getHost() + (ObjectUtil.isNull(compressed) ? fileKey : compressed.getKey()));

        if (ObjectUtil.isNotNull(compressed)) {
            uploadFileResult.setRawFormat(FileUtil.getSuffix(fileKey));
        }

        if (ObjectUtil.isNotNull(thumbnail)) {
            uploadFileResult.setThumbnailUrl(cosConfig.getHost() + thumbnail.getKey());
        }

        return uploadFileResult;
    }

    @Override
    public byte[] getObjectContentByKey(String filePath) {
        try {
            COSObject cosObject = cosClient.getObject(cosConfig.getBucketName(), filePath);
            COSObjectInputStream objectContent = cosObject.getObjectContent();

            return IOUtils.toByteArray(objectContent);
        } catch (CosClientException | IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void removeObjectByUrl(String url) {
        try {
            String key = url.substring(cosConfig.getHost().length());
            cosClient.deleteObject(cosConfig.getBucketName(), key);
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void removeObjectBatchByUrl(List<String> strings) {
        try {
            List<DeleteObjectsRequest.KeyVersion> keyVersions =
                    strings.stream()
                            .map(key -> new DeleteObjectsRequest.KeyVersion(
                                            key.substring(cosConfig.getHost().length())
                                    )
                            ).collect(Collectors.toList());
            DeleteObjectsRequest request = new DeleteObjectsRequest(cosConfig.getBucketName());
            request.setKeys(keyVersions);
            cosClient.deleteObjects(request);
        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void removeObjectByUrlIfExists(String url) {
        try {
            String key = url.substring(cosConfig.getHost().length());
            if (cosClient.doesObjectExist(cosConfig.getBucketName(), key)) {
                cosClient.deleteObject(cosConfig.getBucketName(), key);
            }

        } catch (CosClientException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }
}
