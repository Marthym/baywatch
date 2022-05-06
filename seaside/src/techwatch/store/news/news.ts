import {
    ADD_TAG,
    EMPTY_FILTERS,
    REMOVE_TAG,
    REPLACE_TAGS,
    TOGGLE_POPULAR,
    TOGGLE_UNREAD
} from "@/techwatch/store/news/NewsStoreConstants";

export type NewsStore = {
    tags: string[];
    unread: boolean,
    popular: boolean
}

const state = (): NewsStore => ({
    tags: [],
    unread: true,
    popular: false
});

// getters
const getters = {}

// actions
const actions = {}

// mutations
const mutations = {
    [ADD_TAG](st: NewsStore, payload: string): void {
        st.tags.push(payload);
    },
    [REMOVE_TAG](st: NewsStore, payload: string): void {
        const idx = st.tags.indexOf(payload);
        st.tags.splice(idx, 1);
    },
    [REPLACE_TAGS](st: NewsStore, payload: string[]): void {
        st.tags.splice(0);
        payload.forEach(e => st.tags.push(e));
    },
    [EMPTY_FILTERS](st: NewsStore): void {
        st.tags.splice(0);
    },
    [TOGGLE_UNREAD](st: NewsStore): void {
        st.unread = !st.unread;
    },
    [TOGGLE_POPULAR](st: NewsStore): void {
        st.popular = !st.popular;
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}