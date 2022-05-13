import {createRouter, createWebHashHistory, RouterOptions} from 'vue-router';
import {routes as adminRoutes} from "@/administration/router";

const HomePage = () => import('@/techwatch/pages/HomePage.vue');
const LoginPage = () => import('@/pages/LoginPage.vue');
const FeedsConfigPage = () => import('@/techwatch/pages/FeedsConfigPage.vue');

export default createRouter({
    history: createWebHashHistory(),
    routes: [
        ...adminRoutes,
        {path: '/news', component: HomePage, name: 'HomePage'},
        {path: '/login', component: LoginPage, name: 'LoginPage'},
        {path: '/feeds', component: FeedsConfigPage, name: 'FeedsConfigPage'},
        {path: '/:catchAll(.*)*', redirect: '/news'},
    ]
} as RouterOptions)