package com.mrdotxin.nexusmind.service;

import com.mrdotxin.nexusmind.model.dto.likes.DoLikeRequest;
import com.mrdotxin.nexusmind.model.entity.Likes;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【likes(用户点赞)】的数据库操作Service
* @createDate 2025-06-19 21:25:48
*/
public interface LikesService extends IService<Likes> {


    void like(DoLikeRequest doLikeRequest);


    void unlike(DoLikeRequest doLikeRequest);

    boolean hasLiked(DoLikeRequest doLikeRequest);

    /**
     * 针对某一物品的销毁，撤销掉所有喜欢, 目前还是一个昂贵的操作
     * @param targetId
     * @param target
     * @return
     */
    boolean removeLikedBy(Long targetId, String target);
}
