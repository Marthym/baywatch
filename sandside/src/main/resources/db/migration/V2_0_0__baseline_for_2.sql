create table FEEDS
(
    FEED_ID         VARCHAR(64)   not null primary key,
    FEED_NAME       VARCHAR(60),
    FEED_URL        VARCHAR(2083) not null,
    FEED_LAST_WATCH DATETIME
);

create table NEWS
(
    NEWS_ID          VARCHAR(64)   not null
        primary key,
    NEWS_TITLE       VARCHAR(250)  not null,
    NEWS_IMG_LINK    VARCHAR(2083),
    NEWS_DESCRIPTION TEXT,
    NEWS_PUBLICATION DATETIME      not null,
    NEWS_LINK        VARCHAR(2083) not null
);

create table NEWS_FEEDS
(
    NEFE_NEWS_ID VARCHAR(64) not null,
    NEFE_FEED_ID VARCHAR(64) not null,
    constraint PK_NEWS_FEEDS
        primary key (NEFE_NEWS_ID, NEFE_FEED_ID),
    constraint FK_NEFE_FEED_ID
        foreign key (NEFE_FEED_ID) references FEEDS (FEED_ID),
    constraint FK_NEFE_NEWS_ID
        foreign key (NEFE_NEWS_ID) references NEWS (NEWS_ID)
);

create table USERS
(
    USER_ID         VARCHAR(64)                            not null
        primary key,
    USER_LOGIN      VARCHAR(50)                            not null
        unique,
    USER_EMAIL      VARCHAR(250),
    USER_NAME       VARCHAR(250),
    USER_PASSWORD   VARCHAR(72)                            not null,
    USER_ROLE       VARCHAR(7)                             not null,
    USER_CREATED_AT DATETIME default '2022-02-05 00:00:00' not null,
    USER_LOGIN_AT   DATETIME,
    USER_LOGIN_IP   VARCHAR(45)
);

create table FEEDS_USERS
(
    FEUS_FEED_ID   VARCHAR(64) not null,
    FEUS_USER_ID   VARCHAR(64) not null,
    FEUS_FEED_NAME VARCHAR(60),
    FEUS_TAGS      TEXT,
    constraint PK_FEEDS_USER
        primary key (FEUS_FEED_ID, FEUS_USER_ID),
    constraint FK_FEUS_FEED_ID
        foreign key (FEUS_FEED_ID) references FEEDS (FEED_ID),
    constraint FK_FEUS_USER_ID
        foreign key (FEUS_USER_ID) references USERS (USER_ID)
);

create table NEWS_USER_STATE
(
    NURS_NEWS_ID VARCHAR(64) not null,
    NURS_USER_ID VARCHAR(64) not null,
    NURS_STATE   INTEGER     not null,
    constraint PK_NEWS_USER_STATE
        primary key (NURS_NEWS_ID, NURS_USER_ID),
    constraint FK_NURS_NEWS_ID
        foreign key (NURS_NEWS_ID) references NEWS (NEWS_ID),
    constraint FK_NURS_USER_ID
        foreign key (NURS_USER_ID) references USERS (USER_ID)
);

INSERT INTO USERS (USER_ID, USER_LOGIN, USER_EMAIL, USER_NAME, USER_PASSWORD, USER_ROLE)
VALUES ('fcombes', 'fcombes', 'marthym@gmail.com', 'Fred',
        '$2a$10$uuFG89ZGXcFMKkj3naDhK.e/rjE1SQhh6GvOoimTlyrRfnT5lHlEO', 'ADMIN');
