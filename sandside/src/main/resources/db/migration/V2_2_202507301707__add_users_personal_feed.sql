alter table FEEDS add column FEED_VISIBLE boolean not null default true;

insert into FEEDS
select USER_ID,
       USER_NAME,
       'https://localhost/' || USER_ID,
       null,
       'Personal Baywatch pocket for you',
       null,
       'https://www.gravatar.com/avatar/' || USER_ID || '?s=96&d=retro',
       false
from USERS;

insert into FEEDS_USERS
select USER_ID,
       USER_ID
from USERS;

