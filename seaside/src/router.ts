import {createRouter, createWebHashHistory, RouterOptions} from 'vue-router';
import {routes as adminRoutes} from "@/administration/router";
import {routes as configRoutes} from "@/configuration/router";

const HomePage = () => import('@/techwatch/pages/HomePage.vue');
const LoginPage = () => import('@/pages/LoginPage.vue');

export default createRouter({
    history: createWebHashHistory(),
    routes: [
        ...adminRoutes,
        ...configRoutes,
        {path: '/news', component: HomePage, name: 'HomePage'},
        {path: '/login', component: LoginPage, name: 'LoginPage'},
        {path: '/:catchAll(.*)*', redirect: '/news'},
    ]
} as RouterOptions)