package com.mrdotxin.nexusmind.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrdotxin.nexusmind.common.ErrorCode;
import com.mrdotxin.nexusmind.constant.CommonConstant;
import com.mrdotxin.nexusmind.constant.FileConstant;
import com.mrdotxin.nexusmind.constant.UserConstant;
import com.mrdotxin.nexusmind.exception.BusinessException;
import com.mrdotxin.nexusmind.exception.ThrowUtils;
import com.mrdotxin.nexusmind.model.dto.chat.DoChatRequest;
import com.mrdotxin.nexusmind.model.dto.golem.GolemQueryRequest;
import com.mrdotxin.nexusmind.model.entity.Golem;
import com.mrdotxin.nexusmind.model.entity.User;
import com.mrdotxin.nexusmind.model.vo.GolemVO;
import com.mrdotxin.nexusmind.model.vo.UserVO;
import com.mrdotxin.nexusmind.service.GolemService;
import com.mrdotxin.nexusmind.mapper.mysql.GolemMapper;
import com.mrdotxin.nexusmind.service.UserService;
import com.mrdotxin.nexusmind.upload.FileManager;
import com.mrdotxin.nexusmind.utils.SqlUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
* @author Administrator
* @description 针对表【golem(智能体)】的数据库操作Service实现
* @createDate 2025-06-19 21:25:43
*/
@Slf4j
@Service
public class GolemServiceImpl extends ServiceImpl<GolemMapper, Golem>
    implements GolemService{

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Override
    public QueryWrapper<Golem> getQueryWrapper(GolemQueryRequest request) {
        Long id = request.getId();
        String name = request.getName();
        String category = request.getCategory();
        Long userId = request.getUserId();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();


        QueryWrapper<Golem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.gt("id", 2);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);

        if (StrUtil.isNotBlank(sortField)) {
            SqlUtils.orderBy(queryWrapper, sortField, sortOrder);
        }
        return queryWrapper;
    }

    @Override
    public SearchRequest buildSearchRequest(Golem golem, String content, List<Long> extraRags, User user) {
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();

        FilterExpressionBuilder.Op filterOp;

        List<Long> rags = golem.getRags();
        if (ObjectUtil.isNotNull(extraRags)) {
            rags.addAll(extraRags);
        }

        if (!rags.isEmpty()) {
            filterOp = filterExpressionBuilder.in("appId", golem.getRags().toArray(Object[]::new));
        } else {
            filterOp = filterExpressionBuilder.eq("appId", -1);
        }

        return SearchRequest.builder().topK(15).query(content).similarityThreshold(0.65)
                .filterExpression(filterOp.build()).build();
    }

    @Override
    public GolemVO getGolemVO(Golem golem) {
        GolemVO golemVO = new GolemVO();
        BeanUtils.copyProperties(golem, golemVO);

        Long userId = golem.getUserId();
        if (ObjectUtil.isNotNull(userId) && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);

            golemVO.setUserVO(userVO);
        }

        return golemVO;
    }

    @Override
    public GolemVO getGolemVO(Long golemId) {
        Golem golem = this.getById(golemId);
        ThrowUtils.throwIf(ObjectUtil.isNull(golem), ErrorCode.OPERATION_ERROR, "智能体不存在!");

        return getGolemVO(golem);
    }

    @Override
    public String uploadGolemAvatar(String policy, Object source, User user, Golem golem) {
        ThrowUtils.throwIf(ObjectUtil.isNull(golem), ErrorCode.PARAMS_ERROR, "智能体不存在");
        ThrowUtils.throwIf(!user.getId().equals(golem.getUserId()), ErrorCode.NO_AUTH_ERROR);
        String avatar = fileManager.uploadFileRaw(policy, source, FileConstant.UPLOAD_FILE_PATH + "/golem", 10 * 1024 * 1024L);

        String oldPic = golem.getAvatar();
        boolean update = this.lambdaUpdate()
                .eq(Golem::getId, golem.getId())
                .set(Golem::getAvatar, avatar)
                .update();

        if (!update) {
            fileManager.removeObjectByUrlIfExists(avatar);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法上传该头像!");
        } else {
            if (oldPic.contains(FileConstant.UPLOAD_FILE_PATH)) {
                fileManager.removeObjectByUrlIfExists(oldPic);
            }

        }

        return avatar;
    }


}




