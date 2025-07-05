package com.mrdotxin.nexusmind.config.openapi;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mrdotxin.nexusmind.config.openapi.converter.LongToStringConverter;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.Collections;
import java.util.Iterator;

@Configuration
public class Openapi3Config {

    @Value("${knife4j.description}")
    private String description;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ai Manus 接口文档")
                        .description(description)
                        .version("0.1")
                         .contact(new Contact()
                                .name("Mr.DotXin")
                                .url("https://github.com/MrDotXin")
                                .email("3099856626@qq.com"))
                        .license(new License().name("221042Y234 课程设计").url("https://github.com/MrDotXin")))
                .servers(Collections.singletonList(
                        new Server().url("/").description("默认服务器")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("更多技术支持")
                        .url("https://doc.mrdotxin.com"));
    }

    @Bean
    public GroupedOpenApi customGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("NexusMind")
                .packagesToScan("com.mrdotxin.nexusmind.controller")
                .pathsToMatch("/**")
                .build();
    }

    // 确保SpringDoc将Long类型映射为字符串
    static {
        SpringDocUtils.getConfig()
                .replaceWithSchema(Long.class, new io.swagger.v3.oas.models.media.StringSchema())
                .replaceWithSchema(long.class, new io.swagger.v3.oas.models.media.StringSchema());
    }
}
