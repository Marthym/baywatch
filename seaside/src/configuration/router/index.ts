import {RouteRecordRaw} from "vue-router";

const ConfigurationPage = () => import('@/configuration/pages/ConfigurationPage.vue');

export const routes: RouteRecordRaw[] = [
    {path: '/config', component: ConfigurationPage, name: 'ConfigurationPage'},
];
