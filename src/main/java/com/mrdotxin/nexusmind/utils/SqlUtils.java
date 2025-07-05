package com.mrdotxin.nexusmind.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.constant.CommonConstant;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * SQL 工具
 *
*/
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }

    public static boolean validId(Long id) {
        return id != null && id > 0;
    }

    public static <T> boolean checkFieldExist(IService<T> service, String fieldName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(fieldName, value);
        return service.count(queryWrapper) > 0;
    }

    public static <T> T getFieldByFieldName(IService<T> service, String fieldName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(fieldName, value);
        return service.getOne(queryWrapper);
    }

    public static <T> List<T> listFieldByFieldName(IService<T> service, String fieldName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(fieldName, value);
        return service.list(queryWrapper);
    }


    public static List<Long> toLongList(String idList) {
        if (StrUtil.isNotBlank(idList)) {
            String ragList = idList.substring(0, idList.length() - 1);
            List<String> li = Arrays.stream(ragList.split(",")).map(String::trim).toList();
            if (!li.isEmpty()) {
                return li.stream().map(Long::valueOf).toList();
            }
        }

        return List.of();
    }

    public static <T> boolean setFieldPropertyByFieldName(IService<T> service, String fieldName, Object value, String targetFieldName, Object targetProperty) {
        UpdateWrapper<T> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq(fieldName, value);
        queryWrapper.set(targetFieldName, targetProperty);

        return service.update(queryWrapper);
    }

    public static <T> boolean setFieldSqlByFieldName(IService<T> service, String fieldName, Object value, String targetFieldName, String targetSQL) {
        UpdateWrapper<T> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq(fieldName, value);
        queryWrapper.setSql(targetSQL);

        return service.update(queryWrapper);
    }

    public static void orderBy(QueryWrapper<?> queryWrapper, String fields, String directions) {
        List<String> fieldList = ListUtils.singletonElementOrList(fields);
        List<String> directionList = ListUtils.singletonElementOrList(directions);

        ThrowUtils.throwIf(fieldList.size() != directionList.size(), ErrorCode.PARAMS_ERROR, "格式错误! 排序字段的数量必须对应!");

        Iterator<String> filed_iterator = fieldList.iterator();
        Iterator<String> direction_iterator = directionList.iterator();
        while (filed_iterator.hasNext() && direction_iterator.hasNext()) {
            String field = filed_iterator.next();
            String direction = direction_iterator.next();

            queryWrapper.orderBy(SqlUtils.validSortField(field), CommonConstant.SORT_ORDER_ASC.equals(direction), field);
        }
    }
}
