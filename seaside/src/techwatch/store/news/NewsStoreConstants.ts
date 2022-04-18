export const NAMESPACE = 'news';

export const ADD_TAG = 'addTag';
export const REMOVE_TAG = 'removeTag';
export const REPLACE_TAGS = 'replaceTags';
export const EMPTY_FILTERS = 'emptyFilters';

export const NEWS_ADD_TAG_MUTATION = `${NAMESPACE}/${ADD_TAG}`;
export const NEWS_REMOVE_TAG_MUTATION = `${NAMESPACE}/${REMOVE_TAG}`;
export const NEWS_REPLACE_TAGS_MUTATION = `${NAMESPACE}/${REPLACE_TAGS}`;
export const NEWS_EMPTY_FILTERS_MUTATION = `${NAMESPACE}/${EMPTY_FILTERS}`;
