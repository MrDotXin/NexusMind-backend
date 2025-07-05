package com.mrdotxin.nexusmind.config.oss;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(CosConfig.class)
public class CosClientConfig {

    @Resource
    private CosConfig cosConfig;

    @Bean
    public COSClient cosClient() {
        COSCredentials cosClient = new BasicCOSCredentials(cosConfig.getSecretID(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));

        return new COSClient(cosClient, clientConfig);
    }
}
