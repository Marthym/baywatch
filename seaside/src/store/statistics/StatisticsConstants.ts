export const NAMESPACE = 'statistics';
export const DECREMENT_UNREAD = 'decrementUnread';
export const INCREMENT_UNREAD = 'incrementUnread';
export const UPDATE = 'update';
export const RESET_UPDATED = 'resetUpdated';
export const RELOAD = 'reload';

export const DECREMENT_UNREAD_MUTATION = `${NAMESPACE}/${DECREMENT_UNREAD}`;
export const INCREMENT_UNREAD_MUTATION = `${NAMESPACE}/${INCREMENT_UNREAD}`;
export const UPDATE_MUTATION = `${NAMESPACE}/${UPDATE}`;
export const RESET_UPDATED_MUTATION = `${NAMESPACE}/${RESET_UPDATED}`;
export const RELOAD_ACTION = `${NAMESPACE}/${RELOAD}`;