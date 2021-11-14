import Vuex from "vuex";
import Vue from "vue";
import { createLogger } from 'vuex'
import sidenav from '@/store/sidenav/sidenav'

Vue.use(Vuex);

const debug = process.env.NODE_ENV !== 'production'

export default new Vuex.Store({
    modules: {
        sidenav,
    },
    strict: debug,
    plugins: debug ? [createLogger()] : [],
});