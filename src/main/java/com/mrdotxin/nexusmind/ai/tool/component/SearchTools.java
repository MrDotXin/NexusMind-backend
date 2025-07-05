package com.mrdotxin.nexusmind.ai.tool.component;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduSearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

public class SearchTools {
    private final BaiduSearchService baiduSearchService;

    public SearchTools(BaiduSearchService baiduSearchService) {
        this.baiduSearchService = baiduSearchService;
    }

    @Tool(description = "Baidu Search Tool - Returns titles and URLs only. For detailed page content extraction, use Playwright tools instead.")
    public List<BaiduSearchService.SearchResult> searchFromBaidu(
            @ToolParam(description = "The exact query to search for on Baidu") String search,
            @ToolParam(description = "Number of results to return (max 10)") Integer limits
    ) {
        return search(search, limits);
    }

    private List<BaiduSearchService.SearchResult> search(String search, Integer limits) {
        if (limits > 10) {
            BaiduSearchService.SearchResult searchResult = new BaiduSearchService.SearchResult("错误", "你的搜索条数不能超过10条", "");
        }

        BaiduSearchService.Request request = new BaiduSearchService.Request(search, limits);
        BaiduSearchService.Response apply = baiduSearchService.apply(request);
        if (ObjectUtil.isNull(apply)) {
            return List.of(new BaiduSearchService.SearchResult("错误!", "搜索功能暂时不可用, 稍后重试", ""));
        }

        return apply.results();
    }
}
