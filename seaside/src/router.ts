import {createRouter, createWebHashHistory, RouterOptions} from 'vue-router';
import {routes as adminRoutes} from "@/administration/router";
import {routes as configRoutes} from "@/configuration/router";
import {routes as teamsRoutes} from "@/teams/router";
import {routes as techwatchRoutes} from "@/techwatch/router";

const LoginPage = () => import('@/pages/LoginPage.vue');

export default createRouter({
    history: createWebHashHistory(),
    routes: [
        ...adminRoutes,
        ...teamsRoutes,
        ...configRoutes,
        ...techwatchRoutes,
        // {path: '/news', component: HomePage, name: 'HomePage'},
        // {path: '/clipped', component: HomePage, name: 'ClippedPage'},
        {path: '/login', component: LoginPage, name: 'LoginPage'},
        {path: '/:catchAll(.*)*', redirect: '/news'},
    ]
} as RouterOptions)