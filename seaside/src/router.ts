import Vue from 'vue';
import VueRouter from 'vue-router';

const HomePage = () => import('./pages/HomePage.vue');
const LoginPage = () => import('./pages/LoginPage.vue');

Vue.use(VueRouter)

export default new VueRouter({
    routes: [
        {path: '/', component: HomePage, name: 'home'},
        {path: '/login', component: LoginPage, name: 'links'},
    ]
})