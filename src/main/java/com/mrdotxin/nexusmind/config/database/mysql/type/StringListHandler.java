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
import java.util.Map;

public class StringListHandler implements TypeHandler<List<String>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        if (ObjectUtil.isNotNull(parameter)){
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            try {
                String json = mapper.writeValueAsString(parameter);
                ps.setString(i, json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        String info = rs.getString(columnName);
        if (StrUtil.isBlank(info)) {
            return List.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            List<String> parameter = mapper.readValue(info, new TypeReference<List<String>>() {});
            return ObjectUtil.isNotNull(parameter) ? parameter : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String info = rs.getString(columnIndex);
        if (StrUtil.isBlank(info)) {
            return List.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            List<String> parameter = mapper.readValue(info, new TypeReference<List<String>>() {});
            return ObjectUtil.isNotNull(parameter) ? parameter : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String info = cs.getString(columnIndex);
        if (StrUtil.isBlank(info)) {
            return List.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            List<String> parameter = mapper.readValue(info, new TypeReference<List<String>>() {});
            return ObjectUtil.isNotNull(parameter) ? parameter : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
