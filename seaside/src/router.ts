import { createRouter, createWebHashHistory, RouterOptions } from 'vue-router';
import { routes as adminRoutes } from '@/administration/router';
import { routes as configRoutes } from '@/configuration/router';
import { routes as teamsRoutes } from '@/teams/router';
import { routes as techwatchRoutes } from '@/techwatch/router';
import { useStore } from 'vuex';
import { UserState } from '@/store/user/user';

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

router.beforeEach(async (to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        const isAuthenticated = useStore<UserState>().state.user.isAuthenticated;
        if (isAuthenticated === undefined || isAuthenticated === true) {
            next();
        } else {
            next({ name: 'LoginPage' });
        }
    } else {
        next();
    }
});

export default router;