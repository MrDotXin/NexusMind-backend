package com.mrdotxin.nexusmind.config.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "oss")
public class CosConfig {

  private String bucketName;

  private String secretKey;

  private String secretID;

  private String host;

  private String region;
}
