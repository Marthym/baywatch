import { RouteRecordRaw } from 'vue-router';

const AdministrationPage = () => import('@/administration/page/AdministrationPage.vue');
const UserAdminTab = () => import('@/administration/component/UserAdminTab.vue');
const AdminFeedEditor = () => import('@/administration/component/usereditor/AdminFeedEditor.vue');
const FeedsAdminTab = () => import('@/administration/component/FeedsAdminTab.vue');
const ConfigAdminTab = () => import('@/administration/component/ConfigAdminTab.vue');
const StatisticsAdminTab = () => import('@/administration/component/StatisticsAdminTab.vue');
const UserEditor = () => import('@/administration/component/usereditor/UserEditor.vue');

export const routes: RouteRecordRaw[] = [
    {
        path: '/admin', component: AdministrationPage, redirect: '/admin/users', name: 'admin', children: [
            { path: 'config', component: ConfigAdminTab, name: 'admin-config' },
            {
                path: 'feeds', component: FeedsAdminTab, name: 'admin-feeds', children: [
                    { path: ':id', component: AdminFeedEditor, name: 'admin-feed-editor' },
                ],
            },
            { path: 'stats', component: StatisticsAdminTab, name: 'admin-stats' },
            {
                path: 'users', component: UserAdminTab, name: 'admin-users', children: [
                    { path: ':userId', component: UserEditor, name: 'admin-users-editor' },
                ],
            },
        ],
        meta: { requiresAuth: true },
    },
];
