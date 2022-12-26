create table TEAMS
(
    TEAM_ID         VARCHAR(28) not null primary key,
    TEAM_NAME       VARCHAR(255),
    TEAM_CREATED_BY VARCHAR(64) not null,
    TEAM_CREATED_AT DATETIME
);

create table TEAMS_MEMBERS
(
    TEME_TEAM_ID    VARCHAR(28) not null primary key,
    TEME_USER_ID    VARCHAR(64) not null,
    TEME_CREATED_BY VARCHAR(64) not null,
    TEME_CREATED_AT DATETIME
);
