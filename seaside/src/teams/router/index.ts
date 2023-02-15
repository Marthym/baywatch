import {RouteRecordRaw} from "vue-router";

const TeamsPage = () => import('@/teams/pages/TeamsPage.vue');

export const routes: RouteRecordRaw[] = [
    {path: '/teams', component: TeamsPage, name: 'TeamsPage'},
];
