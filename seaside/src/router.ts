import {createRouter, createWebHashHistory, RouterOptions} from 'vue-router';

const HomePage = () => import('@/pages/HomePage.vue');
const LoginPage = () => import('@/pages/LoginPage.vue');
const FeedsConfigPage = () => import('@/pages/FeedsConfigPage.vue');

export default createRouter({
    history: createWebHashHistory(),
    routes: [
        {path: '/news', component: HomePage, name: 'HomePage'},
        {path: '/login', component: LoginPage, name: 'LoginPage'},
        {path: '/feeds', component: FeedsConfigPage, name: 'FeedsConfigPage'},
        {path: '/:catchAll(.*)*', redirect: '/news'},
    ]
} as RouterOptions)