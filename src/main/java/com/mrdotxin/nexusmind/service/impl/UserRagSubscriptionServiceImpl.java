package com.mrdotxin.nexusmind.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.nexusmind.model.entity.UserRagSubscription;
import com.mrdotxin.nexusmind.service.UserRagSubscriptionService;
import com.mrdotxin.nexusmind.mapper.mysql.UserRagSubscriptionMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【userragsubscription(知识库信息表)】的数据库操作Service实现
* @createDate 2025-06-26 18:28:24
*/
@Service
public class UserRagSubscriptionServiceImpl extends ServiceImpl<UserRagSubscriptionMapper, UserRagSubscription>
    implements UserRagSubscriptionService{

    @Override
    public boolean removeUserRagSubscriptionByRagId(Long appId) {
        LambdaQueryWrapper<UserRagSubscription> ragSubscriptionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ragSubscriptionLambdaQueryWrapper.eq(UserRagSubscription::getRagId, appId);

        return this.remove(ragSubscriptionLambdaQueryWrapper);
    }
}




