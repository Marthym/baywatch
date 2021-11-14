import Vuex from "vuex";
import Vue from "vue";
import { createLogger } from 'vuex'
import sidenav from '@/store/sidenav/sidenav'
import statistics from '@/store/statistics/statistics'

Vue.use(Vuex);

const debug = process.env.NODE_ENV !== 'production'

export default new Vuex.Store({
    modules: {
        sidenav,
        statistics,
    },
    strict: debug,
    plugins: debug ? [createLogger()] : [],
});