import { RouteRecordRaw } from 'vue-router';

const ConfigurationPage = () => import('@/configuration/pages/ConfigurationPage.vue');
const FeedsList = () => import('@/configuration/components/feedslist/FeedsList.vue');
const ProfileTab = () => import('@/configuration/components/profile/ProfileTab.vue');
const SettingsTab = () => import('@/configuration/components/SettingsTab.vue');

export const routes: RouteRecordRaw[] = [
    {
        path: '/config', component: ConfigurationPage, redirect: '/config/feeds', name: 'config', children: [
            { path: 'feeds', component: FeedsList, name: 'config-feeds' },
            { path: 'profile', component: ProfileTab, name: 'config-profile' },
            { path: 'settings', component: SettingsTab, name: 'config-settings' },
        ],
        meta: { requiresAuth: true },
    },
];
