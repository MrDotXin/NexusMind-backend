package com.mrdotxin.nexusmind.config;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
import org.springframework.ai.model.chat.memory.repository.jdbc.autoconfigure.JdbcChatMemoryRepositoryAutoConfiguration;
import org.springframework.ai.model.chat.memory.repository.jdbc.autoconfigure.JdbcChatMemoryRepositoryProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.sql.init.OnDatabaseInitializationCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration(exclude = JdbcChatMemoryRepositoryAutoConfiguration.class)
public class JdbcChatMemoryConfig {
    @Resource
    @Qualifier("mysqlJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Resource
    @Qualifier("mysqlDataSource")
    private DataSource dataSource;

    @Resource
    @Qualifier("mysqlTransactionManager")
    private PlatformTransactionManager transactionManager;

    JdbcChatMemoryRepository getJdbcChatMemoryRepository() {
        JdbcChatMemoryRepositoryDialect dialect = JdbcChatMemoryRepositoryDialect.from(dataSource);
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dataSource(dataSource)
                .dialect(dialect)
                .transactionManager(transactionManager)
                .build();
    }
}
