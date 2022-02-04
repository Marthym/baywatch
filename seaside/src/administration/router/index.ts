import {RouteRecordRaw} from "vue-router";

const AdministrationPage = () => import('@/administration/page/AdministrationPage.vue');

export const routes: RouteRecordRaw[] = [
    {path: '/admin', component: AdministrationPage, name: 'AdministrationPage'},
];
