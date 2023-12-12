create table NOTIFICATIONS
(
    NOTI_ID         VARCHAR(64) NOT NULL PRIMARY KEY,
    NOTI_USER_ID    VARCHAR(64) NOT NULL,
    NOTI_EVENT_TYPE VARCHAR(20) NOT NULL,
    NOTI_DATA       TEXT,
    NOTI_CREATED_AT DATETIME,

    constraint FK_NOTI_USER_ID
        foreign key (NOTI_USER_ID) references USERS (USER_ID)
);