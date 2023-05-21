create table file
(
    uuid             varchar(50)          not null,
    file_owner       varchar(50)          not null,
    father_folder    varchar(50)          null comment '空表示最上级的文件夹',
    file_name        varchar(100)         not null,
    file_size        mediumtext           null,
    is_folder        tinyint(1) default 0 null comment '0 不是 1 是',
    file_updateTime  datetime             null,
    file_status      tinyint    default 1 not null comment '0 被删除 1 默认 2 被分享',
    file_delete_time date                 null,
    constraint table_name_uuid_uindex
        unique (uuid)
);

alter table file
    add primary key (uuid);

create table file_chunk
(
    uuid               varchar(50)  not null,
    chunk_number       int          null,
    chunk_size         mediumtext   null,
    current_chunk_size mediumtext   null,
    total_size         mediumtext   null,
    identifier         varchar(300) null,
    filename           varchar(300) null,
    relative_path      varchar(200) null,
    total_chunks       int          null,
    type               varchar(50)  null,
    constraint file_chunk_uuid_uindex
        unique (uuid)
);

alter table file_chunk
    add primary key (uuid);

create table file_share
(
    uuid        varchar(50) not null,
    file_uuid   varchar(50) null,
    share_code  varchar(6)  null,
    validity    int         null,
    create_time date        null,
    constraint file_share_share_code_uindex
        unique (share_code),
    constraint file_share_uuid_uindex
        unique (uuid)
);

alter table file_share
    add primary key (uuid);

create table user
(
    uuid      varchar(50)       not null,
    username  varchar(50)       not null,
    password  varchar(50)       null,
    type      int     default 1 null comment '0 系统管理员 1 普通用户',
    is_delete tinyint default 0 null comment '0 正常 1 被删除',
    constraint user_uuid_uindex
        unique (uuid)
);

alter table user
    add primary key (uuid);

