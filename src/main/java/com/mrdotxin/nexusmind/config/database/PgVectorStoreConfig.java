package com.mrdotxin.nexusmind.config.database;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableAutoConfiguration
public class PgVectorStoreConfig {

    @Bean(name = "pgVectorStore")
    public VectorStore vectorStore(
            @Qualifier("postgresqlJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Qualifier("dashscopeEmbeddingModel") EmbeddingModel embeddingModel
    ) {

        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1024)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .maxDocumentBatchSize(50000)
                .schemaName("public")
                .vectorTableName("nexusVector")
                .removeExistingVectorStoreTable(false)
                .build();
    }
}
