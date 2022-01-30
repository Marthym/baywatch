import {Statistics} from "@/services/model/Statistics";
import statsService from "@/services/StatisticsService";
import {
    DECREMENT_UNREAD,
    INCREMENT_UNREAD,
    RELOAD,
    RESET_UPDATED,
    UPDATE
} from "@/store/statistics/StatisticsConstants";
import {ActionContext} from "vuex";

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
const actions = {
    [RELOAD]({commit}: ActionContext<StatisticsState, StatisticsState>) {
        statsService.get().subscribe(s => commit(UPDATE, s));
    }
}

// mutations
const mutations = {
    [DECREMENT_UNREAD](state: StatisticsState): void {
        --state.unread;
    },
    [INCREMENT_UNREAD](state: StatisticsState): void {
        ++state.unread;
    },
    [UPDATE](state: StatisticsState, payload: Statistics): void {
        const unread = state.unread;
        Object.assign(state, payload);
        if (unread != 0 && state.unread > unread) {
            state.updated += state.unread - unread;
        }
    },
    [RESET_UPDATED](state: StatisticsState): void {
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