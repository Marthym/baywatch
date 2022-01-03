import {createLogger, createStore} from "vuex";
import sidenav from '@/store/sidenav/sidenav'
import statistics from '@/store/statistics/statistics'

const debug = process.env.NODE_ENV !== 'production'

export default createStore({
    modules: {
        sidenav,
        statistics,
    },
    strict: debug,
    plugins: debug ? [createLogger()] : [],
});
