package com.mrdotxin.nexusmind.service;

import com.mrdotxin.nexusmind.model.entity.RagInfo;
import com.mrdotxin.nexusmind.model.entity.UserRagSubscription;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【userragsubscription(知识库信息表)】的数据库操作Service
* @createDate 2025-06-26 18:28:24
*/
public interface UserRagSubscriptionService extends IService<UserRagSubscription> {


    /**
     * 针对某一物品的销毁，撤销掉所有喜欢, 目前还是一个昂贵的操作
     * @param appId
     * @return
     */
    boolean removeUserRagSubscriptionByRagId(Long appId);
}
