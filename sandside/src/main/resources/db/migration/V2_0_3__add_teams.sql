create table TEAMS
(
    TEAM_ID         VARCHAR(28) not null primary key,
    TEAM_NAME       VARCHAR(100),
    TEAM_TOPIC      VARCHAR(500),
    TEAM_CREATED_BY VARCHAR(64) not null,
    TEAM_CREATED_AT DATETIME
);

create table TEAMS_MEMBERS
(
    TEME_TEAM_ID     VARCHAR(28) not null,
    TEME_USER_ID     VARCHAR(64) not null,
    TEME_PENDING_FOR VARCHAR(8),
    TEME_CREATED_BY  VARCHAR(64) not null,
    TEME_CREATED_AT  DATETIME,

    constraint PK_TEME
        primary key (TEME_TEAM_ID, TEME_USER_ID),
    constraint FK_TEME_TEAM_ID
        foreign key (TEME_TEAM_ID) references TEAMS (TEAM_ID)
);
