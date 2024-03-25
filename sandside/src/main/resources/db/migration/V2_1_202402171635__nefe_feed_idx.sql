create index IDX_NEFE_FEED_ID on NEWS_FEEDS (NEFE_FEED_ID);

alter table FEEDS add column FEED_LAST_ETAG text;
