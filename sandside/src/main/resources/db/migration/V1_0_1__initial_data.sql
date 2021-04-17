INSERT INTO FEEDS
VALUES ('076b66c63e571578374ffa8e3197554cd185c6911658747e52373358e938b5cf', 'Test',
        'https://www.clubic.com/feed/news.rss', null),
       ('4ebc63c73907d3c10b7e672b0b6857f8c53ebc3b6a5eda8104dff5af1cbebf41', 'Reddit',
        'https://www.reddit.com/r/programming/top/.rss', null);

INSERT INTO USERS (USER_ID, USER_LOGIN, USER_EMAIL, USER_NAME, USER_PASSWORD, USER_ROLE)
VALUES ('fcombes', 'fcombes', 'marthym@gmail.com', 'Fred',
        '$2a$10$uuFG89ZGXcFMKkj3naDhK.e/rjE1SQhh6GvOoimTlyrRfnT5lHlEO', 'ADMIN');

INSERT INTO FEEDS_USERS (FEUS_USER_ID, FEUS_FEED_ID)
VALUES ('fcombes', '076b66c63e571578374ffa8e3197554cd185c6911658747e52373358e938b5cf'),
       ('fcombes', '4ebc63c73907d3c10b7e672b0b6857f8c53ebc3b6a5eda8104dff5af1cbebf41');
