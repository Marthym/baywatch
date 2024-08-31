create table USERS_SETTINGS
(
    USSE_USER_ID         VARCHAR(64) not null primary key,
    USSE_PREFERRED_LOCALE TEXT,

    constraint FK_USSE_USER_ID
        foreign key (USSE_USER_ID) references USERS (USER_ID)
);
