package com.mrdotxin.nexusmind.config.database.mysql.type;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LongListHandler implements TypeHandler<List<Long>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        if (ObjectUtil.isNotNull(parameter)){
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            try {
                String info = mapper.writeValueAsString(parameter);
                ps.setString(i, info);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Long> getResult(ResultSet rs, String columnName) throws SQLException {
        String info = rs.getString(columnName);
        if (StrUtil.isBlank(info)) {
            return List.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            List<Long> parameter = mapper.readValue(info, new TypeReference<List<Long>>() {});
            return ObjectUtil.isNotNull(parameter) ? parameter : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String info = rs.getString(columnIndex);
        if (StrUtil.isBlank(info)) {
            return List.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
                List<Long> parameter = mapper.readValue(info, new TypeReference<List<Long>>() {});
            return ObjectUtil.isNotNull(parameter) ? parameter : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String info = cs.getString(columnIndex);
        if (StrUtil.isBlank(info)) {
            return List.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            List<Long> parameter = mapper.readValue(info, new TypeReference<List<Long>>() {});
            return ObjectUtil.isNotNull(parameter) ? parameter : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
