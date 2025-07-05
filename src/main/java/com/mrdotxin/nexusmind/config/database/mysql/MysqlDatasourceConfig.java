package com.mrdotxin.nexusmind.config.database.mysql;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource.mysql")
public class MysqlDatasourceConfig {
    private String url;
    private String driverClassName;
    private String username;
    private String password;
    private HikariCPProperties hikari;

    @Data
    public static class HikariCPProperties {
        private int maximumPoolSize;
        private int minimumIdle;
    }
}
