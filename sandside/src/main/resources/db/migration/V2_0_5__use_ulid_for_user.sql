ALTER TABLE USERS
    RENAME TO _USERS_old;
ALTER TABLE USERS_ROLES
    RENAME TO _USERS_ROLES_old;
ALTER TABLE FEEDS_USERS
    RENAME TO _FEEDS_USERS_old;
ALTER TABLE NEWS_USER_STATE
    RENAME TO _NEWS_USER_STATE_old;
ALTER TABLE TEAMS
    RENAME TO _TEAMS_old;
ALTER TABLE TEAMS_MEMBERS
    RENAME TO _TEAMS_MEMBERS_old;

create table USERS
(
    USER_ID         VARCHAR(28) not null primary key,
    USER_LOGIN      VARCHAR(50) not null unique,
    USER_EMAIL      VARCHAR(250) unique,
    USER_NAME       VARCHAR(250),
    USER_PASSWORD   VARCHAR(72) not null,
    USER_CREATED_AT DATETIME    not null default '2022-02-05 00:00:00',
    USER_LOGIN_AT   DATETIME,
    USER_LOGIN_IP   VARCHAR(45)
);

CREATE TABLE USERS_ROLES
(
    USRO_USER_ID VARCHAR(28) not null,
    USRO_ROLE    VARCHAR(36) not null,
    constraint PK_USRO
        primary key (USRO_USER_ID, USRO_ROLE),
    constraint FK_USRO_USERS
        foreign key (USRO_USER_ID) references USERS (USER_ID)
);

create table FEEDS_USERS
(
    FEUS_FEED_ID   VARCHAR(64) not null,
    FEUS_USER_ID   VARCHAR(28) not null,
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
    NURS_USER_ID VARCHAR(28) not null,
    NURS_STATE   INTEGER     not null,
    constraint PK_NEWS_USER_STATE
        primary key (NURS_NEWS_ID, NURS_USER_ID),
    constraint FK_NURS_NEWS_ID
        foreign key (NURS_NEWS_ID) references NEWS (NEWS_ID),
    constraint FK_NURS_USER_ID
        foreign key (NURS_USER_ID) references USERS (USER_ID)
);

create table TEAMS
(
    TEAM_ID         VARCHAR(28)  not null primary key,
    TEAM_NAME       VARCHAR(100) not null,
    TEAM_TOPIC      VARCHAR(500),
    TEAM_CREATED_BY VARCHAR(28)  not null,
    TEAM_CREATED_AT DATETIME
);

create table TEAMS_MEMBERS
(
    TEME_TEAM_ID     VARCHAR(28) not null,
    TEME_USER_ID     VARCHAR(28) not null,
    TEME_PENDING_FOR INTEGER(1),
    TEME_CREATED_BY  VARCHAR(28) not null,
    TEME_CREATED_AT  DATETIME,

    constraint PK_TEME
        primary key (TEME_TEAM_ID, TEME_USER_ID),
    constraint FK_TEME_TEAM_ID
        foreign key (TEME_TEAM_ID) references TEAMS (TEAM_ID)
);
