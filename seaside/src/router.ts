import Vue from 'vue';
import VueRouter from 'vue-router';

const HomePage = () => import('./pages/HomePage.vue');
const LoginPage = () => import('./pages/LoginPage.vue');
const FeedsConfigPage = () => import('@/pages/FeedsConfigPage.vue');

Vue.use(VueRouter)

export default new VueRouter({
    routes: [
        {path: '/news', component: HomePage, name: 'news'},
        {path: '/login', component: LoginPage, name: 'login'},
        {path: '/feeds', component: FeedsConfigPage, name: 'feeds'},
        {path: '*', redirect: '/news'},
    ]
})