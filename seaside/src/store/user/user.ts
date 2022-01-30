import {ANONYMOUS, User} from "@/services/model/User";
import {UserRole} from "@/services/model/UserRole.enum";
import {LOGOUT, UPDATE} from "@/store/user/UserConstants";

export type UserState = {
    user: User;
    isAuthenticated: boolean;
}

const state = (): UserState => ({
    user: ANONYMOUS,
    isAuthenticated: false,
});

// getters
const getters = {
    hasRoleUser(state): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(state.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.USER);
    },
    hasRoleManager(state): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(state.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.MANAGER);
    },
    hasRoleAdmin(state): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(state.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.ADMIN);
    },
}

// actions
const actions = {}

// mutations
const mutations = {
    [LOGOUT](state: UserState): void {
        state.user = ANONYMOUS;
        state.isAuthenticated = false;
    },
    [UPDATE](state: UserState, payload: User): void {
        state.user = payload;
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(state.user.role);
        state.isAuthenticated = idx >= 0 && idx <= keys.indexOf(UserRole.USER);
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}