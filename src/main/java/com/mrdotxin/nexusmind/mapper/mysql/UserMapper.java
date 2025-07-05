package com.mrdotxin.nexusmind.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrdotxin.nexusmind.model.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户数据库操作
 *
*/
public interface UserMapper extends BaseMapper<User> {
    @Select("select id from user where userRole = 'ROLE_ADMIN'")
    List<Long> selectAllAdminId();

    @Select("select id from user")
    List<Long> selectAllId();
}




