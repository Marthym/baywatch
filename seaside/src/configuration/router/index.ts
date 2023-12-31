import { RouteRecordRaw } from 'vue-router';

const ConfigurationPage = () => import('@/configuration/pages/ConfigurationPage.vue');
const FeedsList = () => import('@/configuration/components/feedslist/FeedsList.vue');
const ProfileTab = () => import('@/configuration/components/profile/ProfileTab.vue');

export const routes: RouteRecordRaw[] = [
    {
        path: '/config', component: ConfigurationPage, redirect: '/config/feeds', name: 'config', children: [
            { path: 'feeds', component: FeedsList, name: 'feeds' },
            { path: 'profile', component: ProfileTab, name: 'profile' },
        ],
        meta: { requiresAuth: true },
    },
];
