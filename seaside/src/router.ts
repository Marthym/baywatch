import {createRouter, createWebHistory} from 'vue-router';
import {defineAsyncComponent} from "vue";

// const SideNavTags = defineAsyncComponent(() => import('./SideNavTags.vue').then(m => m.default))

const HomePage = () => import('@/pages/HomePage.vue');
const LoginPage = () => import('@/pages/LoginPage.vue');
const FeedsConfigPage = () => import('@/pages/FeedsConfigPage.vue');

export default createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/news', name: 'HomePage', component: HomePage},
        {path: '/login', component: LoginPage, name: 'LoginPage'},
        {path: '/feeds', component: FeedsConfigPage, name: 'FeedsConfigPage'},
        {path: '/*', redirect: '/news'},
    ]
})