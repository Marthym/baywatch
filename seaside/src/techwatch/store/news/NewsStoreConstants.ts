export const NAMESPACE = 'news';

export const ADD_TAG = 'addTag';
export const REMOVE_TAG = 'removeTag';
export const REPLACE_TAGS = 'replaceTags';
export const EMPTY_FILTERS = 'emptyFilters';
export const TOGGLE_UNREAD = 'toggleUnread';
export const TOGGLE_POPULAR = 'togglePopular';

export const NEWS_ADD_TAG_MUTATION = `${NAMESPACE}/${ADD_TAG}`;
export const NEWS_REMOVE_TAG_MUTATION = `${NAMESPACE}/${REMOVE_TAG}`;
export const NEWS_REPLACE_TAGS_MUTATION = `${NAMESPACE}/${REPLACE_TAGS}`;
export const NEWS_EMPTY_FILTERS_MUTATION = `${NAMESPACE}/${EMPTY_FILTERS}`;
export const NEWS_TOGGLE_UNREAD_MUTATION = `${NAMESPACE}/${TOGGLE_UNREAD}`;
export const NEWS_TOGGLE_POPULAR_MUTATION = `${NAMESPACE}/${TOGGLE_POPULAR}`;
