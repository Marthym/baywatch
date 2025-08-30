create table CONFIGURATION
(
    CONF_ID          VARCHAR(28)  not null primary key,
    CONF_NAME        VARCHAR(250) not null,
    CONF_VALUE       TEXT
);

create unique index IDX_CONFIGURATION_CONF_NAME
    on CONFIGURATION (CONF_NAME);
