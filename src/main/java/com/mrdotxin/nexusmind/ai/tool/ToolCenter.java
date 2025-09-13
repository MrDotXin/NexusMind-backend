package com.mrdotxin.nexusmind.ai.tool;

import com.alibaba.cloud.ai.toolcalling.baidumap.BaiDuMapWeatherService;
import com.alibaba.cloud.ai.toolcalling.baidumap.BaiduMapSearchInfoService;
import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduSearchService;
import com.mrdotxin.nexusmind.ai.tool.component.*;
import com.mrdotxin.nexusmind.config.EmailConfig;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

@Component
public class ToolCenter {

    @Resource
    private BaiduSearchService baiduSearchService;

    @Resource
    private EmailConfig emailConfig;

    @Resource
    private BaiDuMapWeatherService baiDuMapWeatherService;

    @Resource
    private BaiduMapSearchInfoService baiduMapSearchInfoService;

    public ToolCallbackProvider getAllTools() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();

        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();

        SearchTools searchTools = new SearchTools(baiduSearchService);

        FileOperationTool fileOperationTool = new FileOperationTool();

        MailSendTool mailSendTool = new MailSendTool(emailConfig);

        BaiduMapWeatherSearchInfoTool baiduMapWeatherSearchInfoTool = new BaiduMapWeatherSearchInfoTool(baiDuMapWeatherService);

        AskHumanTool askHumanTool = new AskHumanTool();
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(
                        pdfGenerationTool,
                        resourceDownloadTool,
                        searchTools,
                        fileOperationTool,
                        mailSendTool,
                        baiduMapWeatherSearchInfoTool,
                        askHumanTool
                )
                .build();
    }
}
