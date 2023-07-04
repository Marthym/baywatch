import {RouteRecordRaw} from "vue-router";

const HomePage = () => import('@/techwatch/pages/HomePage.vue');

export const routes: RouteRecordRaw[] = [
    {path: '/news', component: HomePage, name: 'HomePage'},
    {
        path: '/clipped',
        name: 'ClippedPage',
        component: HomePage,
    },
];
