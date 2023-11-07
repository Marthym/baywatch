import { createRouter, createWebHashHistory, RouterOptions } from 'vue-router';
import { routes as adminRoutes } from '@/administration/router';
import { routes as configRoutes } from '@/configuration/router';
import { routes as teamsRoutes } from '@/teams/router';
import { routes as techwatchRoutes } from '@/techwatch/router';
import { Store, useStore } from 'vuex';
import { UserState } from '@/store/user/user';
import { refresh } from '@/security/services/AuthenticationService';
import { LOGOUT_MUTATION, UPDATE_MUTATION as USER_UPDATE_MUTATION } from '@/store/user/UserConstants';

const LoginPage = () => import('@/pages/LoginPage.vue');

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        ...adminRoutes,
        ...teamsRoutes,
        ...configRoutes,
        ...techwatchRoutes,
        { path: '/login', component: LoginPage, name: 'LoginPage' },
        { path: '/:catchAll(.*)*', redirect: '/news' },
    ],
} as RouterOptions);

function refreshSession(store: Store<UserState>): Promise<boolean> {
    const isAuthenticated = useStore<UserState>().state.user.isAuthenticated;
    if (isAuthenticated === undefined) {
        return new Promise(resolve => {
            refresh().subscribe({
                next: session => {
                    store.commit(USER_UPDATE_MUTATION, session.user);
                    resolve(true);
                },
                error: () => {
                    store.commit(LOGOUT_MUTATION);
                    resolve(false);
                },
            });
        });
    } else {
        return Promise.resolve(true);
    }
}

router.beforeEach(async to => {
    const isAuthenticated = await refreshSession(useStore<UserState>());
    if (to.matched.some(record => record.meta.requiresAuth)) {
        console.debug('to', to);
        console.debug('isAuthenticated', isAuthenticated);
        if (!isAuthenticated) {
            return { name: 'LoginPage', query: { redirect: to.path } };
        }
    }
});

export default router;