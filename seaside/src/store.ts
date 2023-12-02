import {createLogger, createStore} from "vuex";
import sidenav from '@/store/sidenav/sidenav'
import statistics from '@/techwatch/store/statistics/statistics'
import user from '@/store/user/user'
import news from '@/techwatch/store/news'

const debug = process.env.NODE_ENV !== 'production'

export default createStore({
    modules: {
        news,
        sidenav,
        statistics,
        user,
    },
    strict: debug,
    plugins: debug ? [createLogger()] : [],
});
