export const SIDENAV_MUTATION_TOGGLE = 'sidenav/toggleSidenav';

export type SidenavState = {
    open: boolean;
}

const state = (): SidenavState => ({
    open: true
});

// getters
const getters = {}

// actions
const actions = {}

// mutations
const mutations = {
    toggleSidenav(state: SidenavState): void {
        state.open = !state.open;
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}