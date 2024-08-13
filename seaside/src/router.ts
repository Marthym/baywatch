import { createRouter, createWebHashHistory, RouterOptions } from 'vue-router';
import { routes as adminRoutes } from '@/administration/router';
import { routes as configRoutes } from '@/configuration/router';
import { routes as teamsRoutes } from '@/teams/router';
import { routes as techwatchRoutes } from '@/techwatch/router';
import { requireAuthNavGuard, routes as securityRoutes } from '@/security/router';
import { useI18n } from 'vue-i18n';
import { loadLocaleMessages } from '@/i18n';

const router = createRouter({
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

router.beforeEach(async (to, from, next) => {
    const paramsLocale = to.params.locale;

    // use locale if paramsLocale is not in SUPPORT_LOCALES
    // if (!SUPPORT_LOCALES.includes(paramsLocale)) {
    //     return next(`/${locale}`)
    // }

    // load locale messages
    await loadLocaleMessages(to.name);

    return next();
});

export default router;