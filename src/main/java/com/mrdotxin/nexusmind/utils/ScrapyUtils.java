package com.mrdotxin.nexusmind.utils;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScrapyUtils {

    private static final Logger log = LoggerFactory.getLogger(ScrapyUtils.class);

    static public List<String> searchPictureUrlFromBing(String searchText, Integer count, Integer pageStart) {
        String url = String.format(
                "https://cn.bing.com/images/async?q=%s&mmasync=1&first=%d&count=%d",
                searchText, pageStart, count
        );

        try {
            Document document = Jsoup.connect(url).get();

            Element dgController = document.getElementsByClass("dgControl").first();
            ThrowUtils.throwIf(ObjUtil.isNull(dgController), ErrorCode.NOT_FOUND_ERROR);

            Elements elements = dgController.select("img.mimg");

            List<String> list = new ArrayList<>();
            for (Element element : elements) {
                String fileUrl = element.attr("src");
                if (StrUtil.isBlank(fileUrl)) {
                    log.info("空链接 {}", fileUrl);
                    continue;
                }

                int questionMarkIndex = fileUrl.indexOf("?");
                if (questionMarkIndex > -1) {
                    fileUrl = fileUrl.substring(0, questionMarkIndex);
                }

                log.info("获取 {}", fileUrl);
                list.add(fileUrl);
                if (list.size() == count) {
                    break;
                }
            }

            return list;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求错误! 无法获取图片来源");
        }
    }
}
