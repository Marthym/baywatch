export type SidenavState = {
    open: boolean;
}

const state = (): SidenavState => ({
    open: false,
});

// getters
const getters = {};

// actions
const actions = {};

// mutations
const mutations = {
    toggleSidenav(state: SidenavState): void {
        state.open = !state.open;
    },
};

export const sidenav = {
    namespaced: true,
    state,
    getters,
    actions,
    mutations,
};