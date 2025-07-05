package com.mrdotxin.nexusmind.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.component.MybatisServiceSelector;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.likes.DoLikeRequest;
import com.mrdotxin.nexusmind.model.entity.Likes;
import com.mrdotxin.nexusmind.service.LikesService;
import com.mrdotxin.nexusmind.mapper.mysql.LikesMapper;
import com.mrdotxin.nexusmind.utils.SqlUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
* @author Administrator
*/
@Service
public class LikesServiceImpl extends ServiceImpl<LikesMapper, Likes> implements LikesService{

    @Lazy
    @Resource
    private MybatisServiceSelector mybatisServiceSelector;

    @Resource
    @Qualifier("mysqlTransactionTemplate")
    private TransactionTemplate transactionTemplate;

    @Override
    public void like(DoLikeRequest doLikeRequest) {
        Likes likes = Likes.buildLikes(doLikeRequest.getUserId(), doLikeRequest.getTarget(), doLikeRequest.getTargetId());
        ThrowUtils.throwIf(hasLiked(doLikeRequest), ErrorCode.PARAMS_ERROR, "不要重复点赞");

        transactionTemplate.executeWithoutResult( transactionStatus -> {
            boolean save = this.save(likes) && SqlUtils.setFieldSqlByFieldName(
                    mybatisServiceSelector.from(doLikeRequest.getTarget()),
                    "id",
                    doLikeRequest.getTargetId(),
                    "likes",
                    "likes = likes + 1"
            );

            ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "点赞失败!");
        });
    }

    @Override
    public void unlike(DoLikeRequest doLikeRequest) {
        ThrowUtils.throwIf(!hasLiked(doLikeRequest), ErrorCode.PARAMS_ERROR, "错误! 你没有赞过该目标");
        Likes likes = Likes.buildLikes(doLikeRequest.getUserId(), doLikeRequest.getTarget(), doLikeRequest.getTargetId());

        QueryWrapper<Likes> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", doLikeRequest.getUserId());
        queryWrapper.eq("target", doLikeRequest.getTarget());
        queryWrapper.eq("targetId", doLikeRequest.getTargetId());

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            boolean remove = this.remove(queryWrapper) && SqlUtils.setFieldSqlByFieldName(
                    mybatisServiceSelector.from(doLikeRequest.getTarget()),
                    "id",
                    doLikeRequest.getTargetId(),
                    "likes",
                    "likes = likes - 1"
            );
            ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR, "取消点赞失败!");
        });
    }

    @Override
    public boolean hasLiked(DoLikeRequest doLikeRequest) {
        Long count = this.fromDoLikeRequest(doLikeRequest).count();

        return count > 0;
    }

    @Override
    public boolean removeLikedBy(Long targetId, String target) {
        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getTargetId, targetId);
        likesLambdaQueryWrapper.eq(Likes::getTarget, target);

        return this.remove(likesLambdaQueryWrapper);
    }

    private LambdaQueryChainWrapper<Likes> fromDoLikeRequest(DoLikeRequest doLikeRequest) {
        return this.lambdaQuery()
                .eq(Likes::getUserId, doLikeRequest.getUserId())
                .eq(Likes::getTarget, doLikeRequest.getTarget())
                .eq(Likes::getTargetId, doLikeRequest.getTargetId());
    }

}




