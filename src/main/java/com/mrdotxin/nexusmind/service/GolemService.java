package com.mrdotxin.nexusmind.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrdotxin.nexusmind.model.dto.chat.DoChatRequest;
import com.mrdotxin.nexusmind.model.dto.golem.GolemQueryRequest;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.model.vo.GolemVO;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;

/**
* @author Administrator
* @description 针对表【golem(智能体)】的数据库操作Service
* @createDate 2025-06-19 21:25:43
*/
public interface GolemService extends IService<Golem> {

    QueryWrapper<Golem> getQueryWrapper(GolemQueryRequest golemQueryRequest);

    /**
     * 建立检索配置
     */
    SearchRequest buildSearchRequest(Golem golem, String content, List<Long> extraRags, User user);


    GolemVO getGolemVO(Golem golem);

    GolemVO getGolemVO(Long golemId);

    String uploadGolemAvatar(String policy, Object source, User user, Golem golem);

}
