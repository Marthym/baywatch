import {Statistics} from "@/services/model/Statistics";

export type StatisticsState = {
    news: number;
    unread: number;
    feeds: number;
    users: number;
}

const state = (): StatisticsState => ({
    news: 0,
    unread: 0,
    feeds: 0,
    users: 0,
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
        Object.assign(state, payload);
    }
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}