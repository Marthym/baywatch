import {
    FeedFilter,
    FILTER_FEED,
    NewsFilters,
    NewsStore,
    REPLACE_TAGS,
    RESET_FILTERS,
    TOGGLE_KEEP,
    TOGGLE_POPULAR,
    TOGGLE_UNREAD
} from "@/common/model/store/NewsStore.type";

const state = (): NewsStore => ({
    tags: [],
    unread: true,
    popular: false,
    keep: false,
});

// getters
const getters = {}

// actions
const actions = {}

// mutations
const mutations = {
    [FILTER_FEED](st: NewsStore, payload: FeedFilter): void {
        st.feed = payload;
    },
    [REPLACE_TAGS](st: NewsStore, payload: string[]): void {
        st.tags.splice(0);
        payload.forEach(e => st.tags.push(e));
    },
    [RESET_FILTERS](st: NewsStore, payload?: NewsFilters): void {
        if (payload) {
            switch (payload) {
                case "popular":
                    st.popular = false;
                    break;
                case "unread":
                    st.unread = true;
                    break;
                case "keep":
                    st.keep = false;
                    break;
                case "feed":
                    delete st.feed;
                    break;
                case "tags":
                    st.tags.splice(0);
                    break
                default:
            }
        } else {
            st.unread = true;
            st.popular = false;
            st.keep = false;
            delete st.feed;
            st.tags.splice(0);
        }
    },
    [TOGGLE_POPULAR](st: NewsStore): void {
        st.popular = !st.popular;
    },
    [TOGGLE_UNREAD](st: NewsStore): void {
        st.unread = !st.unread;
    },
    [TOGGLE_KEEP](st: NewsStore): void {
        st.keep = !st.keep;
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}