package com.mrdotxin.nexusmind.ai.tool.component;

import com.alibaba.cloud.ai.toolcalling.baidumap.BaiDuMapWeatherService;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@AllArgsConstructor
public class BaiduMapWeatherSearchInfoTool {

    private BaiDuMapWeatherService baiDuMapWeatherService;

    @Tool(description = "get weather info for address you provide")
    BaiDuMapWeatherService.Response  searchMapWeatherInfoBaidu(
            @ToolParam(description = "Get the weather conditions for a specified address.") String address
    ) {
        BaiDuMapWeatherService.Request request = new BaiDuMapWeatherService.Request(address);
        return baiDuMapWeatherService.apply(request);
    }
}
