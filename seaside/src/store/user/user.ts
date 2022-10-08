import {ANONYMOUS, User} from "@/security/model/User";
import {UserRole} from "@/security/model/UserRole.enum";
import {HAS_ROLE_ADMIN, HAS_ROLE_MANAGER, HAS_ROLE_USER, LOGOUT, UPDATE} from "@/store/user/UserConstants";

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
    [HAS_ROLE_USER](st): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(st.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.USER);
    },
    [HAS_ROLE_MANAGER](st): boolean {
        const keys = Object.keys(UserRole);
        const idx = keys.indexOf(st.user.role);
        return idx >= 0 && idx <= keys.indexOf(UserRole.MANAGER);
    },
    [HAS_ROLE_ADMIN](st): boolean {
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