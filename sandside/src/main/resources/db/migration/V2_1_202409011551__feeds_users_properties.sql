create table FEEDS_USERS_PROPERTIES
(
    FUPR_FEED_ID        VARCHAR(64) not null,
    FUPR_USER_ID        VARCHAR(28) not null,
    FUPR_PROPERTY_NAME  TEXT        not null,
    FUPR_PROPERTY_VALUE TEXT        not null,

    constraint PK_FEEDS_USERS_PROPERTIES
        primary key (FUPR_FEED_ID, FUPR_USER_ID),
    constraint FK_FUPR_USER_ID
        foreign key (FUPR_USER_ID) references USERS (USER_ID),
    constraint FK_FUPR_FEED_ID
        foreign key (FUPR_FEED_ID) references FEEDS (FEED_ID)
);
