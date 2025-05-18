alter table FEEDS_ERRORS drop column FEER_LAST_LABEL;
alter table FEEDS_ERRORS drop column FEER_LAST_STATUS;
alter table FEEDS_ERRORS add column FEER_CODE VARCHAR(20) not null default 'UNKNOWN';
