export const NAMESPACE = 'news';

export const FILTER_FEED = 'filterFeed';
export const REPLACE_TAGS = 'replaceTags';
export const RESET_FILTERS = 'resetFilters';
export const TOGGLE_POPULAR = 'togglePopular';
export const TOGGLE_UNREAD = 'toggleUnread';

export const NEWS_FILTER_FEED_MUTATION = `${NAMESPACE}/${FILTER_FEED}`;
export const NEWS_REPLACE_TAGS_MUTATION = `${NAMESPACE}/${REPLACE_TAGS}`;
export const NEWS_RESET_FILTERS_MUTATION = `${NAMESPACE}/${RESET_FILTERS}`;
export const NEWS_TOGGLE_POPULAR_MUTATION = `${NAMESPACE}/${TOGGLE_POPULAR}`;
export const NEWS_TOGGLE_UNREAD_MUTATION = `${NAMESPACE}/${TOGGLE_UNREAD}`;

export type FeedFilter = {
    id: string,
    label: string,
}

export type NewsStore = {
    tags: string[];
    unread: boolean,
    popular: boolean,
    feed?: FeedFilter,
}

export type NewsFilters = keyof NewsStore;
