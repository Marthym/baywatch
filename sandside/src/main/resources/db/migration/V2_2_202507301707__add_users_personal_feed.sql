insert into FEEDS
select USERS.USER_ID,
       USER_NAME,
       'https://localhost/' || USER_ID,
       null,
       'Personal Baywatch pocket for you',
       null,
       'https://www.gravatar.com/avatar/' || USER_ID || '?s=96&d=retro'
from USERS;
