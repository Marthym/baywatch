export type SidenavState = {
    open: boolean;
}

const state = (): SidenavState => ({
    open: false
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