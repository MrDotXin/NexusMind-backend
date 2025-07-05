package com.mrdotxin.nexusmind.config.openapi.converter;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Iterator;

public class LongToStringConverter implements io.swagger.v3.core.converter.ModelConverter {
    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (type.getType() instanceof Class<?> classType) {
            if (Long.class.equals(classType) || long.class.equals(classType)) {
                Schema<?> schema = new Schema<>();
                schema.setType("string");
                schema.setFormat("string");
                return schema;
            }
        }
        return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
    }
}
