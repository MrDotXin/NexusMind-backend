package com.mrdotxin.nexusmind.ai.tool.component;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class WebHtmlReaderTool {

    @Tool(description = "see the detailed page from url")
    public String checkDetailedWebPage(
            @ToolParam(description = "URL of the web page to scrape") String url,
            @ToolParam(description = "timeout for connect least(ms) at least 5000. directly reply timeout if timeout") Integer timeout
    ) {
        try {
            Document doc = Jsoup.connect(url).proxy(getProxy()).timeout(timeout).get();
            // 提取关键信息以减少token
            String result = doc.html();
            String simplifiedText = extractContent(doc);
            if (StrUtil.isNotBlank(simplifiedText)) {
                result = simplifiedText;
            }

            if (result.length() > 50000) {
                result = "内容文本超出限制";
            }

            return result;
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }


    private String extractContent(Document document) {
        try {
            // 移除不必要信息
            document.select("script, style, noscript, iframe, embed, object, link, meta, svg, img").remove();

            Elements allElements = document.select("*");
            // 移除不必要属性
            for (Element element : allElements) {
                element.removeAttr("class");
                element.removeAttr("id");
                element.removeAttr("style");
                element.removeAttr("onclick");
                // 保留href属性以便AI理解链接
                if (!element.tagName().equals("a")) {
                    element.removeAttr("href");
                }
            }
            String simplifiedHtml = ObjectUtil.isNotNull(document.body()) ? document.body().html() : document.html();

            // 进一步压缩空白字符
            simplifiedHtml = simplifiedHtml.replaceAll("\\s+", " ").trim();

            // 使用正则表达式清除前端框架自定义标签
            simplifiedHtml = simplifiedHtml.replaceAll("\\s(data-|v-|@)[^=]+=\"[^\"]+\"", "");

            return simplifiedHtml;
        } catch (Exception e) {
            return "";
        }
    }

    private Proxy getProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
    }
}
