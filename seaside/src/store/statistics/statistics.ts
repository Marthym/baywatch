import {Statistics} from "@/services/model/Statistics";

export type StatisticsState = {
    news: number;
    unread: number;
    feeds: number;
    users: number;
    updated: number;
}

const state = (): StatisticsState => ({
    news: 0,
    unread: 0,
    feeds: 0,
    users: 0,
    updated: 0,
});

// getters
const getters = {}

// actions
const actions = {}

// mutations
const mutations = {
    decrementUnread(state: StatisticsState): void {
        --state.unread;
    },
    incrementUnread(state: StatisticsState): void {
        ++state.unread;
    },
    update(state: StatisticsState, payload: Statistics): void {
        const unread = state.unread;
        Object.assign(state, payload);
        if (unread != 0 && state.unread > unread) {
            state.updated += state.unread - unread;
        }
    },
    resetUpdated(state: StatisticsState): void {
        state.updated = 0;
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}