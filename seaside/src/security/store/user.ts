import { ANONYMOUS, User } from '@/security/model/User';
import { UserRole } from '@/security/model/UserRole.enum';
import {
    ADD_ROLE,
    CLOSE_CREATE_ACCOUNT,
    HAS_ROLE_ADMIN,
    HAS_ROLE_MANAGER,
    HAS_ROLE_USER,
    LOGOUT,
    OPEN_CREATE_ACCOUNT,
    UPDATE,
} from '@/security/store/UserConstants';
import { GetterTree } from 'vuex';

export type UserState = {
    user: User;
    isAuthenticated: boolean | undefined;
    isCreateAccountOpen: boolean;
}

const state = (): UserState => ({
    user: ANONYMOUS,
    isAuthenticated: undefined,
    isCreateAccountOpen: false,
});

// getters
const getters: GetterTree<UserState, UserState> = {
    [HAS_ROLE_USER](st: UserState): boolean {
        return hasRole(st.user, UserRole.USER);
    },
    [HAS_ROLE_MANAGER](st: UserState): (entity: string) => boolean {
        return (e) => hasRole(st.user, UserRole.MANAGER, e);
    },
    [HAS_ROLE_ADMIN](st: UserState): boolean {
        return hasRole(st.user, UserRole.ADMIN);
    },
};

// actions
const actions = {};

// mutations
const mutations = {
    [ADD_ROLE](st: UserState, payload: string): void {
        st.user.roles.push(payload);
    },
    [CLOSE_CREATE_ACCOUNT](st: UserState): void {
        st.isCreateAccountOpen = false;
    },
    [LOGOUT](st: UserState): void {
        st.user = ANONYMOUS;
        st.isAuthenticated = false;
    },
    [OPEN_CREATE_ACCOUNT](st: UserState): void {
        st.isCreateAccountOpen = true;
    },
    [UPDATE](st: UserState, payload: User): void {
        st.user = payload;
        st.isAuthenticated = hasRole(st.user, UserRole.USER);
    },
};

const hasRole = (user: User, expectedRole: UserRole, entity?: string): boolean => {
    if (!expectedRole) {
        throw new Error();
    }
    if (!user && user === null) {
        return false;
    }
    let hasRole = false;
    let userRoles: string[] = (entity)
        ? user.roles.map(r => r.split(':')[0])
        : user.roles;

    for (let role of Object.keys(UserRole)) {
        if (userRoles.indexOf(role) >= 0 ||
            ((entity) && userRoles.indexOf(`${role}:${entity}`) >= 0)) {
            hasRole = true;
        }
        if (role == expectedRole) {
            return hasRole;
        }
    }
    return false;
};

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations,
};