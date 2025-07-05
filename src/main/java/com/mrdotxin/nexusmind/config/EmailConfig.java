package com.mrdotxin.nexusmind.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "email.qq")
public class EmailConfig {
    private String smtpHost;
    private String smtpPort;
    private String from;
    private String password;
}
