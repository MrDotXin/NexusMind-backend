package com.mrdotxin.nexusmind.upload.oss;

import com.mrdotxin.nexusmind.model.dto.file.UploadFileResult;

import java.io.File;
import java.util.List;

public interface OSService {

    /**
     * 普通接口, 单纯地上传然后返回url
     *
     * @param fileKey
     * @param file
     * @return oss url
     */
    String upload(String fileKey, File file);

    /**
     * 可获取信息的接口, 上传然后解析得到图片信息
     *
     * @param fileKey
     * @param file
     * @return
     */
    UploadFileResult uploadRawWithInfo(String fileKey, File file);

    /**
     * 完整接口, 会尝试无损压缩原图、生成缩略图, 解析图片信息
     *
     * @param fileKey
     * @param file
     * @return
     */
    UploadFileResult uploadWithInfo(String fileKey, File file);

    /**
     *
     * @param filePath
     * @return
     */
    byte[] getObjectContentByKey(String filePath);

    /**
     *
     * @param url
     */
    void removeObjectByUrl(String url);

    /**
     * @param url
     */
    void removeObjectByUrlIfExists(String url);

    /**
     * 批量删除
     *
     * @param url
     */
    void removeObjectBatchByUrl(List<String> url);
}
