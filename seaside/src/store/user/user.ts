import {ANONYMOUS, User} from "@/security/model/User";
import {UserRole} from "@/security/model/UserRole.enum";
import {LOGOUT, UPDATE} from "@/store/user/UserConstants";

export type UserState = {
    user: User;
    isAuthenticated: boolean | undefined;
}

const state = (): UserState => ({
    user: ANONYMOUS,
    isAuthenticated: undefined,
});

// getters
const getters = {
    hasRoleUser(st): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(st.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.USER);
    },
    hasRoleManager(st): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(st.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.MANAGER);
    },
    hasRoleAdmin(st): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(st.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.ADMIN);
    },
}

// actions
const actions = {}

// mutations
const mutations = {
    [LOGOUT](st: UserState): void {
        st.user = ANONYMOUS;
        st.isAuthenticated = false;
    },
    [UPDATE](st: UserState, payload: User): void {
        st.user = payload;
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(st.user.role);
        st.isAuthenticated = idx >= 0 && idx <= keys.indexOf(UserRole.USER);
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}