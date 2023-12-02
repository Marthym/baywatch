import {Statistics} from "@/administration/model/Statistics.type";
import {
    DECREMENT_UNREAD,
    FILTER,
    INCREMENT_UNREAD,
    RESET_UPDATED,
    UPDATE
} from "@/techwatch/store/statistics/StatisticsConstants";

export type StatisticsState = {
    unread: number;
    unread_filtered: number;
    updated: number;
}

const state = (): StatisticsState => ({
    unread: 0,
    unread_filtered: 0,
    updated: 0,
});

// getters
const getters = {}

// actions
const actions = {}

// mutations
const mutations = {
    [DECREMENT_UNREAD](st: StatisticsState): void {
        --st.unread;
        --st.unread_filtered;
    },
    [INCREMENT_UNREAD](st: StatisticsState): void {
        ++st.unread;
        ++st.unread_filtered;
    },
    [UPDATE](st: StatisticsState, payload: Statistics): void {
        st.updated = 1;
    },
    [RESET_UPDATED](st: StatisticsState): void {
        st.updated = 0;
    },
    [FILTER](st: StatisticsState, payload: number): void {
        st.unread_filtered = payload;
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}