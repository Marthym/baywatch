create table MAIL_TEMPLATES
(
    MATE_ID      VARCHAR(28)  not null primary key,
    MATE_NAME    VARCHAR(250) not null,
    MATE_LANG    VARCHAR(5)   not null,
    MATE_SUBJECT VARCHAR(250) not null,
    MATE_BODY    TEXT         not null
);

create unique index IDX_MAIL_TEMPLATES_NAME_LANG
    on MAIL_TEMPLATES (MATE_NAME, MATE_LANG);
