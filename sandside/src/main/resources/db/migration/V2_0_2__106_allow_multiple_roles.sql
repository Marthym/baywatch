CREATE TABLE USERS_ROLES
(
    USRO_USER_ID VARCHAR(64) not null,
    USRO_ROLE    VARCHAR(36) not null,
    constraint PK_USRO
        primary key (USRO_USER_ID, USRO_ROLE),
    constraint FK_USRO_USERS
        foreign key (USRO_USER_ID) references USERS (USER_ID)
);

insert into USERS_ROLES (USRO_USER_ID, USRO_ROLE)
select USER_ID, USER_ROLE
from USERS;

alter table USERS
    drop column USER_ROLE;
