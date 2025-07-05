package com.mrdotxin.nexusmind.ai.tool.component;

import com.alibaba.cloud.ai.toolcalling.baidumap.BaiduMapSearchInfoService;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@AllArgsConstructor
public class BaiduMapSearchInfoTool {

    private BaiduMapSearchInfoService baiduMapSearchInfoService;


    @Tool(description = "提供具体地点, 返回地点详细信息")
    BaiduMapSearchInfoService.Response  searchMapInfoBaidu(
            @ToolParam(description = "User-requested specific location address") String address
    ) {
        BaiduMapSearchInfoService.Request request = new BaiduMapSearchInfoService.Request(address);
        return baiduMapSearchInfoService.apply(request);
    }
}
