-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userEmail    varchar(256)                           null comment '邮箱',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'ROLE_USER'       not null comment '用户角色：USER/ADMIN/BAN',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

create table if not exists golem
(
    id           bigint auto_increment comment 'id' primary key,
    systemPrompt TEXT comment '智能体初始提示词',
    name         VARCHAR(128) comment '智能体名称',
    description  TEXT comment '智能体介绍',
    tags         TEXT comment '智能体标签',
    category     VARCHAR(64) comment '智能体分类',
    avatar       VARCHAR(512) comment '头像',
    likes        BIGINT                             NOT NULL DEFAULT 0 comment '点赞数',
    createTime   DATETIME default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   DATETIME default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     TINYINT  default 0                 not null comment '逻辑删除'
) comment '智能体' collate = utf8mb4_unicode_ci;


create table if not exists chatSession
(
    id         BIGINT AUTO_INCREMENT comment 'id' primary key,
    userId     BIGINT                             NOT NULL comment '关联的用户Id',
    golemId    BIGINT                             NOT NULL comment '对于智能体Id',
    title      VARCHAR(256) comment '这个会话的名称',
    createTime DATETIME default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime DATETIME default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   TINYINT  default 0                 not null comment '逻辑删除'
) comment '智能体会话' collate = utf8mb4_unicode_ci;


create table if not exists chatLog
(
    id          BIGINT AUTO_INCREMENT comment 'id' primary key,
    sessionId   BIGINT                                     NOT NULL comment '会话Id',
    messageType ENUM ('user', 'assistant','system','tool') not null comment '消息类型',
    text        TEXT                                       NOT NULL comment '内容',
    createTime  DATETIME default CURRENT_TIMESTAMP         not null comment '创建时间',
    updateTime  DATETIME default CURRENT_TIMESTAMP         not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '会话记录' collate = utf8mb4_unicode_ci;