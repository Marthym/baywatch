import { RouteRecordRaw } from 'vue-router';

const AdministrationPage = () => import('@/administration/page/AdministrationPage.vue');
const UserAdminTab = () => import('@/administration/component/UserAdminTab.vue');
const ConfigAdminTab = () => import('@/administration/component/ConfigAdminTab.vue');
const StatisticsAdminTab = () => import('@/administration/component/StatisticsAdminTab.vue');

export const routes: RouteRecordRaw[] = [
    {
        path: '/admin', component: AdministrationPage, redirect: '/admin/users', name: 'admin', children: [
            { path: 'users', component: UserAdminTab, name: 'admin-users' },
            { path: 'config', component: ConfigAdminTab, name: 'admin-config' },
            { path: 'stats', component: StatisticsAdminTab, name: 'admin-stats' },
        ],
        meta: { requiresAuth: true },
    },
];
