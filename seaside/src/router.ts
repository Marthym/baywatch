import { createRouter, createWebHashHistory, RouterOptions } from 'vue-router';
import { routes as adminRoutes } from '@/administration/router';
import { routes as configRoutes } from '@/configuration/router';
import { routes as teamsRoutes } from '@/teams/router';
import { routes as techwatchRoutes } from '@/techwatch/router';
import { requireAuthNavGuard, routes as securityRoutes } from '@/security/router';

export const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        ...adminRoutes,
        ...teamsRoutes,
        ...configRoutes,
        ...techwatchRoutes,
        ...securityRoutes,
        { path: '/:catchAll(.*)*', redirect: '/news' },
    ],
} as RouterOptions);

router.beforeEach(requireAuthNavGuard);
