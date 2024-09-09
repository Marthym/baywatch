create table FEEDS_USERS_PROPERTIES
(
    FUPR_USER_ID        VARCHAR(28) not null,
    FUPR_FEED_ID        VARCHAR(64) not null,
    FUPR_PROPERTY_NAME  TEXT        not null,
    FUPR_PROPERTY_VALUE TEXT        not null,

    constraint FK_FUPR_USER_ID
        foreign key (FUPR_USER_ID) references USERS (USER_ID),
    constraint FK_FUPR_FEED_ID
        foreign key (FUPR_FEED_ID) references FEEDS (FEED_ID)
);

WITH RECURSIVE split(fid, uid, label, str) AS (SELECT FEUS_FEED_ID, FEUS_USER_ID, '', FEEDS_USERS.FEUS_TAGS || ','
                                               FROM FEEDS_USERS
                                               UNION ALL
                                               SELECT fid,
                                                      uid,
                                                      substr(str, 0, instr(str, ',')),
                                                      substr(str, instr(str, ',') + 1)
                                               FROM split
                                               WHERE str != '')
insert
into FEEDS_USERS_PROPERTIES
SELECT uid, fid, 'TAG', label
FROM split
WHERE label != '';

CREATE INDEX IDX_FUPR_USER_ID_FEED_ID ON FEEDS_USERS_PROPERTIES (FUPR_USER_ID, FUPR_FEED_ID);

alter table FEEDS_USERS
    drop COLUMN FEUS_TAGS;
alter table FEEDS_USERS
    drop COLUMN FEUS_FEED_NAME;
