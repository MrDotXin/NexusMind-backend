package com.mrdotxin.nexusmind.mapper.mysql;

import com.mrdotxin.nexusmind.model.entity.ChatLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Administrator
* @description 针对表【chatlog(会话记录)】的数据库操作Mapper
* @createDate 2025-06-19 21:25:18
* @Entity com.mrdotxin.nexusmind.model.entity.ChatLog
*/
public interface ChatLogMapper extends BaseMapper<ChatLog> {


    @Select("SELECT MAX(sequenceId) FROM chatLog")
    Long getMaxSequenceId();

    @Select("SELECT id FROM chatlog")
    List<String> getIds();
}




