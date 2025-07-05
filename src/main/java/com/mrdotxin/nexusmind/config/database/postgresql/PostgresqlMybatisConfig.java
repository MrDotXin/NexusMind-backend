package com.mrdotxin.nexusmind.config.database.postgresql;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
@MapperScan(
        basePackages = "com.mrdotxin.nexusmind.mapper.postgresql",
        sqlSessionFactoryRef = "postgresqlSqlSessionFactory"
)
public class PostgresqlMybatisConfig {

    @Resource
    private PostgresqlDatasourceConfig datasourceConfig;

    @Bean(name = "postgresqlDataSource")
    public DataSource postgresqlDataSource() {
        return DataSourceBuilder.create()
                .url(datasourceConfig.getUrl())
                .driverClassName(datasourceConfig.getDriverClassName())
                .username(datasourceConfig.getUsername())
                .password(datasourceConfig.getPassword())
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "postgresqlSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("postgresqlDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);

        if (dataSource instanceof HikariDataSource hikariDataSource) {
            log.info("Postgresql JDBC URL: {}", hikariDataSource.getJdbcUrl());
            log.info("Driver Class: {}", hikariDataSource.getDriverClassName());
        }

        return bean.getObject();
    }

    @Bean(name = "postgresqlSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "postgresqlTransactionManager")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "postgresqlJdbcTemplate")
    public JdbcTemplate postgresqlJdbcTemplate(
            @Qualifier("postgresqlDataSource") DataSource postgresqlDataSource) {
        return new JdbcTemplate(postgresqlDataSource);
    }

    @Bean(name = "postgresqlTransactionTemplate")
    public TransactionTemplate postgresqlTransactionTemplate(@Qualifier("postgresqlTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
