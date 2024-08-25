import { createLogger, createStore } from 'vuex';
import { sidenav, SidenavState } from '@/store/sidenav/sidenav';
import { statistics, StatisticsState } from '@/techwatch/store/statistics/statistics';
import { user, UserState } from '@/security/store/user';
import { news } from '@/techwatch/store/news';
import { NewsStore } from '@/common/model/store/NewsStore.type';


type State = {
    news: NewsStore
    sidenav: SidenavState
    statistics: StatisticsState
    user: UserState
}

const debug = process.env.NODE_ENV !== 'production';

export const store = createStore<State>({
    modules: {
        news,
        sidenav,
        statistics,
        user,
    },
    strict: debug,
    plugins: debug ? [createLogger()] : [],
});
